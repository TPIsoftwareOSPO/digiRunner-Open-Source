package tpi.dgrv4.gateway.component.job.appt;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpRjobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class ApptRjobDispatcher implements Runnable {

	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private ApptJobDispatcher apptJobDispatcher;

	// 是否啟用週期排程器
	private Boolean isSchedulerEnabled;

	// 每隔多久同步一次資料庫
	private Long period;

	private ScheduledExecutorService syncExecutor;

	// 同步資料庫的排程
	private ScheduledFuture<?> syncFuture;

	private Object syncLock;
	
	// 查無週期排程UID
	// Check for periodic schedule UID
	private final static String CHECK_FOR_PERIODIC_SCHEDULE_UID = "Check for periodic schedule UID: ";
	
	// 工作已被異動
	// Job has been moved
	private final static String JOB_HAS_BEEN_MOVED = "Job has been moved: ";
	
	@Autowired
	public ApptRjobDispatcher(TsmpDpApptJobDao tsmpDpApptJobDao, TsmpDpApptRjobDao tsmpDpApptRjobDao,
			TsmpDpApptRjobDDao tsmpDpApptRjobDDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ServiceConfig serviceConfig, JobHelper jobHelper, ApplicationContext ctx) {
		super();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
	}
	
	/*
	 * Because using constructor injection will cause a circular dependency, use method injection instead
	 */
	@Autowired
	public void setApptJobDispatcher(ApptJobDispatcher apptJobDispatcher) {
		this.apptJobDispatcher = apptJobDispatcher;
	}

	@PostConstruct
	public void init() {
		initAttributes();
		
		resetSyncExecutor();
	}

	public void initAttributes() {
		this.isSchedulerEnabled = getValue("scheduler.appt-rjob.enable", (val) -> {
			return Boolean.valueOf(val);
		}, false);
		this.period = getValue("job-dispatcher.period.ms", (val) -> {
			return Long.valueOf(val);
		}, 1800000L);
		this.syncLock = new Object();
	}

	public void resetSyncExecutor() {
		if (!this.isSchedulerEnabled) {
			TPILogger.tl.info("未啟用週期排程器, 請設定 service.scheduler.appt-rjob.enable=true");
			return;
		}

		synchronized (this.syncLock) {
			try {
				TPILogger.tl.trace("正在重啟同步排程...");
				if (getSyncFuture() != null) {
					getSyncFuture().cancel(false);
				}
				this.syncFuture = getSyncExecutor().scheduleAtFixedRate(this, 0L, this.period, TimeUnit.MILLISECONDS);
				TPILogger.tl.trace("重啟完成");
			} finally {
				this.syncLock.notifyAll();
			}
		}
	}

	@Override
	public void run() {
		synchronized (this.syncLock) {
			try {
				List<TsmpDpApptRjob> rjobList = getTsmpDpApptRjobDao().findAll();
				TPILogger.tl.trace("同步中，共 " + rjobList.size() + " 筆週期設定");
				if (rjobList != null && !rjobList.isEmpty()) {
					for (TsmpDpApptRjob rjob : rjobList) {
						dispatch(rjob);
					}
				}
				TPILogger.tl.trace("同步完成");
			} finally {
				this.syncLock.notifyAll();
			}
		}
	}

	/**
	 * 依照 Rjob 的狀態決定如何操作 ApptJob 的資料
	 * @param rjob
	 */
	protected void dispatch(TsmpDpApptRjob rjob) {
		String apptRjobId = rjob.getApptRjobId();
		Long nextDateTime = rjob.getNextDateTime();
		String status = rjob.getStatus();
		switch(status) {
			// 停用
			case "0":
				TPILogger.tl.trace(apptRjobId + " 已停用, 取消預定在 " + toDtStr(nextDateTime) + " 執行的工作");//1
				cancelApptJobs(rjob.getApptRjobId(), rjob.getNextDateTime());
				break;
			// 啟動
			case "1":
				TPILogger.tl.trace(apptRjobId + " 已啟動, 準備排入工作");//1
				dispatch_active(rjob);
				break;
			// 暫停
			case "2":
				TPILogger.tl.trace(apptRjobId + " 已暫停, 取消預定在 " + toDtStr(nextDateTime) + " 執行的工作");
				cancelApptJobs(rjob.getApptRjobId(), rjob.getNextDateTime());
				break;
			// 執行中
			case "3":
				TPILogger.tl.trace(apptRjobId + " 正在執行中");
				break;
			default:
				TPILogger.tl.trace(apptRjobId + " 無法同步, 未知的狀態代碼: " + status);
				break;
		}
	}

	/**
	 * 處理狀態為"啟動"的週期設定<br>
	 * 將 nextDateTime 調整至週期上, 並派發 ApptJob 
	 * @param rjob
	 */
	protected void dispatch_active(TsmpDpApptRjob rjob) {
		if (!isRjobValid(rjob)) return;

		String apptRjobId = rjob.getApptRjobId();
		TsmpDpApptRjobD rjobD = getTsmpDpApptRjobDDao().queryNextRjobD(apptRjobId, null);
		if (rjobD == null) {
			TPILogger.tl.info(apptRjobId + " 無法同步, 未設定週期排程內容");
			return;
		}

		Date nextDateTime = new Date(rjob.getNextDateTime());
		Date nextExecutionTime = getAdjustedExecutionTime(nextDateTime, DateTimeUtil.now(), rjob.getCronExpression(), -1);
		if (nextExecutionTime == null) {
			TPILogger.tl.debug(apptRjobId + " 無法同步, 計算下次執行時間失敗: nextDateTime=" + rjob.getNextDateTime() + ", cronExpression=" + rjob.getCronExpression());
			return;
		}
		TPILogger.tl.trace(apptRjobId + " 調整下次執行時間: " + toDtStr(rjob.getNextDateTime()) + " -> " + toDtStr(nextExecutionTime));
		rjob.setLastDateTime(rjob.getNextDateTime());
		rjob.setNextDateTime(nextExecutionTime.getTime());
		rjob.setUpdateDateTime(DateTimeUtil.now());
		rjob.setUpdateUser("SYS");
		rjob = getTsmpDpApptRjobDao().save(rjob);
		
		insertApptJob(rjobD, nextExecutionTime);
	}

	/**
	 * 依照 periodUid 及 periodNexttime 取消 ApptJob，並從 MemList 立即移除工作
	 * @param periodUid
	 * @param periodNexttime
	 * @return
	 */
	protected List<Long> cancelApptJobs(String periodUid, Long periodNexttime) {
		String periodNexttimeStr = toDtStr(periodNexttime);
		List<TsmpDpApptJob> apptJobList = getTsmpDpApptJobDao().findByPeriodUidAndPeriodNexttime(periodUid, periodNexttime);
		if (apptJobList == null || apptJobList.isEmpty()) {
			TPILogger.tl.trace("在 " + periodNexttimeStr + " 沒有預訂的工作(UID=" + periodUid + ")");
			return Collections.emptyList();
		}
		// 把所有下次週期要做的工作都押上取消
		List<Long> cancelList = new ArrayList<>();
		for (TsmpDpApptJob apptJob : apptJobList) {
			apptJob.setStatus(TsmpDpApptJobStatus.CANCEL.value());
			apptJob.setUpdateDateTime(DateTimeUtil.now());
			apptJob.setUpdateUser("SYS");
			apptJob = getTsmpDpApptJobDao().saveAndFlush(apptJob);
			cancelList.add(apptJob.getApptJobId());
		}
		cancelList = getApptJobDispatcher().removeCacheJobsImmediately(cancelList);
		TPILogger.tl.trace("已取消預訂於 " + periodNexttimeStr + " 執行的工作(UID=" + periodUid +"): " + cancelList);
		return cancelList;
	}

	protected TsmpDpApptJob insertApptJob(TsmpDpApptRjobD rjobD, Date startDateTime) {
		return insertApptJob(rjobD, startDateTime, false);
	}

	/**
	 * 必須要設定 RjobD 才可以執行此方法
	 * @param rjobD
	 * @param startDateTime
	 * @param forceInsert true時會不斷遞增nextDateTime直到塞成功為止
	 * @return
	 */
	protected TsmpDpApptJob insertApptJob(TsmpDpApptRjobD rjobD, Date startDateTime, boolean forceInsert) {
		String periodUid = rjobD.getApptRjobId();
		Long periodItemsId = rjobD.getApptRjobDId();
		Long periodNexttime = startDateTime.getTime();
		
		// 先確認有沒有排入相同的週期工作
		TsmpDpApptJob job = null;
		do {
			job = getApptJobDispatcher().getPeriodJob(periodUid, periodItemsId, periodNexttime);
			if (job != null) {
				if (!forceInsert) {
					TPILogger.tl.trace("發現相同的工作項目: " + job.getApptJobId());
					return job;
				} else {
					TPILogger.tl.trace("發現相同的工作項目" + job.getApptJobId() + ", 遞增時間: " + periodNexttime + " -> " + (periodNexttime + 1));
					periodNexttime++;
				}
			} else {
				break;
			}
		} while(job != null);
		
		job = new TsmpDpApptJob();
		job.setRefItemNo(rjobD.getRefItemNo());
		job.setRefSubitemNo(rjobD.getRefSubitemNo());
		job.setInParams(rjobD.getInParams());
		job.setIdentifData(rjobD.getIdentifData());
		job.setStartDateTime(startDateTime);
		job.setPeriodUid(periodUid);
		job.setPeriodItemsId(periodItemsId);
		job.setPeriodNexttime(periodNexttime);
		job.setCreateDateTime(DateTimeUtil.now());
		job.setCreateUser("SYS");
		TPILogger.tl.trace("準備寫入週期排程項目: rjobId= " + job.getPeriodUid() +", rjobDId=" + job.getPeriodItemsId() + ", nextDt="
				+ DateTimeUtil.dateTimeToString(new Date(job.getPeriodNexttime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295))
			    + ", inParams=" + job.getInParams());
		job = getApptJobDispatcher().addAndRefresh(job);
		if (job != null) {
			TPILogger.tl.debug("排程工作已建立: " + job.getApptJobId());
		} else {
			TPILogger.tl.debug("無法寫入排程工作");
		}
		return job;
	}

	/**
	 * 暫停
	 * @param apptRjobId
	 * @param updateUser
	 * @return
	 * @throws DgrException
	 */
	public TsmpDpApptRjob pause(String apptRjobId, String updateUser) throws DgrException {
		if (!StringUtils.hasText(apptRjobId)) {
			TPILogger.tl.error("暫停週期排程失敗, 未傳入週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!opt.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + apptRjobId);
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptRjob ardPause_rjob = opt.get();
		String status = ardPause_rjob.getStatus();
		if (
			TsmpDpRjobStatus.DISABLED.value().equals(status) ||
			TsmpDpRjobStatus.IN_PROGRESS.value().equals(status) ||
			TsmpDpRjobStatus.PAUSE.value().equals(status)
		) {
			TPILogger.tl.error("當前排程狀態不允許暫停: " + status);
			throw DgrRtnCode._1251.throwing();
		}
		ardPause_rjob.setStatus(TsmpDpRjobStatus.PAUSE.value());
		ardPause_rjob.setUpdateDateTime(DateTimeUtil.now());
		ardPause_rjob.setUpdateUser(updateUser);

		try {
			ardPause_rjob = getTsmpDpApptRjobDao().saveAndFlush(ardPause_rjob);
			// 取消預定的工作
			cancelApptJobs(apptRjobId, ardPause_rjob.getNextDateTime());
			return ardPause_rjob;
		} catch (ObjectOptimisticLockingFailureException ardPause_e) {
			TPILogger.tl.info(JOB_HAS_BEEN_MOVED + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception ardPause_e) {
			TPILogger.tl.error("暫停週期排程失敗\n" + StackTraceUtil.logStackTrace(ardPause_e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 啟動
	 * @param apptRjobId
	 * @param updateUser
	 * @return
	 * @throws DgrException
	 */
	public TsmpDpApptRjob activate(String apptRjobId, String updateUser) throws DgrException {
		if (!StringUtils.hasText(apptRjobId)) {
			TPILogger.tl.error("啟動週期排程失敗, 未傳入週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> ardActivate_opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!ardActivate_opt.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + apptRjobId);
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptRjobD ardActivate_rjobD = getTsmpDpApptRjobDDao().queryNextRjobD(apptRjobId, null);
		if (ardActivate_rjobD == null) {
			TPILogger.tl.info("啟動週期排程失敗, 未設定週期排程內容: " + apptRjobId);
			throw DgrRtnCode._1296.throwing();
		}
		TsmpDpApptRjob rjob = ardActivate_opt.get();
		String ardActivate_status = rjob.getStatus();
		if (
			TsmpDpRjobStatus.DISABLED.value().equals(ardActivate_status) ||
			TsmpDpRjobStatus.IN_PROGRESS.value().equals(ardActivate_status)
		) {
			TPILogger.tl.error("當前排程狀態不允許啟動: " + ardActivate_status);
			throw DgrRtnCode._1251.throwing();
		}

		Date baseDateTime = (rjob.getEffDateTime() == null ? DateTimeUtil.now() : new Date(rjob.getEffDateTime()));
		Date nextDateTime = new Date(rjob.getNextDateTime());
		nextDateTime = getAdjustedExecutionTime(nextDateTime, baseDateTime, rjob.getCronExpression(), -1);
		rjob.setNextDateTime(nextDateTime.getTime());
		rjob.setStatus(TsmpDpRjobStatus.ACTIVE.value());
		rjob.setUpdateDateTime(DateTimeUtil.now());
		rjob.setUpdateUser(updateUser);
		try {
			// 檢查下次執行時間是否超過效期, ex: 現在時間10:00, 排程每天中午12:00執行，效期卻是 10:00~11:59，則此排程不能派工
			if (!isOverInvDateTime(rjob, nextDateTime)) {
				TsmpDpApptJob ardActive_apptJob = insertApptJob(ardActivate_rjobD, nextDateTime, true);
				
				// 因為 insertApptJob() 的 forceInsert 如果等於 true, 則 nextDateTime 有可能被更改, 所以要回壓到 Rjob
				rjob.setNextDateTime(ardActive_apptJob.getPeriodNexttime());
			}
		
			// 更新主檔
			rjob = getTsmpDpApptRjobDao().saveAndFlush(rjob);
			return rjob;
		} catch (ObjectOptimisticLockingFailureException ardActive_e) {
			TPILogger.tl.info(JOB_HAS_BEEN_MOVED + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception ardActive_e) {
			TPILogger.tl.error("啟動週期排程失敗\n" + StackTraceUtil.logStackTrace(ardActive_e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 略過
	 * @param apptRjobId
	 * @param updateUser
	 * @return
	 * @throws DgrException
	 */
	public TsmpDpApptRjob ignore(String apptRjobId, String updateUser) throws DgrException {
		if (!StringUtils.hasText(apptRjobId)) {
			TPILogger.tl.error("略過週期排程失敗, 未傳入週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> ardIgnore_opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!ardIgnore_opt.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + apptRjobId);
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptRjobD ardIgnore_rjobD = getTsmpDpApptRjobDDao().queryNextRjobD(apptRjobId, null);
		if (ardIgnore_rjobD == null) {
			TPILogger.tl.info("略過週期排程失敗, 未設定週期排程內容: " + apptRjobId);
			throw DgrRtnCode._1296.throwing();
		}
		TsmpDpApptRjob rjob = ardIgnore_opt.get();
		String status = rjob.getStatus();
		if (
			TsmpDpRjobStatus.DISABLED.value().equals(status) ||
			TsmpDpRjobStatus.IN_PROGRESS.value().equals(status) ||
			TsmpDpRjobStatus.PAUSE.value().equals(status)
		) {
			TPILogger.tl.error("當前排程狀態不允許略過: " + status);
			throw DgrRtnCode._1251.throwing();
		}

		// 取消預定的工作
		cancelApptJobs(apptRjobId, rjob.getNextDateTime());
		
		// 算出略過一次之後的執行時間
		Date nextDateTime = new Date(rjob.getNextDateTime());
		nextDateTime = getAdjustedExecutionTime(nextDateTime, DateTimeUtil.now(), rjob.getCronExpression(), 1);
		
		rjob.setNextDateTime(nextDateTime.getTime());
		rjob.setUpdateDateTime(DateTimeUtil.now());
		rjob.setUpdateUser(updateUser);
		try {
			rjob = getTsmpDpApptRjobDao().saveAndFlush(rjob);
			// 下次執行時間若超過效期就不新增工作
			if (!isOverInvDateTime(rjob, nextDateTime)) {
				insertApptJob(ardIgnore_rjobD, nextDateTime);
			}
			return rjob;
		} catch (ObjectOptimisticLockingFailureException e) {
			TPILogger.tl.info(JOB_HAS_BEEN_MOVED + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception e) {
			TPILogger.tl.error("略過週期排程失敗\n" + StackTraceUtil.logStackTrace(e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 作廢
	 * @param apptRjobId
	 * @param updateUser
	 * @return
	 * @throws DgrException
	 */
	public TsmpDpApptRjob suspend(String apptRjobId, String updateUser) throws DgrException {
		if (!StringUtils.hasText(apptRjobId)) {
			TPILogger.tl.error("作廢週期排程失敗, 未傳入週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!opt.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + apptRjobId);
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptRjob ardSuspend_rjob = opt.get();
		String status = ardSuspend_rjob.getStatus();
		if (
			TsmpDpRjobStatus.DISABLED.value().equals(status) ||
			TsmpDpRjobStatus.IN_PROGRESS.value().equals(status)
		) {
			TPILogger.tl.error("當前排程狀態不允許作廢: " + status);
			throw DgrRtnCode._1251.throwing();
		}
		ardSuspend_rjob.setStatus(TsmpDpRjobStatus.DISABLED.value());
		ardSuspend_rjob.setUpdateDateTime(DateTimeUtil.now());
		ardSuspend_rjob.setUpdateUser(updateUser);

		try {
			ardSuspend_rjob = getTsmpDpApptRjobDao().saveAndFlush(ardSuspend_rjob);
			// 取消預定的工作
			cancelApptJobs(apptRjobId, ardSuspend_rjob.getNextDateTime());
			return ardSuspend_rjob;
		} catch (ObjectOptimisticLockingFailureException ardSuspend_e) {
			TPILogger.tl.info(JOB_HAS_BEEN_MOVED + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception ardSuspend_e) {
			TPILogger.tl.error("作廢週期排程失敗\n" + StackTraceUtil.logStackTrace(ardSuspend_e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 更新
	 * @param rjob
	 * @param updateUser
	 * @return
	 * @throws DgrException
	 */
	public TsmpDpApptRjob update(TsmpDpApptRjob rjob, String updateUser, String locale) throws DgrException {
		TsmpDpApptRjob existingRjob = validateRjob(rjob, locale);
		String status = existingRjob.getStatus();
		if (
			TsmpDpRjobStatus.DISABLED.value().equals(status) ||
			TsmpDpRjobStatus.IN_PROGRESS.value().equals(status)
		) {
			TPILogger.tl.error("當前排程狀態不允許更新: " + status);
			throw DgrRtnCode._1251.throwing();
		}
		String apptRjobId = existingRjob.getApptRjobId();
		TsmpDpApptRjobD rjobD = getTsmpDpApptRjobDDao().queryNextRjobD(apptRjobId, null);
		if (rjobD == null) {
			TPILogger.tl.info("更新週期排程失敗, 未設定週期排程內容: " + apptRjobId);
			throw DgrRtnCode._1296.throwing();
		}
		try {
			// 取消預定的工作
			cancelApptJobs(apptRjobId, existingRjob.getNextDateTime());
			
			// 計算新的執行時間
			Date now = DateTimeUtil.now();
			Date base = (rjob.getEffDateTime() == null ? now : new Date(rjob.getEffDateTime()));
			Date nextDateTime = getAdjustedExecutionTime(now, base, rjob.getCronExpression(), -1);
			rjob.setNextDateTime(nextDateTime.getTime());

			// 如果是暫停或是失效就不發佈新的 Apptjob
			if (!TsmpDpRjobStatus.PAUSE.value().equals(rjob.getStatus()) && !isOverInvDateTime(rjob, nextDateTime)) {
				TsmpDpApptJob ardUpdate_apptJob = insertApptJob(rjobD, nextDateTime, true);
				// 因為 insertApptJob() 的 forceInsert 如果等於 true, 則 nextDateTime 有可能被更改, 所以要回壓到 Rjob
				rjob.setNextDateTime(ardUpdate_apptJob.getPeriodNexttime());
			}
			
			// 更新主檔
			rjob = getTsmpDpApptRjobDao().saveAndFlush(rjob);
			return rjob;
		} catch (ObjectOptimisticLockingFailureException ardUpdate_e) {
			TPILogger.tl.info(JOB_HAS_BEEN_MOVED + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception ardUpdate_e) {
			TPILogger.tl.error("更新週期排程失敗\n" + StackTraceUtil.logStackTrace(ardUpdate_e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 往後推移 Rjob 的下次執行時間一次, 並排入 ApptJob2
	 * @return
	 */
	public TsmpDpApptJob step(String apptRjobId, String updateUser, boolean isSetToInProgress) {
		if (!StringUtils.hasText(apptRjobId)) {
			TPILogger.tl.error("推移週期排程失敗, 未傳入週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> ardStep_opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!ardStep_opt.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + apptRjobId);
			throw DgrRtnCode._1298.throwing();
		}
		TsmpDpApptRjobD ardStep_rjobD = getTsmpDpApptRjobDDao().queryNextRjobD(apptRjobId, null);
		if (ardStep_rjobD == null) {
			TPILogger.tl.info("推移週期排程失敗, 未設定週期排程內容: " + apptRjobId);
			throw DgrRtnCode._1296.throwing();
		}
		TsmpDpApptRjob rjob = ardStep_opt.get();
		String status = rjob.getStatus();
		if (!TsmpDpRjobStatus.ACTIVE.value().equals(status)) {
			TPILogger.tl.error("當前排程狀態不允許推移: " + status);
			throw DgrRtnCode._1251.throwing();
		}
		
		try {
			// 先改週期排程狀態, 避免此節點還在推移時, 其他節點也同時在推移
			if (isSetToInProgress) {
				rjob.setStatus(TsmpDpRjobStatus.IN_PROGRESS.value());
				try {
					rjob = getTsmpDpApptRjobDao().saveAndFlush(rjob);  // JpaSystemException: could not execute statement [The database is read only
				} catch (JpaSystemException e) {
					String err = e.getMessage();
					if (err != null && err.indexOf("The database is read only") != -1) {
						TPILogger.tl.debugDelay2sec("The database is read only");
					}else {
						TPILogger.tl.error("getTsmpDpApptRjobDao().saveAndFlush(rjob)::\n" + StackTraceUtil.logStackTrace(e));
					}
				}
			}

			Date updateDateTime = DateTimeUtil.now();
			Date oldNextDateTime = new Date(rjob.getNextDateTime());
			Date newNextDateTime = getAdjustedExecutionTime(// 
				oldNextDateTime, updateDateTime, rjob.getCronExpression(), -1);
			/*
			 * 如果算出來, 下次執行時間跟原本的一樣, 表示還沒到原本的週期時間就先呼叫此step方法了,
			 * 這可能發生在, 提早按下排程作業的"執行"按鈕時,
			 * 這個情況下允許再推移一次時間
			 */
			if (oldNextDateTime.compareTo(newNextDateTime) == 0) {
				Date duplicatedNextDateTime = newNextDateTime;
				newNextDateTime = getAdjustedExecutionTime(// 
					oldNextDateTime, updateDateTime, rjob.getCronExpression(), 1);
				TPILogger.tl.trace("過早執行推移, 自動重算下次執行時間: " + apptRjobId + "::" + toDtStr(duplicatedNextDateTime) + "->" + toDtStr(newNextDateTime));
			}
			
			try {
				getTsmpDpApptRjobDao().update_apptRjobDispatcher_01(
						newNextDateTime.getTime(), 
						rjob.getNextDateTime(), 
						updateDateTime, 
						updateUser, 
						apptRjobId, 
						rjob.getNextDateTime()
						);  //JpaSystemException: JDBC exception executing SQL
			} catch (JpaSystemException e) {
				String msg = StackTraceUtil.logTpiShortStackTrace(e);
				if (msg.indexOf("The database is read only") == -1) {
					throw e;
				}
			}
			rjob = getTsmpDpApptRjobDao().findById(apptRjobId).orElse(null);
			assert rjob != null;

			// 如果推移後的執行時間, 週期排程將會失效, 就不寫入 apptJob
			if (isRjobValid(rjob, newNextDateTime.getTime())) {
				TPILogger.tl.trace("產生下次週期工作: " + apptRjobId + "::" + ardStep_rjobD.getApptRjobDId() + "::" + toDtStr(newNextDateTime));
				return insertApptJob(ardStep_rjobD, newNextDateTime);
			}

			return null;
		} catch (ObjectOptimisticLockingFailureException e) {
			TPILogger.tl.warn("\nMaybe other worker has done \n " + apptRjobId);
			throw DgrRtnCode._1191.throwing();
		} catch (Exception e) {
			TPILogger.tl.error("推移週期排程失敗\n" + StackTraceUtil.logStackTrace(e));
			throw DgrRtnCode._1297.throwing();
		}
	}

	/**
	 * 檢查 Rjob 各欄位, 並找出資料庫現有的設定檔
	 * @param rjob
	 * @return
	 */
	protected TsmpDpApptRjob validateRjob(TsmpDpApptRjob rjob, String locale) {
		if (!StringUtils.hasText(rjob.getApptRjobId())) {
			TPILogger.tl.debug("缺少週期排程UID");
			throw DgrRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(rjob.getRjobName())) {
			TPILogger.tl.debug("缺少週期排程名稱");
			throw DgrRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(rjob.getCronExpression())) {
			TPILogger.tl.debug("缺少週期排程表達式");
			throw DgrRtnCode._1296.throwing();
		}
		try {
			CronExpression.parse(rjob.getCronExpression());
		} catch (IllegalArgumentException e) {
			TPILogger.tl.debug("週期排程表達式錯誤");
			throw DgrRtnCode._1290.throwing();
		}
		if (!StringUtils.hasText(rjob.getCronJson())) {
			TPILogger.tl.debug("缺少週期排程表達式表單");
			throw DgrRtnCode._1296.throwing();
		}
		if (rjob.getNextDateTime() == null) {
			TPILogger.tl.debug("缺少下次執行時間");
			throw DgrRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(rjob.getStatus())) {
			TPILogger.tl.debug("缺少週期排程狀態");
			throw DgrRtnCode._1261.throwing();
		}
		Date effDateTime = checkPastDate(rjob.getEffDateTime(), "起始時間");
		Date invDateTime = checkPastDate(rjob.getInvDateTime(), "結束時間");
		if (effDateTime != null && invDateTime != null && invDateTime.compareTo(effDateTime) < 0) {
			TPILogger.tl.debug("結束時間不得小於起始時間");
			throw DgrRtnCode._1295.throwing();
		}
		TsmpDpItems items = getItemsById("RJOB_STATUS", rjob.getStatus(), false, locale);
		if (items == null) {
			TPILogger.tl.debug("週期排程狀態代碼不存在: " + rjob.getStatus());
			throw DgrRtnCode._1290.throwing();
		}
		Optional<TsmpDpApptRjob> opt_rjob = getTsmpDpApptRjobDao().findById(rjob.getApptRjobId());
		if (!opt_rjob.isPresent()) {
			TPILogger.tl.error(CHECK_FOR_PERIODIC_SCHEDULE_UID + rjob.getApptRjobId());
			throw DgrRtnCode._1298.throwing();
		}
		return opt_rjob.get();
	}

	private <T> T getValue(String key, Function<String, T> func, T defaultVal) {
		String strVal = getServiceConfig().get(key);
		try {
			return func.apply(strVal);
		} catch (Exception e) {
			TPILogger.tl.debug("Error value of " + strVal + ", set to default " + defaultVal);
		}
		return defaultVal;
	}

	protected Date checkPastDate(Long dt, String fieldName) {
		if (dt == null) return null;
		Date datetime = new Date(dt);
		if (datetime.compareTo(DateTimeUtil.now()) < 0) {
			TPILogger.tl.debug(fieldName + "不得小於現在");
			throw DgrRtnCode._1227.throwing();
		}
		return datetime;
	}

	/**
	 * 無條件進位, 將請求的時間, 往後調整至週期時間上<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:50, 現在時間為09:58, 則此函數會回傳10:00<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:57, 現在時間為09:58, 則此函數會回傳10:00<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:58, 現在時間為09:58, 則此函數會回傳10:00<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:59, 現在時間為09:58, 則此函數會回傳10:00<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:00, 現在時間為09:58, 則此函數會回傳10:00<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:05, 現在時間為09:58, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:50, 現在時間為10:00, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入09:55, 現在時間為10:00, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:00, 現在時間為10:00, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:05, 現在時間為10:00, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:10, 現在時間為10:00, 則此函數會回傳10:10<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:15, 現在時間為10:00, 則此函數會回傳10:20<br>
	 * ex: 週期為每0/10分鐘執行一次, 若傳入10:20, 現在時間為10:00, 則此函數會回傳10:20<br>
	 * @param requestTime
	 * @param currentTime
	 * @param expression
	 * @param skipTimes 不需要就傳-1
	 * @return
	 */
	protected Date getAdjustedExecutionTime(Date requestTime, Date currentTime, String expression, int skipTimes) {
		if (requestTime == null || !StringUtils.hasText(expression)){
			return null;
		}

		if (currentTime == null) {
			currentTime = Date.from(Instant.now());
		}

		CronExpression ce = CronExpression.parse(expression);
		ZonedDateTime requestZdt = ZonedDateTime.ofInstant(requestTime.toInstant(), ZoneId.systemDefault());
		ZonedDateTime currentZdt = ZonedDateTime.ofInstant(currentTime.toInstant(), ZoneId.systemDefault());

		ZonedDateTime executionTime;
		if (requestZdt.isAfter(currentZdt)) {
			executionTime = isOnScheduledTime(requestTime, expression) ? requestZdt : ce.next(requestZdt);
		} else {
			executionTime = ce.next(currentZdt);
		}

		for (int i = 0; i < skipTimes; i++) {
            assert executionTime != null;
            executionTime = ce.next(executionTime);
		}
		assert executionTime != null;
		return Date.from(executionTime.toInstant());
	}

	/**
	 * 判斷週期排程是否在生效期間(between effDateTime and invDateTime)
	 * @return
	 */
	protected boolean isRjobValid(TsmpDpApptRjob rjob, Long base) {
		String apptRjobId = rjob.getApptRjobId();
		Long effDateTime = rjob.getEffDateTime();
		
		boolean isStart = (effDateTime == null);	// 未設定表示即時開始
		if (!isStart) {
			isStart = (base >= effDateTime);
		}
		if (isStart) {
			TPILogger.tl.trace("週期排程已生效: " + apptRjobId);
		}

		boolean isEnd = isOverInvDateTime(rjob, new Date(base));

		return (isStart && !isEnd);
	}

	protected boolean isRjobValid(TsmpDpApptRjob rjob) {
		return isRjobValid(rjob, DateTimeUtil.now().getTime());
	}

	protected boolean isOverInvDateTime(TsmpDpApptRjob rjob, Date base) {
		Long invDateTime = rjob.getInvDateTime();
		if (invDateTime == null) return false;	// 未設定表示永遠不結束
		Date invDt = new Date(invDateTime);
		boolean isOver = base.compareTo(invDt) > 0;
		if (isOver) {
			TPILogger.tl.trace("週期排程已失效: " + rjob.getApptRjobId());
		}
		return base.compareTo(invDt) > 0;
	}

	/**
	 * 判斷某個時間點是否在週期上<br>
	 * 比對到秒
	 * @param dt
	 * @param expression
	 * @return
	 */
	protected boolean isOnScheduledTime(Date dt, String expression) {
		CronExpression ce = CronExpression.parse(expression);
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault())
				.withNano(0);  // 忽略納秒
		ZonedDateTime previous = dateTime.minusSeconds(1);

		ZonedDateTime nextDateTime = ce.next(previous);

		// 檢查 nextDateTime 是否為 null
		if (nextDateTime == null) {
			// 如果 next() 返回 null，表示沒有下一個執行時間
			// 這種情況下，當前時間肯定不是預定的執行時間
			return false;
		}

		return nextDateTime.equals(dateTime);
	}

	protected TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw DgrRtnCode._1297.throwing();
		}
		return i;
	}

	private String toDtStr(Long dt) {
		if (dt == null) return new String();
		return toDtStr(new Date(dt));
	}

	private String toDtStr(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return apptJobDispatcher;
	}

	protected ScheduledExecutorService getSyncExecutor() {
		if (this.syncExecutor == null) {
			this.syncExecutor = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("ApptRJobDispatcher"));
		}
		return this.syncExecutor;
	}

	protected ScheduledFuture<?> getSyncFuture(){
		return this.syncFuture;
	}

}
