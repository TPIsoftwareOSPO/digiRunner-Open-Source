package tpi.dgrv4.gateway.component.job.appt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.ApptJobEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.TCP.Packet.NotifyClientRefreshMemListPacket;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.RunLoopJob;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class ApptJobDispatcher implements Runnable {

	private final static String APPT_JOB_PREFIX = "apptJob";

	private Map<Long, TsmpDpApptJob> jobCache;

	private Long period;

	private Integer nr;	// Number of Record

	/* 2023.01.11 改在 TPILogger.connect() 中啟動執行緒
	private ScheduledFuture<?> refreshScheduleFuture;

	private ScheduledExecutorService taskScheduler;
	*/

	private final TsmpDpApptJobDao tsmpDpApptJobDao;
	private final TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private final TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private final JobHelper jobHelper;
	private final ApplicationContext ctx;
	private final ServiceConfig serviceConfig;
	private final ApptRjobDispatcher apptRjobDispatcher;
	private final TsmpSettingService tsmpSettingService;
	private final FileHelper fileHelper;

	// 是否啟用排程器
	private Boolean isSchedulerEnabled;
	
	// 未啟用排程器, 請設定 service.scheduler.appt-job.enable=true
	// Scheduler is not enabled, please set service.scheduler.appt-job.enable=true
	private final static String SCHEDULER_IS_NOT_ENABLED_PLEASE_SET = "Scheduler is not enabled, please set service.scheduler.appt-job.enable=true";
	
	// 排程儲存失敗 // Schedule save failed
	private final static String SCHEDULE_SAVE_FAILED = "Schedule save failed\n";
	
	@PostConstruct
	public void init() {
		initAttributes();
		
		if (!this.isSchedulerEnabled) {
			TPILogger.tl.info(SCHEDULER_IS_NOT_ENABLED_PLEASE_SET);
			return;
		}

		// 啟動Timer每1秒檢查本機MemList是否有可執行工作
		/* 2023.01.11 改在 TPILogger.connect() 中啟動執行緒
		getTaskScheduler().scheduleAtFixedRate(this, 0L, 1L, TimeUnit.SECONDS);
		*/

		// 啟動Timer每N分同步一次DB至 MemList，N大於30分鐘，每次取最近欲執行的5筆資料(狀態wait) 
		resetRefreshSchedule();

		TPILogger.tl.debugDelay2sec("ApptJobDispatcher is initialized");
	}

	public void initAttributes() {
		this.jobCache = new LinkedHashMap<>();
		this.period = getValue("job-dispatcher.period.ms", (val) -> {
			return Long.valueOf(val);
		}, 1800000L);
		this.nr = getValue("job-dispatcher.number-of-record", (val) -> {
			return Integer.valueOf(val);
		}, 5);
		this.isSchedulerEnabled = getValue("scheduler.appt-job.enable", (val) -> {
			return Boolean.valueOf(val);
		}, false);
	}

	// 檢查cache是否有應執行的工作
	@Override
	public void run() {
		synchronized (this.jobCache) {
			TPILogger.tl.trace("[#JOB#]檢查 cache 中...");

			dispatchCacheJobs();

			TPILogger.tl.trace("[#JOB#]檢查 cache 結束");
		}
	}

	/**
	 * 直接將排程寫到Cache以及資料庫中
	 * @param job
	 * @return
	 */
	public TsmpDpApptJob addAndRefresh(TsmpDpApptJob job) {
		if (job == null) {
			return null;
		}

		try {
			job = getTsmpDpApptJobDao().saveAndFlush(job); //JpaSystemException: error performing isolated work [The database is read only
		} catch (JpaSystemException e) {
			String err = e.getMessage();
			if (err != null && err.indexOf("The database is read only") != -1) {
				TPILogger.tl.debugDelay2sec("The database is read only");
			}else {
				TPILogger.tl.error(SCHEDULE_SAVE_FAILED + StackTraceUtil.logStackTrace(e));
			}
			return null;
		} catch (DataIntegrityViolationException e) {
			// 週期性排程的設計, 需要容許寫入 ApptJob 時出現 UK Constraint 例外
			if (StringUtils.hasText(job.getPeriodUid()) && job.getPeriodItemsId() != null) {
				TPILogger.tl.debugDelay2sec("已寫入相同的週期排程工作: " + //
					job.getPeriodUid() + "::" + job.getPeriodItemsId() + "::" + 
					DateTimeUtil.dateTimeToString(new Date(job.getPeriodNexttime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
			} else {
				TPILogger.tl.error(SCHEDULE_SAVE_FAILED + StackTraceUtil.logStackTrace(e));
			}
			return null;
		} catch (Exception e) {
			TPILogger.tl.error(SCHEDULE_SAVE_FAILED + StackTraceUtil.logStackTrace(e));
			return null;
		}

		try {
			if (this.isSchedulerEnabled) {
				// 塞入Cache
				synchronized (this.jobCache) {
					TsmpDpApptJob copyJob = ServiceUtil.deepCopy(job, TsmpDpApptJob.class);
					this.jobCache.put(job.getApptJobId(), copyJob);
					TPILogger.tl.trace("插入排程: " + job.getApptJobId());
				}
			} else {
				TPILogger.tl.info(SCHEDULER_IS_NOT_ENABLED_PLEASE_SET);
			}

			// 同步資料庫
			/* 此處沒有必要resetRefreshSchedule，而且若執行reset，則提早加進cache中的新工作會被retainWaitingJobs移除
			resetRefreshSchedule();
			*/

			return job;
		} catch (Exception e) {
			TPILogger.tl.error("插入排程失敗\n" + StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	/**
	 * 存入多筆工作
	 * @param jobs
	 * @return
	 */
	public List<TsmpDpApptJob> addAndRefresh(List<TsmpDpApptJob> jobs) {
		if (jobs == null || jobs.isEmpty()) {
			return Collections.emptyList();
		}

		List<TsmpDpApptJob> copiedJobs = new ArrayList<>();
		TsmpDpApptJob job = new TsmpDpApptJob();
		try {
			TsmpDpApptJob copiedJob = null;
			for(int i = 0; i < jobs.size(); i++) {
				job = jobs.get(i);
				job = getTsmpDpApptJobDao().saveAndFlush(job);
				copiedJob = ServiceUtil.deepCopy(job, TsmpDpApptJob.class);
				copiedJobs.add(copiedJob);
			}
		} catch (DataIntegrityViolationException e) {
			// 週期性排程的設計, 需要容許寫入 ApptJob 時出現 UK Constraint 例外
			if (StringUtils.hasText(job.getPeriodUid()) && job.getPeriodItemsId() != null) {
				TPILogger.tl.debugDelay2sec("已寫入相同的週期排程工作: " +
					job.getPeriodUid() + "::" + job.getPeriodItemsId() + "::" + 
					DateTimeUtil.dateTimeToString(new Date(job.getPeriodNexttime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
			} else {
				TPILogger.tl.error(SCHEDULE_SAVE_FAILED + StackTraceUtil.logStackTrace(e));
			}
			return null;
		} catch (Exception e) {
			TPILogger.tl.error(SCHEDULE_SAVE_FAILED + StackTraceUtil.logStackTrace(e));
			return Collections.emptyList();
		}

		try {
			if (this.isSchedulerEnabled) {
				// 塞入Cache
				synchronized (this.jobCache) {
					for(TsmpDpApptJob copiedJob : copiedJobs) {
						this.jobCache.put(copiedJob.getApptJobId(), copiedJob);
						TPILogger.tl.trace("插入排程: " + copiedJob.getApptJobId());
					}
				}
			} else {
				TPILogger.tl.info(SCHEDULER_IS_NOT_ENABLED_PLEASE_SET);
			}

			// 同步資料庫
			/* 此處沒有必要resetRefreshSchedule，而且若執行reset，則提早加進cache中的新工作會被retainWaitingJobs移除
			resetRefreshSchedule();
			*/

			return copiedJobs;
		} catch (Exception e) {
			TPILogger.tl.error("插入排程失敗\n" + StackTraceUtil.logStackTrace(e));
			return Collections.emptyList();
		}
	}

	/**
	 * 從資料庫撈取工作並重置Timer
	 */
	public void resetRefreshSchedule() {
		if (!this.isSchedulerEnabled) {
			TPILogger.tl.info(SCHEDULER_IS_NOT_ENABLED_PLEASE_SET);
			return;
		}
		
		/* 2023.01.11 改在 TPILogger.connect() 中啟動執行緒
		if (this.refreshScheduleFuture != null) {
			try {
				this.refreshScheduleFuture.cancel(false);
			} catch (Exception e) {
				this.logger.warn("無法重置同步排程\n" + StackTraceUtil.logStackTrace(e));
			}
		}

		this.refreshScheduleFuture = getTaskScheduler().scheduleAtFixedRate(() -> {
			refreshJobCache();
		}, 0L, this.period, TimeUnit.MILLISECONDS);
		*/

		refreshJobCache();

	}

	public void dispatchCacheJobs() {
		TsmpDpApptJob tsmpDpApptJob = null;
		Date startDateTime = null;
		String beanName = null;
		ApptJob apptJob = null;

		HashSet<Long> deletableIds = new HashSet<>();
		for(Map.Entry<Long, TsmpDpApptJob> entry : this.jobCache.entrySet()) {
			tsmpDpApptJob = entry.getValue();
			
			startDateTime = tsmpDpApptJob.getStartDateTime();	// 開始時間

			// 不能用 DateTimeUtil.now() 因為會去除毫秒, 比對時間不準確
			//if (startDateTime != null && startDateTime.compareTo(DateTimeUtil.now()) <= 0) {
			if (startDateTime != null && startDateTime.compareTo(new Date()) <= 0) {
				beanName = getBeanName(tsmpDpApptJob);
				
				// 20221104; Kim; 確認找得到 Bean 才執行，找不到也不能將狀態改為"失敗"
				try {
					apptJob = getBeanByName(beanName, tsmpDpApptJob);
				} catch (Exception e) {
					TPILogger.tl.trace("[#JOB#][" + tsmpDpApptJob.getApptJobId() + "]ApptJob bean '" + beanName + "' is not found in this module.");
					deletableIds.add(tsmpDpApptJob.getApptJobId());
					continue;
				}

				try {
					// 找到可執行工作項目後，將工作投入DeferrableQueue執行
					if (apptJob.isPeriodJob()) {
						SerialApptJob serialApptJob = getSerialApptJob(apptJob);
						getJobHelper().add(serialApptJob);
						TPILogger.tl.trace("[#JOB#][" + apptJob.getTsmpDpApptJob().getApptJobId() + "]SerialApptJob排入Queue");
					} else if (ApptJobEnum.RUNLOOP_ITEM_NO.equals(tsmpDpApptJob.getRefItemNo())) {
						RunLoopJob runLoopJob = getRunLoopJob(apptJob);
						getJobHelper().add(runLoopJob);
						TPILogger.tl.trace("[#JOB#][" + apptJob.getTsmpDpApptJob().getApptJobId() + "]RunLoopJob排入Queue");
					} else {
						getJobHelper().add(apptJob);
						TPILogger.tl.trace("[#JOB#][" + apptJob.getTsmpDpApptJob().getApptJobId() + "]ApptJob排入Queue");
					}
					deletableIds.add(tsmpDpApptJob.getApptJobId());
				} catch (Exception e) {
					TPILogger.tl.error("[#JOB#][" + apptJob.getTsmpDpApptJob().getApptJobId() + "]無法建立名為 \"" + beanName + "\" 的排程工作\n" + StackTraceUtil.logStackTrace(e));
					// 更新DB
					String eMsg = ServiceUtil.truncateExceptionMessage(e, ApptJob.MAX_STACK_TRACE_LENGTH);
					tsmpDpApptJob.setStackTrace(eMsg);
					tsmpDpApptJob.setStatus(TsmpDpApptJobStatus.ERROR.value());
					tsmpDpApptJob.setUpdateDateTime(DateTimeUtil.now());
					tsmpDpApptJob.setUpdateUser(tsmpDpApptJob.getCreateUser());
					getTsmpDpApptJobDao().save(tsmpDpApptJob);
					deletableIds.add(tsmpDpApptJob.getApptJobId());
				}
			} else {
				TPILogger.tl.trace("[#JOB#][" + tsmpDpApptJob.getApptJobId()
					+ "]現在("
					+ DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日時分秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295))
					+ ")未到開始執行時間: "
					+ DateTimeUtil.dateTimeToString(startDateTime, DateTimeFormatEnum.西元年月日時分秒_2)
					.orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
			}
		}
		// 刷新Cache
		deletableIds.forEach((deletableId) -> {
			this.jobCache.remove(deletableId);
		});
		TPILogger.tl.trace("[#JOB#]已排入" + deletableIds.size() + "個排程到Queue中: " + deletableIds);
	}

	public String getBeanName(TsmpDpApptJob job) {
		String beanName = APPT_JOB_PREFIX;
		if (StringUtils.hasText(job.getRefItemNo())) {
			beanName += "_" + job.getRefItemNo();
		}
		if (StringUtils.hasText(job.getRefSubitemNo())) {
			beanName += "_" + job.getRefSubitemNo();
		}
		return beanName;
	}

	public ApptJob getBeanByName(String beanName, Object ... args) {
		return (ApptJob) getCtx().getBean(beanName, args);
	}

	public void refreshJobCache() {
		synchronized (this.jobCache) {
			TPILogger.tl.trace("[#JOB#]同步工作清單中...");

			// 移除非等待中的Job
			retainWaitingJobs();

			// 新增job
			addExecutableJobs();
			
			TPILogger.tl.trace("[#JOB#]同步結束, 剩餘" +  this.jobCache.size() + "個排程在Cache中");
		}
	}

	/** 通知其他節點刷新排程工作(memList) */
	public void refreshAllNodes() {
		try {
            synchronized (TPILogger.lc) {//
                TPILogger.lc.send(new NotifyClientRefreshMemListPacket());
            }
        } catch (Exception e) {
        	TPILogger.tl.warn(String.format("Failed to notify node to refresh job list: %s", e.getMessage()));
        }
	}

	private void retainWaitingJobs() {
		HashSet<Long> expiredIds = new HashSet<>();
		TsmpDpApptJob dbData = null;
		String status = null;

		for(Long id : this.jobCache.keySet()) {
			Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(id);
			if (opt.isPresent()) {
				dbData = opt.get();
				status = dbData.getStatus();

				if (!TsmpDpApptJobStatus.WAIT.value().equals(status)) {
					expiredIds.add(id);
				}
			} else {
				expiredIds.add(id);
			}
		}
		expiredIds.forEach(id -> {
			this.jobCache.remove(id);
		});
		TPILogger.tl.trace("[#JOB#]移除" + expiredIds.size() + "個排程: " + expiredIds);
	}

	private void addExecutableJobs() {
		List<TsmpDpApptJob> executableJobs = findExecutableJobs();

		if (executableJobs == null || executableJobs.isEmpty()) {
			TPILogger.tl.trace("[#JOB#]存入0個排程到 cache 中");
			return;
		}

		String beanName = null;
		Long apptJobId = null;
		TsmpDpApptJob copyJob = null;
		int addCnt = 0;

		for(TsmpDpApptJob job : executableJobs) {
			if (addCnt >= this.nr) break;
			
			// 20210224; Kim; 先確認找得到 Bean 才將工作放入 memList
			apptJobId = job.getApptJobId();
			try {
				beanName = getBeanName(job);
				getBeanByName(beanName, job);
			} catch (Exception e) {
				TPILogger.tl.trace("[#JOB#][" +  + apptJobId + "]ApptJob bean '" + beanName + "' is not found in this module. apptJobjId=["+ apptJobId +"]");
				continue;
			}
			
			copyJob = ServiceUtil.deepCopy(job, TsmpDpApptJob.class);

			this.jobCache.put(apptJobId, copyJob);
			TPILogger.tl.trace("[#JOB#][" + apptJobId + "]工作已被存入Cache");
			addCnt++;
		}
		TPILogger.tl.trace("[#JOB#]存入" + addCnt + "個排程到 cache 中");
	}
		
	/**
	 * 可執行工作條件如下:
	 * where status=Wait & startDateTime<=now()
	 * @return
	 */
	private List<TsmpDpApptJob> findExecutableJobs() {
		Date maxStartDateTime = new Date( DateTimeUtil.now().getTime() + this.period );
		return getTsmpDpApptJobDao().queryExecutableJobs(maxStartDateTime);
	}

	private <T> T getValue(String key, Function<String, T> func, T defaultVal) {
		String strVal = getServiceConfig().get(key);
		try {
			return func.apply(strVal);
		} catch (Exception e) {
			TPILogger.tl.warn("Error value of " + strVal + ", set to default " + defaultVal);
		}
		return defaultVal;
	}

	public SerialApptJob getSerialApptJob(ApptJob apptJob) {
		return new SerialApptJob(apptJob, //
			getTsmpDpApptJobDao(), getTsmpDpApptRjobDao(), getTsmpDpApptRjobDDao(), this, //
			getApptRjobDispatcher(), TPILogger.tl);
	}

	public RunLoopJob getRunLoopJob(ApptJob apptJob) {
		int interval = getTsmpSettingService().getVal_TSMP_DPAA_RUNLOOP_INTERVAL();
		return new RunLoopJob(apptJob, this, getFileHelper(), interval);
	}

	public TsmpDpApptJob getPeriodJob(String periodUid, Long periodItemsId, Long periodNexttime) {
		return getTsmpDpApptJobDao().findFirstByPeriodUidAndPeriodItemsIdAndPeriodNexttime( //
				periodUid, periodItemsId, periodNexttime);
	}

	public List<Long> removeCacheJobsImmediately(List<Long> apptJobIds) {
		List<Long> removedIds = new ArrayList<>();
		synchronized (this.jobCache) {
			for (Long apptJobId : apptJobIds) {
				this.jobCache.remove(apptJobId);
				if (this.jobCache.get(apptJobId) == null) {
					removedIds.add(apptJobId);
				}
			}
			return removedIds;
		}
	}

	/* 2023.01.11 改在 TPILogger.connect() 中啟動執行緒
	protected ScheduledExecutorService getTaskScheduler() {
		if (this.taskScheduler == null) {
			// 一條用來同步DBList, 一條用來每秒檢查cacheList
			this.taskScheduler = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("ApptJobDispatcher"));
		}
		return this.taskScheduler;
	}
	*/

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected ApptRjobDispatcher getApptRjobDispatcher() {
		return this.apptRjobDispatcher;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
