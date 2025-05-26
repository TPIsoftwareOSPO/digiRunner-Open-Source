package tpi.dgrv4.dpaa.component.apptJob;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.dpaa.component.alert.*;
import tpi.dgrv4.dpaa.constant.DpaaAlertDetectorJobCommand;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.Function;

import static tpi.dgrv4.common.utils.StackTraceUtil.getLineNumber;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

@SuppressWarnings("serial")
public abstract class DpaaAlertDetectorJob<P extends DpaaAlertDetectorJobParams> extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private DpaaAlertNotifierRoleEmail dpaaAlertNotifierRoleEmail;
	private DpaaAlertNotifierLine dpaaAlertNotifierLine;
	private DpaaAlertNotifierCustom dpaaAlertNotifierCustom;
	private DpaaAlertDispatcherJobHelper dpaaAlertDispatcherJobHelper;
	private TsmpAlertDao tsmpAlertDao;

	private ObjectMapper objectMapper;

	// 本次偵測開始時間
	private final Date currentDetectSDt;

	private Class<P> paramsType;

	private P params;

	@Autowired
	public DpaaAlertDetectorJob(TsmpDpApptJob tsmpDpApptJob, ObjectMapper objectMapper, Class<P> clazz
			, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao, 
			DpaaAlertNotifierRoleEmail dpaaAlertNotifierRoleEmail, DpaaAlertNotifierLine dpaaAlertNotifierLine,
			DpaaAlertNotifierCustom dpaaAlertNotifierCustom, DpaaAlertDispatcherJobHelper dpaaAlertDispatcherJobHelper,
			TsmpAlertDao tsmpAlertDao) //
		throws Exception {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.objectMapper = objectMapper;	// Couldn't use auto-wired bean in constructor
		this.currentDetectSDt = new Date();
		this.paramsType = clazz;
		this.params = parseInParams();
		this.dpaaAlertNotifierRoleEmail = dpaaAlertNotifierRoleEmail;
		this.dpaaAlertNotifierLine = dpaaAlertNotifierLine;
		this.dpaaAlertNotifierCustom = dpaaAlertNotifierCustom;
		this.dpaaAlertDispatcherJobHelper = dpaaAlertDispatcherJobHelper;
		this.tsmpAlertDao = tsmpAlertDao;
	}

	private P parseInParams() throws Exception {
		String json = getTsmpDpApptJob().getInParams();
		P params = getObjectMapper().readValue(json, this.paramsType);
		if (params == null) {
			throw new IllegalArgumentException("\"in_params\" must be in json format.");
		}
		TsmpAlert tsmpAlert = params.getTsmpAlert();
		if (tsmpAlert == null || tsmpAlert.getAlertId() == null) {
			throw new IllegalArgumentException("\"tsmpAlert\" and \"alertId\" in it are both required.");
		}
		String startDtStr = params.getStartDt();
		if (!StringUtils.hasLength(startDtStr)) {
			startDtStr = params.saveStartDt(this.currentDetectSDt);
		}
		return params;
	}

	@Override
	public String runApptJob() throws Exception {
		checkDetectionEnabled();
		
		TsmpAlert cri = this.params.getTsmpAlert();
		
		if (!cri.getAlertEnabled()) {
			throw new IllegalStateException("'alertEnabled' in [Params] is false. "
				+ "Please enable from Alert Setting.");
		}

		checkManuallyExecute(cri);

		// 執行關聯檔內的指令
		executeCommand();

		// 判斷現在是否在例外時間(不執行告警)
		String exType = cri.getExType();
		String exDaysStr = cri.getExDays();
		String exTimeStr = cri.getExTime();
		boolean isIgnore = isIgnoreExecution(exType, exDaysStr, exTimeStr);
		if (isIgnore) {
			setStackTrace(String.format("忽略告警偵測: 現值例外期間, 告警編號=%d, 例外設定=([%s]-[%s]-[%s])", //
				cri.getAlertId(), exType, nvl(exDaysStr), nvl(exTimeStr)
			), getLineNumber());
        } else {
        	// 執行偵測
        	DpaaAlertDetectResult result = detect(this.params, this.currentDetectSDt);
        	if (result != null && result.isAlert()) {
        		DpaaAlertEvent event = buildAlertEvent(result);
        		sendAlertEvent(event);
        		setStackTrace(String.format("發送告警於監測開始後第%d秒, 告警編號=%d, 告警類型=%s", //
    				getElapsedTime(), cri.getAlertId(), cri.getAlertType()
    			), getLineNumber());
        	}
        }
		
		// 儲存參數
		saveInParams();
		
		return "SUCCESS";
	}

	/**
	 * 1. Relation file which builds relationship between Alert and ApptJob
	 *    (See {@link DpaaAlertDispatcherJobHelper#createRelationFile})
	 */
	@Override
	public List<String> provideReplicableFileNames() {
		Long alertId = this.params.getTsmpAlert().getAlertId();
		String relationFileName = getDpaaAlertDispatcherJobHelper().buildRelationFileName(alertId);
		return Arrays.asList(new String[] {
			relationFileName
		});
	}

	private void checkDetectionEnabled() throws Exception {
		boolean isAlertDetectionEnabled = getDpaaAlertDispatcherJobHelper().isAlertDetectionEnabled();
		if (!isAlertDetectionEnabled) {
			String msg = "Alert detection is disabled. Please set value of TSMP_SETTING.TSMP_DPAA_RUNLOOP_INTERVAL greater than 0.";
			this.logger.error(msg);
			throw new Exception(msg);
		}
	}

	private void checkManuallyExecute(TsmpAlert cri) {
		// 不允許手動[執行]或[重做](呼叫 DPB0059)此排程
		String identifData = getTsmpDpApptJob().getIdentifData();
		if (!(StringUtils.hasLength(identifData) && identifData.startsWith("apptJobId="))) {
			return;
		}
		
		// 以下檢核是為了避免: 因應用中斷而狀態停留在'R'的工作, 會因無法[重做]也無法透過刷新來重啟偵測
		IllegalStateException e = new IllegalStateException("This job should be launched through Alert Setting. "
			+ "If alert is already enabled, please try to search or update to trigger "
			+ "a synchronization between TsmpAlert and TsmpDpApptjob.");
		
		// 來源排程的狀態仍在[執行中]
		Long fromJobId = getTsmpDpApptJob().getFromJobId();
		Optional<TsmpDpApptJob> opt_fromJob = getTsmpDpApptJobDao().findById(fromJobId);
		TsmpDpApptJob fromJob = opt_fromJob.orElseThrow(() -> e);
		if (!TsmpDpApptJobStatus.RUNNING.isValueEquals(fromJob.getStatus())) {
			throw e;
		}
		
		// 若對應的告警項目仍為[停用], 則要啟用後才繼續執行排程
		Long alertId = cri.getAlertId();
		TsmpAlert source = getTsmpAlertDao().findById(alertId).orElseThrow(() -> e);
		boolean isAlertEnabled = source.getAlertEnabled();
		if (!isAlertEnabled) {
			// [啟用]對應的告警項目, 再交給 executeCommand 去刷新此排程中的 in_params
			source.setAlertEnabled(true);
			source.setUpdateTime(this.currentDetectSDt);
			source.setUpdateUser(getTsmpDpApptJob().getCreateUser());
			source = getTsmpAlertDao().save(source);
			
			// 押 UPDATE 到關聯檔, 等 executeCommand() 讀出時自動同步 entity 到 in_params
			Long apptJobId = getTsmpDpApptJob().getApptJobId();
			DpaaAlertDetectorJobCommand command = DpaaAlertDetectorJobCommand.UPDATE;
			getDpaaAlertDispatcherJobHelper().updateRelationFileByApptJobIdAndFileName(apptJobId, alertId, command, true);

			// 需重設偵測起始時間, 應與對應的告警項目[啟用]的時間相同
			this.params.saveStartDt(this.currentDetectSDt);
		}
		
		// 避免再次循環時, 又掉入此檢核
		getTsmpDpApptJob().setIdentifData("alertId=" + alertId + ",　" + identifData);
		
		// 取消舊的[執行中]排程
		fromJob.setStatus(TsmpDpApptJobStatus.CANCEL.value());
		fromJob.setUpdateDateTime(this.currentDetectSDt);
		fromJob.setUpdateUser(getTsmpDpApptJob().getCreateUser());
		getTsmpDpApptJobDao().save(fromJob);
	}

	private boolean isIgnoreExecution(String exType, String exDaysStr, String exTimeStr) {
		Calendar cal = Calendar.getInstance();
		// 沒有設定例外時間就代表一律執行告警偵測
		if (!StringUtils.hasLength(exTimeStr)) {
			return false;
		}
		switch (exType) {
			case "W":
				if (!StringUtils.hasLength(exDaysStr)) {
					return false;
				}
				return isDayOfWeek(cal, exDaysStr, exTimeStr);
			case "M":
				if (!StringUtils.hasLength(exDaysStr)) {
					return false;
				}
				return isDayOfMonth(cal, exDaysStr, exTimeStr);
			case "D":
				exDaysStr = "1_2_3_4_5_6_7_8_9_10_11_12_13_14_15_16_17_18_19_20_21_22_23_24_25_26_27_28_29_30_31";
				return isDayOfMonth(cal, exDaysStr, exTimeStr);
			default:
				return false;
		}
	}

	private boolean isDayOfWeek(Calendar calendar, String exDaysStr, String exTimeStr) {
		int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		day = day == 0 ? 7 : day;
		return checkDay(exDaysStr, String.valueOf(day), exTimeStr, 7);
	}

	private boolean isDayOfMonth(Calendar calendar, String exDaysStr, String exTimeStr) {
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return checkDay(exDaysStr, String.valueOf(day), exTimeStr, 31);
	}

	/**
	 * 須包含跨日判斷, ex:<br>
	 * 例外類型設為"每月", 例外日期設為"1_2", 且例外時間設為"2300-0200"<br>
	 * 代表每個月1、2號的晚上11點到隔天的凌晨2點不須告警
	 * @param exDaysStr
	 * @param day
	 * @param exTimeStr
	 * @param int_vaild
	 * @return
	 */
	private boolean checkDay(String exDaysStr, String day, String exTimeStr, int int_vaild) {
		String[] exDays = splitDays(exDaysStr);
		if (null == exDays || (exDays.length < 1)) {
			return false;
		}

		// 檢查本日是否為例外日期
		for (int i = 0; i < exDays.length; i++) {
			if (String.valueOf(day).equals(exDays[i])) {
				// 檢查現在是否為例外時間
				boolean isIgnore = isInExceptTime("today", exTimeStr);
				if (isIgnore) {
					return true;
				}
			}
		}

		List<Integer> intdays = new ArrayList<>();
		// 本日如果沒有，製作檢查明日的陣列
		for (int i = 0; i < exDays.length; i++) {
			int day_int = Integer.valueOf(exDays[i]);
			int add_day = day_int + 1;
			if (add_day > int_vaild) { // int_valid 如果是月份，就是31，如果是星期，那就是7
				add_day = 1;
			}
			intdays.add(add_day);
		}

		for (int i = 0; i < intdays.size(); i++) {
			String com_string = String.valueOf(intdays.get(i));
			if (String.valueOf(day).equals(com_string)) {
				boolean isExpTime = isInExceptTime("tomorrow", exTimeStr);
				if (isExpTime) {
					return true;
				}
			}
		}

		return false;
	}

	private String[] splitDays(String exDays) {
		return exDays == null ? null : exDays.split("_");
	}

	private boolean isInExceptTime(String todayOrTomorrow, String exTimeStr) {
		// 如果是空值，不是例外時間
		if (!StringUtils.hasLength(exTimeStr)) {
			return false;
		}

		// 有值，檢查時間; false 不是例外時間
		boolean isIgnore = false;
		try {
			// 目前時間
			LocalTime localtime_now = LocalTime.now();
			DateTimeFormatter formater = DateTimeFormatter.ofPattern("HH:mm");
			DateTimeFormatter formater_ss = DateTimeFormatter.ofPattern("HH:mm:ss");
			DateTimeFormatter formater_ds = DateTimeFormatter.ofPattern("HHmm");
			LocalTime time_0000 = LocalTime.parse("00:00", formater);
			LocalTime time_235959 = LocalTime.parse("23:59:59", formater_ss);

			String[] spit = splitTime(exTimeStr);
			if (spit != null) {
				String ex_start = spit[0];
				String ex_end = spit[1];

				// 檢查時間的開始與結束
				LocalTime localtime_start = LocalTime.parse(ex_start, formater_ds);
				LocalTime localtime_end = LocalTime.parse(ex_end, formater_ds);

				// 如果時間相同,24小時例外,交由日期決定是不是例外時間
				if ("today".equals(todayOrTomorrow)) {
					if (isCrossToday(exTimeStr)) {
						if (localtime_now.isAfter(localtime_start)) {
							if (localtime_now.isBefore(time_235959)) {
								isIgnore = true;
							}
						}
					} else {
						if (localtime_now.isAfter(localtime_start)) {
							if (localtime_now.isBefore(localtime_end)) {
								isIgnore = true;
							}
						}
					}
				} else {
					if (isCrossToday(exTimeStr)) {
						if (localtime_now.isAfter(time_0000)) {
							if (localtime_now.isBefore(localtime_end)) {
								isIgnore = true;
							}
						}
					} else {
						return false;
					}
				}
			}
		} catch (DateTimeException e) {
			this.logger.error("DateTimeException! Please check DpaaAlertDetectorJob isInExceptTime");
			throw TsmpDpAaRtnCode.SYSTEM_ERROR.throwing();
		}

		return isIgnore;
	}

	private String[] splitTime(String exTime) {
		return exTime != null && exTime.length() > 0 ? exTime.split("-") : null;
	}

	/**
	 * 判斷所設定的時間區間是否跨日。<br>
	 * 如: 2300-0200, 或 1000-1000 皆為跨日
	 * @param exTime: HHmm-HHmm
	 * @return
	 */
	private boolean isCrossToday(String exTime) {
		DateTimeFormatter formater_ds = DateTimeFormatter.ofPattern("HHmm");
		// 檢查時間的開始與結束
		String[] spit = splitTime(exTime);
		if (spit != null) {
			String ex_start = spit[0];
			String ex_end = spit[1];
			// 如果結束時間是23:59,不算跨日
			if ("2359".equals(ex_end)) {
				return false;
			}
			// 如果時間相同,算跨日
			if (ex_start.equals(ex_end)) {
				return true;
			}
			LocalTime localtime_start = LocalTime.parse(ex_start, formater_ds);
			LocalTime localtime_end = LocalTime.parse(ex_end, formater_ds);
			if (localtime_start.isAfter(localtime_end)) {
				return true; // 跨日
			}
		}
		return false;
	}

	private void sendAlertEvent(DpaaAlertEvent dpaaAlertEvent) {
		List<DpaaAlertNotifier> allNotifiers = Arrays.asList(new DpaaAlertNotifier[] {
			getDpaaAlertNotifierRoleEmail(), getDpaaAlertNotifierLine(), getDpaaAlertNotifierCustom()
		});
		List<DpaaAlertNotifier> notifiers = DpaaAlertNotifierFactory.findNotifiers(dpaaAlertEvent, allNotifiers);
		if (!CollectionUtils.isEmpty(notifiers)) {
			notifiers.forEach((notifier) -> notifier.notice(dpaaAlertEvent));
		}
	}

	private void saveInParams() throws Exception {
		String json = getObjectMapper().writerWithDefaultPrettyPrinter() //
			.writeValueAsString(this.params);
		getTsmpDpApptJob().setInParams(json);
	}

	/**
	 * 回傳監控程序開始後所歷經的秒數(去除毫秒)
	 * ex: 00:00:00.999 ~ 00:00:01.001 應該算是相差 1 秒
	 * 否則會因為排程器執行工作時有毫秒的誤差, 導致未滿 1 秒而忽略執行
	 * @return
	 */
	protected long getElapsedTime() {
		Function<Date, Date> truncFunc = (input) -> {
			Calendar c = Calendar.getInstance();
			c.setTime(input);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		};
		
		Date startDt = this.params.getStartDate().orElse(null);
		startDt = truncFunc.apply(startDt);
		Date endDt = truncFunc.apply(this.currentDetectSDt);
		long elaspedTime = (endDt.getTime() - startDt.getTime()) / 1000;
		return elaspedTime;
	}

	protected void setStackTrace(String stackTrace, String lineNumber) {
		getTsmpDpApptJob().setStackTrace(stackTrace);
		this.logger.trace(String.format("[%s] %s", lineNumber, stackTrace));
	}

	protected void executeCommand() throws Exception {
		Long apptJobId = getTsmpDpApptJob().getApptJobId();
		Long alertId = this.params.getTsmpAlert().getAlertId();
		DpaaAlertDetectorJobCommand command = getDpaaAlertDispatcherJobHelper() // 
			.readCommandFromRelationFileAndCreateIfNotExists(apptJobId, alertId);
		if (command == null) {
			return;
		}

		switch(command) {
			case DELETE:
				// Update before delete. Because 'in_params' should be consistent with TsmpAlert event if
				// this apptJob is canceled.
				updateParams(alertId);
				// TsmpAlert.alertEnabled field in 'in_params' should be 'false', which is the only field
				// that can be inconsistent with the source TsmpAlert.
				this.params.getTsmpAlert().setAlertEnabled(false);
				saveInParams();
				this.logger.debug(String.format("AlertId %d is deleted or disabled / ApptJobId %d will be canceled", alertId, apptJobId));
				// This is the only way to interrupt a RunLoopJob without an ApptJob error.
				throw new CancellationException();
			case UPDATE:
				updateParams(alertId);
				// 更新完要清除指令
				getDpaaAlertDispatcherJobHelper().clearRelationFile(apptJobId, alertId);
				this.logger.debug(String.format("ApptJobId %d{} of TsmpAlert %d is updated", apptJobId, alertId));
				break;
			default:
				break;
		}
	}

	private void updateParams(Long alertId) {
		Optional<TsmpAlert> opt = getTsmpAlertDao().findById(alertId);
		if (!opt.isPresent()) {
			this.logger.error(String.format("Missing TsmpAlert with id: %d", alertId));
			return;
		}
		// 重抓 TSMP_ALERT 的值
		TsmpAlert newAlert = ServiceUtil.deepCopy(opt.get(), TsmpAlert.class);
		this.params.setTsmpAlert(newAlert);
	}

	protected DpaaAlertNotifierRoleEmail getDpaaAlertNotifierRoleEmail() {
		return this.dpaaAlertNotifierRoleEmail;
	}

	protected DpaaAlertNotifierLine getDpaaAlertNotifierLine() {
		return this.dpaaAlertNotifierLine;
	}

	protected DpaaAlertNotifierCustom getDpaaAlertNotifierCustom() {
		return this.dpaaAlertNotifierCustom;
	}

	protected DpaaAlertDispatcherJobHelper getDpaaAlertDispatcherJobHelper() {
		return this.dpaaAlertDispatcherJobHelper;
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return this.tsmpAlertDao;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected abstract DpaaAlertDetectResult detect(P params, Date currentDetectSDt);

	protected abstract DpaaAlertEvent buildAlertEvent(DpaaAlertDetectResult detectResult);

}
