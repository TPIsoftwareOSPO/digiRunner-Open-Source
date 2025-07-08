package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.config.UrlMappingRefresher;
import tpi.dgrv4.dpaa.es.ESLogBuffer;
import tpi.dgrv4.dpaa.vo.DPB9903Req;
import tpi.dgrv4.dpaa.vo.DPB9903Resp;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.BotDetectionUpdatePacket;
import tpi.dgrv4.gateway.TCP.Packet.UpdateComposerTSPacket;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;
import tpi.dgrv4.gateway.service.DgrApiLog2ESQueue;
import tpi.dgrv4.gateway.service.IKibanaService2;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class DPB9903Service {
	public enum EncrptionType {
		ENC, TAEASK, NONE
	}

	// @Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
	// @Autowired(required = false)
	private IKibanaService2 kibanaService2;
	
	private LicenseUtilBase licenseUtilBase;
	
	private final TsmpSettingDao tsmpSettingDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpTAEASKHelper tsmpTAEASKHelper;
	private final DaoGenericCacheService daoGenericCacheService;
	private final ComposerWebSocketClientConn composerWebSocketClientConn;
	private final UrlMappingRefresher urlMappingRefresher;
	
	@Autowired
	public void setDpb9903Service(ApplicationContext applicationContext, @Nullable TsmpCoreTokenBase tsmpCoreTokenBase,
			@Nullable IKibanaService2 kibanaService2) {
		this.tsmpCoreTokenBase = tsmpCoreTokenBase;
		this.kibanaService2 = kibanaService2;

		if (applicationContext != null) {
			this.licenseUtilBase = applicationContext.getBean(LicenseUtilBase.class); // non-Singleton
		}
	}

	public DPB9903Resp updateTsmpSetting(TsmpAuthorization auth, DPB9903Req req, InnerInvokeParam iip) {

		// 寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_TSMP_SETTING.value());

		checkParams(req);

		try {
			TsmpSetting oldSetting = findOldSetting(req);
			if (oldSetting == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			checkInput(req);
			TsmpSetting newSetting = updateOldSetting(oldSetting, req, iip);

			/**
			 * 
			 * Composer專用邏輯，若更新的ID為COMPOSER_LOG_SWICTH、COMPOSER_LOG_SIZE、COMPOSER_LOG_INTERVAL、COMPOSER_LOG_MAX_FILES。
			 * 需要更新AA0325Controller.ts。
			 * 
			 */
			boolean isComposerFlag = false;
			String id = newSetting.getId().toUpperCase();
			isComposerFlag = "COMPOSER_LOG_SWICTH".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_SIZE".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_INTERVAL".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_LOG_MAX_FILES".equals(id);
			isComposerFlag = isComposerFlag || "COMPOSER_REQUEST_TIMEOUT".equals(id);
			if (isComposerFlag) {
				TPILogger.lc.send(new UpdateComposerTSPacket());
			}

			refreshCache(newSetting);
			freshUrlMappings(newSetting);
			freshBotDetectionRuleValidator(newSetting);

			// 因為composer address被更新,websocket要重連
			if ("TSMP_COMPOSER_ADDRESS".equals(req.getId())) {
				restartWs();
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.SETTING.value());

			if (kibanaService2 != null) {
				kibanaService2.clearCacheMap();
				TPILogger.tl.info("Clear Kibana cache");
			}

			// When updating, it should also be logged as info.
			logSomeInfo(id);

			return new DPB9903Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1286.throwing();
		}
	}

	private void checkInput(DPB9903Req req) {

		String id = req.getId();

		if (TsmpSettingDao.Key.TSMP_EDITION.equals(id)) {

			String key = req.getNewVal();

			try {
				if (licenseUtilBase != null) { // for Enterprise
					licenseUtilBase.initLicenseUtil(key, new ArrayList<>());
					licenseUtilBase.getExpiryDate(key);
				}
			} catch (Exception e) {
				TPILogger.tl.error(
						"Invalid license, id=" + id + ", newVal=" + req.getNewVal() + ", error: " + e.getMessage());
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1559.throwing("Invalid license key, please verify license.key");
			}
		}
	}

	private void logSomeInfo(String id) {
		// Bulk 寫入 Elastic fail 時 retry 3 次, 與寫入 Bulk file 前做 url detection 異動時印出
		if (id.equals(TsmpSettingDao.Key.ES_CHECK_CONNECTION) || id.equals(TsmpSettingDao.Key.ES_LOGFILE_FAIL_RETRY)) {
			DgrApiLog2ESQueue.esUrlCheckConnection = getTsmpSettingDao()
					.findById(TsmpSettingDao.Key.ES_CHECK_CONNECTION)
					.map(TsmpSetting::getValue)
					.map(Boolean::parseBoolean) // String 轉換為 boolean
					.orElse(false); // 如果沒有找到值，默認為 false

			ESLogBuffer.enableRetry = getTsmpSettingDao().findById(TsmpSettingDao.Key.ES_LOGFILE_FAIL_RETRY)
					.map(TsmpSetting::getValue)
					.map(Boolean::parseBoolean) // String 轉換為 boolean
					.orElse(false); // 如果沒有找到值，默認為 false

			TPILogger.tl.info(String.format("\n...ES_CHECK_CONNECTION=[%b] ,ES_LOGFILE_FAIL_RETRY=[%b]",
					DgrApiLog2ESQueue.esUrlCheckConnection,
					ESLogBuffer.enableRetry));
		}

	}

	private void freshBotDetectionRuleValidator(TsmpSetting newSetting) {

		if (newSetting != null) {
			String id = newSetting.getId();
			if (TsmpSettingDao.Key.CHECK_BOT_DETECTION.equals(id) || TsmpSettingDao.Key.BOT_DETECTION_LOG.equals(id)) {
				BotDetectionUpdatePacket packet = new BotDetectionUpdatePacket();
				TPILogger.lc.send(packet);
			}
		}
	}

	private void freshUrlMappings(TsmpSetting newSetting) {

		String id = newSetting.getId();

		if (TsmpSettingDao.Key.DGR_AC_LOGIN_PAGE.equalsIgnoreCase(id)) {
			urlMappingRefresher.refreshUrlMappings();
		}
	}

	protected void restartWs() {
		getComposerWebSocketClientConn().restart();
	}

	protected void checkParams(DPB9903Req req) {
		String id = req.getId();

		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected TsmpSetting findOldSetting(DPB9903Req req) {
		String id = req.getId();
		String oldVal = req.getOldVal();
		TsmpSetting oldSetting = getTsmpSettingDao().findByIdAndValue(id, oldVal);
		return oldSetting;
	}

	protected TsmpSetting updateOldSetting(TsmpSetting setting, DPB9903Req req, InnerInvokeParam iip) throws Exception {
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, setting); // 舊資料統一轉成 String

		String newVal = req.getNewVal();
		String memo = req.getMemo();
		if (req.getEncrptionType().equals(EncrptionType.ENC.toString())) {
			String encEncode = getENCEncode(newVal);
			setting.setValue(encEncode);
		} else if (req.getEncrptionType().equals(EncrptionType.TAEASK.toString())) {
			String TAEASKEncode = getTAEASKEncode(newVal);
			setting.setValue(TAEASKEncode);
		} else {
			setting.setValue(newVal);
		}

		// setting.setValue(newVal);
		setting.setMemo(memo);
		setting = getTsmpSettingDao().saveAndFlush(setting);

		// 寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpSetting.class.getSimpleName(), TableAct.U.value(),
				oldRowStr, setting);// U

		return setting;
	}

	protected String getENCEncode(String value) throws Exception {
		String encoded = getTsmpCoreTokenBase().encrypt(value);
		encoded = String.format("ENC(%s)", encoded);
		TPILogger.tl.debug("ENC encode  " + encoded);
		return encoded;
	}

	protected String getTAEASKEncode(String value) {
		String encoded = null;
		encoded = getTsmpTAEASKHelper().encrypt(value);
		TPILogger.tl.debug("TAEASK encode   " + encoded);
		return encoded;
	}

	protected void refreshCache(TsmpSetting newSetting) {
		/*
		 * String id = newSetting.getId(); getTsmpSettingCacheProxy().findById(id);
		 */
		getDaoGenericCacheService().clearAndNotify();
	}
}
