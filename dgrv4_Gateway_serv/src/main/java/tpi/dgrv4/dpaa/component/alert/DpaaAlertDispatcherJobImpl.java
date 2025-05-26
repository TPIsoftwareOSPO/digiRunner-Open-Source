package tpi.dgrv4.dpaa.component.alert;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.ApptJobEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.apptJob.DpaaAlertDetectorJobParams;
import tpi.dgrv4.dpaa.constant.DpaaAlertDetectorJobCommand;
import tpi.dgrv4.dpaa.constant.DpaaAlertType;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
@Primary
public class DpaaAlertDispatcherJobImpl implements DpaaAlertDispatcherIfs {

	private TPILogger logger = TPILogger.tl;

	private final ServiceConfig serviceConfig;
	private final TsmpAlertDao tsmpAlertDao;
	private final DpaaAlertDispatcherJobHelper helper;
	private final ObjectMapper objectMapper;
	private final ApptJobDispatcher apptJobDispatcher;
	private final TsmpDpApptJobDao tsmpDpApptJobDao;

	private ScheduledExecutorService executor;
	
	@PostConstruct
	public void init() {
		ThreadFactory threadFactory = new CustomizableThreadFactory("DpaaAlertDispatcherJobImpl");
		this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		
		// 啟動Timer每{period}秒同步TsmpAlert與TsmpDpApptJob
		int period = getSchedulePeriodSecs();
		this.executor.scheduleAtFixedRate(this::syncAlert, 0L, period, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void destroy() {
		if (this.executor != null) {
			this.executor.shutdown();
		}
	}

	@Override
	public void joinAlert(TsmpAlert tsmpAlert) {
		// 加入排程失敗時, 告警也不可新增成功, 因為告警是靠排程在運作
		if (tsmpAlert == null || tsmpAlert.getAlertId() == null) {
			this.logger.error("TsmpAlert entity or it's alertId is null");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		Long alertId = tsmpAlert.getAlertId();
		String alertType = tsmpAlert.getAlertType();
		String refSubitemNo = getRefSubitemNo(alertType);
		List<Long> apptJobIds = findDuplicatedRunLoopJob(alertId, refSubitemNo);
		if (!CollectionUtils.isEmpty(apptJobIds)) {
			this.logger.error(String.format("Duplicated RunLoopJob(s) of AlertId %d: apptJobId(s) = %s", alertId, apptJobIds.toString()));
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		TsmpDpApptJob job = new TsmpDpApptJob();
		job.setRefItemNo(ApptJobEnum.RUNLOOP_ITEM_NO);
		job.setRefSubitemNo(refSubitemNo);
		Boolean isAlertEnabled = tsmpAlert.getAlertEnabled();
		TsmpDpApptJobStatus status = isAlertEnabled ? TsmpDpApptJobStatus.WAIT : TsmpDpApptJobStatus.CANCEL;
		job.setStatus(status.value());
		String inParams = getInParams(tsmpAlert);
		job.setInParams(inParams);
		job.setStartDateTime(DateTimeUtil.now());
		String identifData = getIdentifData(tsmpAlert);
		job.setIdentifData(identifData);
		job.setCreateDateTime(DateTimeUtil.now());
		job.setCreateUser(tsmpAlert.getCreateUser());

		// 如果未啟用就不加入排程佇列
		if (isAlertEnabled) {
			job = getApptJobDispatcher().addAndRefresh(job);
		} else {
			job = getTsmpDpApptJobDao().save(job);
		}

		// 建立告警與排程的關聯檔案
		TsmpDpFile relationFile = getHelper().createRelationFile(job.getCreateUser(), //
			alertId, job.getApptJobId(), null);

		this.logger.trace(String.format("RunLoopJob of alertId %d is created successfully: %d (Relation file id: %d)", //
				alertId, job.getApptJobId(), relationFile.getFileId()));
	}

	@Override
	public void separateAlert(Long alertId) {
		if (alertId == null) {
			this.logger.error("TsmpAlert alertId is null");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		DpaaAlertDetectorJobCommand command = DpaaAlertDetectorJobCommand.DELETE;
		List<Long> modifiedApptJobIds = getHelper().updateRelationFileByAlertId(alertId, command, false);

		if (CollectionUtils.isEmpty(modifiedApptJobIds)) {
			this.logger.debugDelay2sec(String.format("No any RunLoopJob related to alertId %d is marked as '%s'.", alertId, command.toString())); 
		} else {
			this.logger.debugDelay2sec(String.format("RunLoopJob(s) related to alertId %d are marked as '%s': %s", //
				alertId, command.toString(), modifiedApptJobIds.toString()));
		}
	}

	@Override
	public void updateAlert(TsmpAlert tsmpAlert) {
		if (tsmpAlert == null || tsmpAlert.getAlertId() == null) {
			this.logger.error("TsmpAlert entity or it's alertId is null");
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		Long alertId = tsmpAlert.getAlertId();
		boolean isEnabled = tsmpAlert.getAlertEnabled();
		// 啟用
		if (isEnabled) {
			// Find apptJobs by relation file name and apptJob status
			String alertType = tsmpAlert.getAlertType();
			String refSubitemNo = getRefSubitemNo(alertType);
			List<TsmpDpApptJob> relatedApptJobs = getHelper().findRelatedApptJob(refSubitemNo, alertId);
			// 新增排程
			if (CollectionUtils.isEmpty(relatedApptJobs)) {
				joinAlert(tsmpAlert);
				// 更新
			} else {
				final DpaaAlertDetectorJobCommand command = DpaaAlertDetectorJobCommand.UPDATE;
				List<Long> updatedApptJobIds = relatedApptJobs.stream() //
				.map((relatedApptJob) -> {
					Long relatedApptJobId = relatedApptJob.getApptJobId();
					getHelper().updateRelationFileByApptJobIdAndFileName( //
						relatedApptJobId, alertId, command, true);
					return relatedApptJobId;
				}) //
				.collect(Collectors.toList());
				this.logger.trace(String.format("RunLoopJob(s) related to alertId %d are marked as '%s': %s", //
					alertId, command.toString(), updatedApptJobIds.toString()));
			}
			// 停用
		} else {
			separateAlert(alertId);
		}
	}

	@Override
	public void syncAlert() {
		this.logger.trace("Synchronizing TsmpAlert and TsmpDpApptJob...");
		
		List<TsmpAlert> allAlerts = getTsmpAlertDao().findAll();
		int syncCnt = 0;
		for (TsmpAlert tsmpAlert : allAlerts) {
			try {
				updateAlert(tsmpAlert);
				syncCnt++;
			} catch (Exception e) {
				this.logger.error(String.format("Failed to sync alertId: %d\n%s", tsmpAlert.getAlertId(), //
					StackTraceUtil.logStackTrace(e)));
			}
		}
		
		getApptJobDispatcher().resetRefreshSchedule();
			
		this.logger.trace(String.format("Synchronizing is done. %d TsmpAlert(s) are updated.", syncCnt));
	}

	private String getRefSubitemNo(String alertType) {
		if (DpaaAlertType.isSystemBasicType(alertType)) {
			return "ALERT_SYSTEM_BASIC";
		} else if (DpaaAlertType.isKeywordType(alertType)) {
			return "ALERT_KEYWORD";
		}
		this.logger.error("Unknown alert type: " + alertType);
		throw TsmpDpAaRtnCode._1290.throwing();
	}

	private	List<Long> findDuplicatedRunLoopJob(Long alertId, String refSubitemNo) {
		// 找出哪些狀態為 W 或 R 的 RunLoop 排程, 有相同 alertId 的關聯檔
		// 狀態非 W 或 R 的排程, 可能是因跨日或手動執行而產生的 RunLoop
		List<TsmpDpApptJob> existingApptJobs = getHelper().findRelatedApptJob(refSubitemNo, alertId);
		return existingApptJobs.stream() //
		.map(TsmpDpApptJob::getApptJobId) //
		.collect(Collectors.toList());
	}

	private String getInParams(TsmpAlert tsmpAlert) {
		DpaaAlertDetectorJobParams params = new DpaaAlertDetectorJobParams();
		params.setTsmpAlert(tsmpAlert);
		try {
			String inParams = getObjectMapper().writerWithDefaultPrettyPrinter() //
				.writeValueAsString(params);
			return inParams;
		} catch (Exception e) {
			this.logger.error(String.format("Parse DpaaAlertDetectorJobParams to JSON error!%s", StackTraceUtil.logStackTrace(e)));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private String getIdentifData(TsmpAlert tsmpAlert) {
		Long alertId = tsmpAlert.getAlertId();
		String identifData = String.format("alertId=%d", alertId);
		return identifData;
	}

	private int getSchedulePeriodSecs() {
		int periodSec = 1800;
		String val = getServiceConfig().get("job-dispatcher.period.ms");
		try {
			periodSec = Integer.valueOf(val);
			periodSec = periodSec / 1000;
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid period: %s, use default %d.", val, periodSec));
		}
		return periodSec;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return this.tsmpAlertDao;
	}

	protected DpaaAlertDispatcherJobHelper getHelper() {
		return this.helper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

}
