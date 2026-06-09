package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.constant.SofProxyStickySortBy;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0317Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickySearchReq;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyStickyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxyStickySearchService {

	private TPILogger logger = TPILogger.tl;

	private DgrSmartOnFhirProxyStickyDao stickyDao;
	private DgrSmartOnFhirProxyDiversionDao diversionDao;

	@Autowired
	public SmartOnFhirProxyStickySearchService(DgrSmartOnFhirProxyStickyDao stickyDao,
			DgrSmartOnFhirProxyDiversionDao diversionDao) {
		this.stickyDao = stickyDao;
		this.diversionDao = diversionDao;
	}

	public DPB0317Resp queryStickyList(TsmpAuthorization auth, SmartOnFhirProxyStickySearchReq req) {
		DPB0317Resp resp = new DPB0317Resp();

		try {
			validatePaginationParams(req);

			// 解析精確匹配的 ID（String → Long）
			Long sofProxyId = parseOptionalId(req.getSofProxyId(), SmartOnFhirProxyStickySearchReq.Fields.sofProxyId);
			Long sofProxyDiversionId = parseOptionalId(req.getSofProxyDiversionId(),
					SmartOnFhirProxyStickySearchReq.Fields.sofProxyDiversionId);

			Pageable pageable = createPageable(req);

			Page<DgrSmartOnFhirProxySticky> page = stickyDao.findByFiltersWithPagination(
					sofProxyId,
					sofProxyDiversionId,
					req.getSofProxyStickyType(),
					req.getKeywords(),
					pageable);

			List<SmartOnFhirProxyStickyDto> dtoList = convertToDto(page.getContent());

			// 批次查詢 Diversion URL 並填入 DTO
			populateDiversionUrl(dtoList, page.getContent());

			resp.setContent(dtoList);
			resp.setTotalElements(page.getTotalElements());
			resp.setTotalPages(page.getTotalPages());
			resp.setNumber(page.getNumber());
			resp.setSize(page.getSize());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private void validatePaginationParams(SmartOnFhirProxyStickySearchReq req) {
		if (req.getPageNum() == null || req.getPageNum() < 0) {
			String fieldName = SmartOnFhirProxyStickySearchReq.Fields.pageNum;
			String msg = "Field '" + fieldName + "' is invalid: " + req.getPageNum() + ". Page number must be >= 0.";
			this.logger.error(msg);
			throw TsmpDpAaRtnCode._1559.throwing(msg);
		}

		if (req.getPageSize() == null || req.getPageSize() <= 0) {
			String fieldName = SmartOnFhirProxyStickySearchReq.Fields.pageSize;
			String msg = "Field '" + fieldName + "' is invalid: " + req.getPageSize() + ". Page size must be > 0.";
			this.logger.error(msg);
			throw TsmpDpAaRtnCode._1559.throwing(msg);
		}

		if (req.getPageSize() > 1000) {
			req.setPageSize(1000);
		}
	}

	private Pageable createPageable(SmartOnFhirProxyStickySearchReq req) {
		String sortBy = req.getSortBy();
		SofProxyStickySortBy sortByEnum = SofProxyStickySortBy.SOF_PROXY_STICKY_ID;

		if (StringUtils.hasText(sortBy)) {
			Optional<SofProxyStickySortBy> opt = SofProxyStickySortBy.fromKey(sortBy);
			if (opt.isPresent()) {
				sortByEnum = opt.get();
			} else {
				String msg = "Field 'sortBy' is invalid: " + sortBy + ". Allowed values are: "
						+ SofProxyStickySortBy.allowedKeys().toString();
				this.logger.error(msg);
				throw TsmpDpAaRtnCode._1559.throwing(msg);
			}
		}

		Sort.Direction direction = Sort.Direction.fromOptionalString(req.getSortOrder()).orElse(Sort.Direction.DESC);
		Sort sort = Sort.by(direction, sortByEnum.key());

		return PageRequest.of(req.getPageNum(), req.getPageSize(), sort);
	}

	/**
	 * 解析選填的 ID 參數（String → Long），空值返回 null
	 */
	private Long parseOptionalId(String value, String fieldName) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return IdCodec.parseLong(value, fieldName, logger).getOrThrow();
	}

	private void populateDiversionUrl(List<SmartOnFhirProxyStickyDto> dtoList,
			List<DgrSmartOnFhirProxySticky> entityList) {
		// 收集不重複的 diversionId
		List<Long> diversionIds = entityList.stream()
				.map(DgrSmartOnFhirProxySticky::getSofProxyDiversionId)
				.distinct()
				.collect(Collectors.toList());

		if (diversionIds.isEmpty()) {
			return;
		}

		// 批次查詢建立 diversionId → url 的映射
		Map<Long, String> diversionUrlMap = diversionDao.findAllById(diversionIds).stream()
				.collect(Collectors.toMap(
						DgrSmartOnFhirProxyDiversion::getSofProxyDiversionId,
						DgrSmartOnFhirProxyDiversion::getSofProxyDiversionUrl,
						(a, b) -> a));

		// 填入每個 DTO
		for (int i = 0; i < dtoList.size(); i++) {
			Long diversionId = entityList.get(i).getSofProxyDiversionId();
			dtoList.get(i).setSofProxyDiversionUrl(diversionUrlMap.get(diversionId));
		}
	}

	private List<SmartOnFhirProxyStickyDto> convertToDto(List<DgrSmartOnFhirProxySticky> entityList) {
		List<SmartOnFhirProxyStickyDto> dtoList = new ArrayList<>();
		for (DgrSmartOnFhirProxySticky entity : entityList) {
			dtoList.add(convertToDto(entity));
		}
		return dtoList;
	}

	static SmartOnFhirProxyStickyDto convertToDto(DgrSmartOnFhirProxySticky entity) {
		SmartOnFhirProxyStickyDto dto = new SmartOnFhirProxyStickyDto();
		dto.setSofProxyStickyId(IdCodec.toString(entity.getSofProxyStickyId()));
		dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
		dto.setSofProxyDiversionId(IdCodec.toString(entity.getSofProxyDiversionId()));
		dto.setSofProxyStickyType(entity.getSofProxyStickyType());
		dto.setSofProxyStickyTypeId(entity.getSofProxyStickyTypeId());
		dto.setSofProxyStickyVerb(entity.getSofProxyStickyVerb());
		dto.setSofProxyStickyPath(entity.getSofProxyStickyPath());
		dto.setSofProxyStickyHashcode(entity.getSofProxyStickyHashcode());
		if (entity.getSofProxyStickyInteraction() != null) {
			dto.setSofProxyStickyInteraction(entity.getSofProxyStickyInteraction().name());
		}
		dto.setCreateDateTime(entity.getCreateDateTime());
		dto.setCreateUser(entity.getCreateUser());
		dto.setUpdateDateTime(entity.getUpdateDateTime());
		dto.setUpdateUser(entity.getUpdateUser());
		dto.setVersion(entity.getVersion().toString());
		return dto;
	}
}
