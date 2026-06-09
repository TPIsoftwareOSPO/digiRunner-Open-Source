package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0319Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0319Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0320Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0320Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickyUpdateDto;
import tpi.dgrv4.entity.constant.FhirInteraction;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyStickyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyStickyService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxyStickyCreateUpdateDeleteService {

	private TPILogger logger = TPILogger.tl;

	private DgrSmartOnFhirProxyStickyDao stickyDao;
	private DgrSmartOnFhirProxyDao proxyDao;
	private DgrSmartOnFhirProxyDiversionDao diversionDao;
	private SmartOnFhirProxyStickyService stickyService;

	@Autowired
	public SmartOnFhirProxyStickyCreateUpdateDeleteService(
			DgrSmartOnFhirProxyStickyDao stickyDao,
			DgrSmartOnFhirProxyDao proxyDao,
			DgrSmartOnFhirProxyDiversionDao diversionDao,
			SmartOnFhirProxyStickyService stickyService) {
		this.stickyDao = stickyDao;
		this.proxyDao = proxyDao;
		this.diversionDao = diversionDao;
		this.stickyService = stickyService;
	}

	/**
	 * DPB0318: 新增 Sticky
	 */
	@Transactional
	public SmartOnFhirProxyStickyDto addSticky(TsmpAuthorization auth, SmartOnFhirProxyStickyDto reqDto) {
		try {
			// 1. 驗證必填欄位
			validateCreateRequired(reqDto);

			// 2. 轉換 ID
			Long sofProxyId = IdCodec.parseLong(reqDto.getSofProxyId(), "sofProxyId", logger).getOrThrow();
			Long sofProxyDiversionId = IdCodec.parseLong(reqDto.getSofProxyDiversionId(), "sofProxyDiversionId", logger)
					.getOrThrow();

			// 3. 驗證 Proxy 存在
			Optional<DgrSmartOnFhirProxy> proxyOpt = proxyDao.findById(sofProxyId);
			if (!proxyOpt.isPresent()) {
				throw TsmpDpAaRtnCode._1559.throwing(
						"Proxy with ID '" + reqDto.getSofProxyId() + "' not found.");
			}

			// 4. 驗證 Diversion 存在且屬於該 Proxy
			Optional<DgrSmartOnFhirProxyDiversion> diversionOpt = diversionDao.findById(sofProxyDiversionId);
			if (!diversionOpt.isPresent()) {
				throw TsmpDpAaRtnCode._1559.throwing(
						"Diversion with ID '" + reqDto.getSofProxyDiversionId() + "' not found.");
			}
			if (!diversionOpt.get().getSofProxyId().equals(sofProxyId)) {
				throw TsmpDpAaRtnCode._1559.throwing(
						"Diversion '" + reqDto.getSofProxyDiversionId() +
								"' does not belong to Proxy '" + reqDto.getSofProxyId() + "'.");
			}

			// 5. 計算 hashcode 並檢查重複
			String hashcode = SmartOnFhirProxyStickyService.calculateStickyHashcode(
					sofProxyId, reqDto.getSofProxyStickyType());
			Optional<DgrSmartOnFhirProxySticky> existingOpt = stickyDao
					.findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, hashcode);
			if (existingOpt.isPresent()) {
				throw TsmpDpAaRtnCode._1559.throwing(
						"Duplicate binding: Proxy '" + reqDto.getSofProxyId() +
								"' already has a sticky record for type '" + reqDto.getSofProxyStickyType() + "'.");
			}

			// 6. 驗證選填欄位
			validateOptionalFields(reqDto);

			// 7. 建立 Entity
			DgrSmartOnFhirProxySticky entity = new DgrSmartOnFhirProxySticky();
			entity.setSofProxyId(sofProxyId);
			entity.setSofProxyDiversionId(sofProxyDiversionId);
			entity.setSofProxyStickyType(reqDto.getSofProxyStickyType());
			entity.setSofProxyStickyTypeId(reqDto.getSofProxyStickyTypeId());
			entity.setSofProxyStickyVerb(reqDto.getSofProxyStickyVerb());
			entity.setSofProxyStickyPath(reqDto.getSofProxyStickyPath());
			entity.setSofProxyStickyHashcode(hashcode);

			if (StringUtils.hasText(reqDto.getSofProxyStickyInteraction())) {
				entity.setSofProxyStickyInteraction(
						FhirInteraction.valueOf(reqDto.getSofProxyStickyInteraction()));
			}

			entity.setCreateDateTime(DateTimeUtil.now());
			entity.setCreateUser(auth != null ? auth.getUserName() : "SYSTEM");

			// 8. 儲存
			DgrSmartOnFhirProxySticky saved = stickyDao.save(entity);

			// 9. 預熱快取
			stickyService.putCache(hashcode, saved);

			logger.debug("Sticky record created: id=" + saved.getSofProxyStickyId() +
					", type=" + saved.getSofProxyStickyType() + ", hashcode=" + hashcode);

			return SmartOnFhirProxyStickySearchService.convertToDto(saved);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	/**
	 * DPB0319: 批次更新 Sticky
	 */
	@Transactional
	public DPB0319Resp batchUpdateSticky(TsmpAuthorization auth, DPB0319Req req) {
		try {
			if (req.getStickyList() == null || req.getStickyList().isEmpty()) {
				throw TsmpDpAaRtnCode._1559.throwing("Sticky list cannot be empty.");
			}

			List<SmartOnFhirProxyStickyDto> updatedList = new ArrayList<>();
			int index = 0;

			for (SmartOnFhirProxyStickyUpdateDto updateDto : req.getStickyList()) {
				// 驗證必填
				validateUpdateItem(updateDto, index);

				Long stickyId = IdCodec.parseLong(updateDto.getSofProxyStickyId(),
						"Sticky[" + index + "].sofProxyStickyId", logger).getOrThrow();
				Long versionLong = IdCodec.parseLong(updateDto.getVersion(),
						"Sticky[" + index + "].version", logger).getOrThrow();

				// 查詢現有紀錄
				Optional<DgrSmartOnFhirProxySticky> entityOpt = stickyDao.findById(stickyId);
				if (!entityOpt.isPresent()) {
					throw TsmpDpAaRtnCode._1559.throwing(
							"Sticky[" + index + "] with ID '" + updateDto.getSofProxyStickyId() + "' not found.");
				}

				DgrSmartOnFhirProxySticky entity = entityOpt.get();

				// 樂觀鎖檢查
				if (!entity.getVersion().equals(versionLong)) {
					throw TsmpDpAaRtnCode._1559.throwing(
							"Version mismatch for Sticky[" + index + "]: expected " +
									entity.getVersion() + ", found " + updateDto.getVersion() +
									". Please refresh the data and try again.");
				}

				String oldHashcode = entity.getSofProxyStickyHashcode();
				boolean hashcodeChanged = false;

				// 套用 JsonNullable 更新
				if (updateDto.getSofProxyDiversionId().isPresent()) {
					Long newDiversionId = IdCodec.parseLong(updateDto.getSofProxyDiversionId().get(),
							"Sticky[" + index + "].sofProxyDiversionId", logger).getOrThrow();

					// 驗證新 Diversion 存在且屬於原 Proxy
					Optional<DgrSmartOnFhirProxyDiversion> divOpt = diversionDao.findById(newDiversionId);
					if (!divOpt.isPresent()) {
						throw TsmpDpAaRtnCode._1559.throwing(
								"Sticky[" + index + "]: Diversion '" + updateDto.getSofProxyDiversionId().get()
										+ "' not found.");
					}
					if (!divOpt.get().getSofProxyId().equals(entity.getSofProxyId())) {
						throw TsmpDpAaRtnCode._1559.throwing(
								"Sticky[" + index + "]: Diversion '" + updateDto.getSofProxyDiversionId().get()
										+ "' does not belong to Proxy '" + entity.getSofProxyId() + "'.");
					}
					entity.setSofProxyDiversionId(newDiversionId);
				}

				if (updateDto.getSofProxyStickyType().isPresent()) {
					String newType = updateDto.getSofProxyStickyType().get();
					if (!StringUtils.hasText(newType)) {
						throw TsmpDpAaRtnCode._1559.throwing(
								"Sticky[" + index + "]: sofProxyStickyType cannot be empty.");
					}
					if (newType.length() > 200) {
						throw TsmpDpAaRtnCode._1559.throwing(
								"Sticky[" + index + "]: sofProxyStickyType exceeds max length 200.");
					}
					entity.setSofProxyStickyType(newType);
					hashcodeChanged = true;
				}

				if (updateDto.getSofProxyStickyTypeId().isPresent()) {
					entity.setSofProxyStickyTypeId(updateDto.getSofProxyStickyTypeId().get());
				}

				if (updateDto.getSofProxyStickyVerb().isPresent()) {
					entity.setSofProxyStickyVerb(updateDto.getSofProxyStickyVerb().get());
				}

				if (updateDto.getSofProxyStickyPath().isPresent()) {
					entity.setSofProxyStickyPath(updateDto.getSofProxyStickyPath().get());
				}

				if (updateDto.getSofProxyStickyInteraction().isPresent()) {
					String interactionStr = updateDto.getSofProxyStickyInteraction().get();
					if (StringUtils.hasText(interactionStr)) {
						try {
							entity.setSofProxyStickyInteraction(FhirInteraction.valueOf(interactionStr));
						} catch (IllegalArgumentException e) {
							throw TsmpDpAaRtnCode._1559.throwing(
									"Sticky[" + index + "]: Invalid interaction '" + interactionStr + "'.");
						}
					} else {
						entity.setSofProxyStickyInteraction(null);
					}
				}

				// 若 type 變更，重算 hashcode
				String newHashcode = oldHashcode;
				if (hashcodeChanged) {
					newHashcode = SmartOnFhirProxyStickyService.calculateStickyHashcode(
							entity.getSofProxyId(), entity.getSofProxyStickyType());

					// 檢查新 hashcode 是否重複（排除自己）
					Optional<DgrSmartOnFhirProxySticky> dupOpt = stickyDao
							.findBySofProxyIdAndSofProxyStickyHashcode(entity.getSofProxyId(), newHashcode);
					if (dupOpt.isPresent() && !dupOpt.get().getSofProxyStickyId().equals(stickyId)) {
						throw TsmpDpAaRtnCode._1559.throwing(
								"Sticky[" + index + "]: Duplicate binding for type '"
										+ entity.getSofProxyStickyType() + "'.");
					}
					entity.setSofProxyStickyHashcode(newHashcode);
				}

				// 更新系統欄位
				entity.setUpdateDateTime(DateTimeUtil.now());
				entity.setUpdateUser(auth != null ? auth.getUserName() : "SYSTEM");

				// 儲存
				DgrSmartOnFhirProxySticky saved = stickyDao.saveAndFlush(entity);

				// 快取管理
				if (hashcodeChanged && !oldHashcode.equals(newHashcode)) {
					stickyService.evictCache(oldHashcode);
				}
				stickyService.putCache(newHashcode, saved);

				updatedList.add(SmartOnFhirProxyStickySearchService.convertToDto(saved));
				index++;
			}

			DPB0319Resp resp = new DPB0319Resp();
			resp.setSuccessCount(updatedList.size());
			resp.setUpdatedStickies(updatedList);
			return resp;

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	/**
	 * DPB0320: 批次刪除 Sticky
	 */
	@Transactional
	public DPB0320Resp batchDeleteSticky(TsmpAuthorization auth, DPB0320Req req) {
		try {
			if (req.getStickyList() == null || req.getStickyList().isEmpty()) {
				throw TsmpDpAaRtnCode._1559.throwing("Sticky list cannot be empty.");
			}

			List<String> deletedIds = new ArrayList<>();
			int index = 0;

			for (SmartOnFhirProxyStickyUpdateDto deleteDto : req.getStickyList()) {
				// 驗證必填
				validateDeleteItem(deleteDto, index);

				Long stickyId = IdCodec.parseLong(deleteDto.getSofProxyStickyId(),
						"Sticky[" + index + "].sofProxyStickyId", logger).getOrThrow();
				Long versionLong = IdCodec.parseLong(deleteDto.getVersion(),
						"Sticky[" + index + "].version", logger).getOrThrow();

				// 查詢
				Optional<DgrSmartOnFhirProxySticky> entityOpt = stickyDao.findById(stickyId);
				if (!entityOpt.isPresent()) {
					throw TsmpDpAaRtnCode._1559.throwing(
							"Sticky[" + index + "] with ID '" + deleteDto.getSofProxyStickyId() + "' not found.");
				}

				DgrSmartOnFhirProxySticky entity = entityOpt.get();

				// 樂觀鎖檢查
				if (!entity.getVersion().equals(versionLong)) {
					throw TsmpDpAaRtnCode._1559.throwing(
							"Version mismatch for Sticky[" + index + "]: expected " +
									entity.getVersion() + ", found " + deleteDto.getVersion() +
									". Please refresh the data and try again.");
				}

				// 清除快取
				stickyService.evictCache(entity.getSofProxyStickyHashcode());

				// 刪除
				stickyDao.delete(entity);
				deletedIds.add(IdCodec.toString(entity.getSofProxyStickyId()));

				logger.debug("Sticky record deleted: id=" + entity.getSofProxyStickyId());
				index++;
			}

			DPB0320Resp resp = new DPB0320Resp();
			resp.setSuccessCount(deletedIds.size());
			resp.setDeletedStickyIds(deletedIds);
			return resp;

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	// ===== Validation Helpers =====

	private void validateCreateRequired(SmartOnFhirProxyStickyDto dto) {
		if (!StringUtils.hasText(dto.getSofProxyId())) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyId' is required.");
		}
		if (!StringUtils.hasText(dto.getSofProxyDiversionId())) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyDiversionId' is required.");
		}
		if (!StringUtils.hasText(dto.getSofProxyStickyType())) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyStickyType' is required.");
		}
	}

	private void validateOptionalFields(SmartOnFhirProxyStickyDto dto) {
		if (dto.getSofProxyStickyType() != null && dto.getSofProxyStickyType().length() > 200) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyStickyType' exceeds max length 200.");
		}
		if (dto.getSofProxyStickyTypeId() != null && dto.getSofProxyStickyTypeId().length() > 200) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyStickyTypeId' exceeds max length 200.");
		}
		if (dto.getSofProxyStickyVerb() != null && dto.getSofProxyStickyVerb().length() > 50) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyStickyVerb' exceeds max length 50.");
		}
		if (dto.getSofProxyStickyPath() != null && dto.getSofProxyStickyPath().length() > 2000) {
			throw TsmpDpAaRtnCode._1559.throwing("Field 'sofProxyStickyPath' exceeds max length 2000.");
		}
		if (StringUtils.hasText(dto.getSofProxyStickyInteraction())) {
			try {
				FhirInteraction.valueOf(dto.getSofProxyStickyInteraction());
			} catch (IllegalArgumentException e) {
				throw TsmpDpAaRtnCode._1559.throwing(
						"Field 'sofProxyStickyInteraction' is invalid: '" + dto.getSofProxyStickyInteraction() + "'.");
			}
		}
	}

	private void validateUpdateItem(SmartOnFhirProxyStickyUpdateDto dto, int index) {
		if (!StringUtils.hasText(dto.getSofProxyStickyId())) {
			throw TsmpDpAaRtnCode._1559.throwing(
					"Sticky[" + index + "]: sofProxyStickyId is required.");
		}
		if (!StringUtils.hasText(dto.getVersion())) {
			throw TsmpDpAaRtnCode._1559.throwing(
					"Sticky[" + index + "]: version is required.");
		}
	}

	private void validateDeleteItem(SmartOnFhirProxyStickyUpdateDto dto, int index) {
		if (!StringUtils.hasText(dto.getSofProxyStickyId())) {
			throw TsmpDpAaRtnCode._1559.throwing(
					"Sticky[" + index + "]: sofProxyStickyId is required for deletion.");
		}
		if (!StringUtils.hasText(dto.getVersion())) {
			throw TsmpDpAaRtnCode._1559.throwing(
					"Sticky[" + index + "]: version is required for deletion.");
		}
	}
}
