package tpi.dgrv4.dpaa.component.apptJob;

import static tpi.dgrv4.common.utils.StackTraceUtil.getLineNumber;
import static tpi.dgrv4.dpaa.util.ServiceUtil.deepCopy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDetectResult;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherJobHelper;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertEvent;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierCustom;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierLine;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierRoleEmail;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class DpaaAlertDetectorJobKeyword extends DpaaAlertDetectorJob<DpaaAlertDetectorJobKeywordParams> {

	private TPILogger logger = TPILogger.tl;

//	@Autowired(required = false)
	private DgrESService dgrESService;
	
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public DpaaAlertDetectorJobKeyword(TsmpDpApptJob tsmpDpApptJob, ObjectMapper objectMapper,
			ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao,
			DpaaAlertNotifierRoleEmail dpaaAlertNotifierRoleEmail, DpaaAlertNotifierLine dpaaAlertNotifierLine,
			DpaaAlertNotifierCustom dpaaAlertNotifierCustom, DpaaAlertDispatcherJobHelper dpaaAlertDispatcherJobHelper,
			TsmpAlertDao tsmpAlertDao, @Nullable DgrESService dgrESService, TsmpSettingService tsmpSettingService)
			throws Exception {
		super(tsmpDpApptJob, objectMapper, DpaaAlertDetectorJobKeywordParams.class, apptJobDispatcher, tsmpDpApptJobDao,
				dpaaAlertNotifierRoleEmail, dpaaAlertNotifierLine, dpaaAlertNotifierCustom,
				dpaaAlertDispatcherJobHelper, tsmpAlertDao);

		this.dgrESService = dgrESService;
		this.tsmpSettingService = tsmpSettingService;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DpaaAlertDetectResult detect(DpaaAlertDetectorJobKeywordParams params, Date currentDetectSDt) {
		TsmpAlert cri = params.getTsmpAlert();
		
		long elapsedTime = getElapsedTime();

		// 檢查 duration
		int duration = cri.getDuration();
		boolean isAction = checkActionTime(elapsedTime, duration);
		if (isAction) {
			checkAlertRule(params, currentDetectSDt);
			if (StringUtils.hasLength(params.getLastAlertDt())) {
				this.logger.trace(String.format("儲存告警偵測結果: [%d]-%s", //
					cri.getAlertId(), params.getLastAlertDt()));
			}
		}

		// 檢查 alert interval
		int interval = cri.getAlertInterval();
		boolean isOnInterval = isOnAlertInterval(elapsedTime, interval);
		if (isOnInterval) {
			// 因為只有 isAlert = true 時才會存入 lastAlertDt
			if (StringUtils.hasLength(params.getLastAlertDt())) {
				DpaaAlertDetectResult result = new DpaaAlertDetectResult();
				result.setAlert(true);
				result.setEntity(deepCopy(cri, TsmpAlert.class));
				result.setPayload(deepCopy(params.getAlertPayload(), Map.class));
				params.clearAlertRecord(); // 發過告警就清空觸警的紀錄
				return result;
			} else {
				setStackTrace(String.format("忽略告警偵測: 未曾觸警, 告警編號=%d, 問題持續時間=(%d/%d)", //
					cri.getAlertId(), elapsedTime, duration
				), getLineNumber());
			}
		} else {
			setStackTrace(String.format("忽略告警偵測: 現不在告警間隔上, 告警編號=%d, 間隔=(%d/%d)", //
				cri.getAlertId(), elapsedTime, interval
			), getLineNumber());
		}
		
		return new DpaaAlertDetectResult();
	}

	@Override
	protected DpaaAlertEvent buildAlertEvent(DpaaAlertDetectResult detectResult) {
		DpaaAlertEvent dpaaAlertEvent = new DpaaAlertEvent(detectResult);
		Object alertMsg = detectResult.getPayload().get("alertMsg");
		dpaaAlertEvent.getEntity().setAlertMsg(String.valueOf(alertMsg));
		dpaaAlertEvent.setAlertType(detectResult.getEntity().getAlertType());
		return dpaaAlertEvent;
	}

	private boolean checkActionTime(long elapsedTime, int duration) {
		// 多久檢查一次(秒)
		if (elapsedTime % duration == 0) {
			return true;
		}
		return false;
	}

	private void checkAlertRule(DpaaAlertDetectorJobKeywordParams params, Date currentDetectSDt) {
		params.clearAlertRecord();

		TsmpAlert entity = params.getTsmpAlert();
		
		// 資料起訖時間
		Date endTime = currentDetectSDt;
		int offset = entity.getDuration() * 2;
		Date startTime = DateUtils.addSeconds(endTime, -offset);
		
		// 告警類型
		String alertType = entity.getAlertType();
		
		// ES搜尋條件
		String esSearchPayload = entity.getEsSearchPayload();
		if (!StringUtils.hasLength(esSearchPayload)) {
			this.logger.error("未設定 EsSearchPayload, 無法轉換 ES 查詢語法");
			return;
		}

		// 搜尋筆數上限
		int threshold = entity.getThreshold();
		int size = threshold + 1;
		
		// 時間欄位
		String timeField = "ts";	// 預設搜尋 tsmp_api_log		

		// 要搜尋的索引名稱
		String endTimeStr = DateTimeUtil.dateTimeToString(endTime, DateTimeFormatEnum.西元年月日_3).orElse(null);
		List<String> indices = Arrays.asList(new String[] {"tsmp_api_log_" + endTimeStr});
		String startTimeStr = DateTimeUtil.dateTimeToString(startTime, DateTimeFormatEnum.西元年月日_3).orElse(null);
		
		if (!StringUtils.hasLength(startTimeStr)) {
			throw TsmpDpAaRtnCode._1381.throwing();
		}
		
		if (!startTimeStr.equals(endTimeStr)) {	// 起訖不同日
			indices.add("tsmp_api_log_" + startTimeStr);
		}
		
		int queryCount = 0;
		try {
			queryCount = countAlertData(startTime, endTime, alertType, esSearchPayload, size, timeField, indices);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}

		if (queryCount > threshold) {
			params.saveLastAlertDt(currentDetectSDt);
			// 組成 alertMsg
			String alertMsg = entity.getAlertMsg();
			alertMsg = alertMsg.replace("$THRESHOLD", String.valueOf(entity.getThreshold()))
				.replace("$DURATION", String.valueOf(entity.getDuration()))
				.replace("$INTERVAL", String.valueOf(entity.getAlertInterval()));
			params.addToPayload("alertMsg", alertMsg);
		}

		// 記 log
		this.logger.trace(String.format("偵測結果: 過去%d秒內, 找到%d筆符合的紀錄(告警門檻=%d). 告警編號=%d", //
			offset, queryCount, threshold, entity.getAlertId()));
	}

	private boolean isOnAlertInterval(long elapsedTime, int interval) {
		if (elapsedTime % interval == 0) {
			return true;
		}
		return false;
	}

	protected int countAlertData(Date startTime, Date endTime, String alertType, String esSearchPayload, int size, String timeField, List<String> indices) throws Exception {
		return this.dgrESService.countAlertData(startTime, endTime, alertType, esSearchPayload, size, timeField, indices);
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}