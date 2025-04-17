package tpi.dgrv4.dpaa.component.apptJob;

import static tpi.dgrv4.common.utils.StackTraceUtil.getLineNumber;
import static tpi.dgrv4.dpaa.util.ServiceUtil.deepCopy;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDetectResult;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherJobHelper;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertEvent;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierCustom;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierLine;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertNotifierRoleEmail;
import tpi.dgrv4.dpaa.constant.DpaaAlertType;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class DpaaAlertDetectorJobSystemBasic extends DpaaAlertDetectorJob<DpaaAlertDetectorJobSystemBasicParams> {

	private TPILogger logger = TPILogger.tl;
	
	private DpaaSystemInfoHelper dpaaSystemInfoHelper = DpaaSystemInfoHelper.getInstance();
	
	public DpaaAlertDetectorJobSystemBasic(TsmpDpApptJob tsmpDpApptJob, ObjectMapper objectMapper,
			ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao,
			DpaaAlertNotifierRoleEmail dpaaAlertNotifierRoleEmail, DpaaAlertNotifierLine dpaaAlertNotifierLine,
			DpaaAlertNotifierCustom dpaaAlertNotifierCustom, DpaaAlertDispatcherJobHelper dpaaAlertDispatcherJobHelper,
			TsmpAlertDao tsmpAlertDao) throws Exception {
		super(tsmpDpApptJob, objectMapper, DpaaAlertDetectorJobSystemBasicParams.class, apptJobDispatcher,
				tsmpDpApptJobDao, dpaaAlertNotifierRoleEmail, dpaaAlertNotifierLine, dpaaAlertNotifierCustom,
				dpaaAlertDispatcherJobHelper, tsmpAlertDao);
	}

	@Override
	protected DpaaAlertDetectResult detect(DpaaAlertDetectorJobSystemBasicParams params, Date currentDetectSDt) {
		TsmpAlert cri = params.getTsmpAlert();

		// 執行偵測
		DpaaAlertDetectResult detectResult = checkAlertRule(params);
		
		// 本次偵測結果
		final boolean isCurrentAlert = detectResult.isAlert();
		
		// 等同於檢查 duration
		int duration = cri.getDuration();
		boolean isOnDuration = isOnDuration(isCurrentAlert, params, duration, currentDetectSDt);
		if (!isOnDuration) {
			detectResult.setAlert(false);
			boolean hasNoRecentRecord = hasNoRecentRecord(params);
			setStackTrace(String.format( //
				"須持續發生%d秒才會告警, 本次偵測結果為 %s, %s, 告警編號=%d", //
				duration, isCurrentAlert, //
				(hasNoRecentRecord ? "最近無偵測紀錄" : ("最近的紀錄在 " + params.getRecentDetectDt() + ", 結果為 " + params.isRecentAlert())), //
				cri.getAlertId()
			), getLineNumber());
		}

		// 紀錄偵測結果
		saveAlertRecord(isCurrentAlert, currentDetectSDt, params);
		
		// 檢查 alert interval
		long elapsedTime = getElapsedTime();
		int alertInterval = cri.getAlertInterval();
		boolean isOnInterval = isOnAlertInterval(elapsedTime, alertInterval);
		if (!isOnInterval) {
			detectResult.setAlert(false);
			setStackTrace(String.format( //
				"忽略告警偵測: 現不在告警間隔上, 告警編號=%d, 間隔=(%d/%d)",
				cri.getAlertId(), elapsedTime, alertInterval
			), getLineNumber());
		}
		
		// 如果本次偵測有發出告警通知, 就要押上時間, 以重新計算告警間隔
		if (detectResult.isAlert()) {
			params.setDetectResult(true, currentDetectSDt);
		}

		return detectResult;
	}

	@Override
	protected DpaaAlertEvent buildAlertEvent(DpaaAlertDetectResult detectResult) {
		DpaaAlertEvent dpaaAlertEvent = new DpaaAlertEvent(detectResult);

		TsmpAlert ent = detectResult.getEntity();
		dpaaAlertEvent.setAlertType(ent.getAlertType());
	
		String alertMsg = ent.getAlertMsg();
		if (alertMsg == null) {
			alertMsg = "";
		}
		alertMsg = alertMsg.replace("$THRESHOLD", String.valueOf(ent.getThreshold()));
		alertMsg = alertMsg.replace("$DURATION", String.valueOf(ent.getDuration()));
		alertMsg = alertMsg.replace("$INTERVAL", String.valueOf(ent.getAlertInterval()));
		ent.setAlertMsg(alertMsg);

		Map<String, Object> payload = dpaaAlertEvent.getPayload();
		if (payload == null) {
			payload = new HashMap<>();
		}
		payload.put("message", alertMsg);
		dpaaAlertEvent.setPayload(payload);
		return dpaaAlertEvent;
	}

	// 這裡不能用 params 儲存告警結果
	@SuppressWarnings("unchecked")
	private DpaaAlertDetectResult checkAlertRule(DpaaAlertDetectorJobSystemBasicParams params) {
		params.addToPayload("Check Time", LocalDateTime.now().toString());
		
		DpaaSystemInfo dpaaSystemInfo = new DpaaSystemInfo();
		TsmpAlert entity = params.getTsmpAlert();
		String alertType = entity.getAlertType();
		Integer threshold = entity.getThreshold();
		boolean isAlert = false;
		switch (alertType) {
			case DpaaAlertType.CPU:
				this.dpaaSystemInfoHelper.setCpuUsedRateAndMem(dpaaSystemInfo);
				Float cpu = dpaaSystemInfo.getCpu();	// ex: 0.0123
				cpu *= 100F;	// ex: 1.23%
				if (cpu > threshold) {
					isAlert = true;
				}
				break;
			case DpaaAlertType.Disk:
				this.dpaaSystemInfoHelper.setDiskInfo(dpaaSystemInfo);
				double loading = dpaaSystemInfo.getDusage();	// ex: 0.4321
				loading *= 100D;	// ex: 43.21%
				if (loading > threshold) {
					isAlert = true;
				}
				break;
			case DpaaAlertType.Heap:
				this.dpaaSystemInfoHelper.setRuntimeInfo(dpaaSystemInfo);
				float heap_loading = (dpaaSystemInfo.getHused().floatValue() / dpaaSystemInfo.getHtotal().floatValue()) * 100;
				if (heap_loading > threshold) {
					isAlert = true;
				}
				break;
		}

		DpaaAlertDetectResult detectResult = new DpaaAlertDetectResult();
		detectResult.setAlert(isAlert);
		detectResult.setEntity(deepCopy(entity, TsmpAlert.class));
		detectResult.setPayload(deepCopy(params.getAlertPayload(), Map.class));
		return detectResult;
	}

	// 沒有紀錄, 或是本次偵測結果與上次紀錄結果相異, 才紀錄本次偵測結果與時間
	private void saveAlertRecord(boolean isAlert, Date detectDt, DpaaAlertDetectorJobSystemBasicParams params) {
		boolean noRecentRecord = hasNoRecentRecord(params);
		if (
			noRecentRecord ||	// 沒有紀錄
			(isAlert ^ params.isRecentAlert().booleanValue())	// 本次偵測結果與上次紀錄結果相異
		) {
			params.setDetectResult(isAlert, detectDt);
			this.logger.trace(String.format("儲存告警偵測結果: [%d]-%b", params.getTsmpAlert().getAlertId(), isAlert));
		}
	}

	private boolean hasNoRecentRecord(DpaaAlertDetectorJobSystemBasicParams params) {
		Boolean hasNoRecentAlert = Objects.isNull(params.isRecentAlert());
		Boolean hasNoRecentDt = !StringUtils.hasLength(params.getRecentDetectDt());
		return (hasNoRecentAlert || hasNoRecentDt);
	}

	private boolean isOnAlertInterval(long elapsedTime, int interval) {
		if (elapsedTime % interval == 0) {
			return true;
		}
		return false;
	}

	// 本次偵測結果為true, 且最近無相異的偵測結果(也是true), 且距離上次異動的紀錄時間 >= duration
	private boolean isOnDuration(boolean isAlert, DpaaAlertDetectorJobSystemBasicParams params, int duration, Date currentDetectSDt) {
		// 最近無偵測紀錄就不發告警通知
		boolean noRecentRecord = hasNoRecentRecord(params);
		if (noRecentRecord) {
			return false;
		}
		
		boolean recentAlert = params.isRecentAlert();
		long diffInSecs = calDurationBetween(params.getRecentDetectDate().orElse(null), currentDetectSDt);
		return (isAlert && recentAlert && (diffInSecs >= duration));
	}

	private long calDurationBetween(Date ds, Date de) {
		long diffInMillis = Math.abs(de.getTime() - ds.getTime());
		long diffInSecs = TimeUnit.SECONDS.convert(diffInMillis, TimeUnit.MILLISECONDS);
		return diffInSecs;
	}


}
