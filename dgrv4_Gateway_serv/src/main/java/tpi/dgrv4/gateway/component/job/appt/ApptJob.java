package tpi.dgrv4.gateway.component.job.appt;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.LimitLogger;

/**
 * 因排程工作是由 DeferrableJob 實作
 * 所以若某一工作執行太久, Queue 中累積了其他重複的 apptJobId (groupId)
 * 則只會丟入最後一筆工作到延遲佇列
 */
@SuppressWarnings("serial")
public abstract class ApptJob extends DeferrableJob implements TsmpDpApptJobSetter, ApptJobAttachmentFilter {

	public final static Integer MAX_STACK_TRACE_LENGTH = 1000;

	private TPILogger logger;

	private TsmpDpApptJob tsmpDpApptJob;
	private ApptJobDispatcher apptJobDispatcher;
	private TsmpDpApptJobDao tsmpDpApptJobDao;

	// 此處的 TsmpDpApptJob, 是  deep clone 而來的
	@Autowired
	public ApptJob(TsmpDpApptJob tsmpDpApptJob, TPILogger logger, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		// 以 apptJobId 作為 groupId, 會使得重複的工作被丟棄
		super(String.valueOf(tsmpDpApptJob.getApptJobId()));
		this.tsmpDpApptJob = tsmpDpApptJob;
		this.logger = logger;
		this.apptJobDispatcher = apptJobDispatcher;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		runJobBody();
	}
	
	private DpaaSystemInfoHelper dpaaSystemInfoHelper = DpaaSystemInfoHelper.getInstance();
	
	protected DpaaSystemInfoHelper getDpaaSystemInfoHelper() {
		return dpaaSystemInfoHelper;
	}
	
	@Value("${job.start.threshold:60}")
    Integer jobStartThreshold = 60; // 預設 60% 即不執行
	
	@Value("${job.start.enable:true}")
	Boolean jobStartEanble = true; // 預設 true , false 表示不執行
	
	public void runJobBody() {
		
		// 可利用 properties 指定某些 gateway 不執行排程例如:Keeper 
		if (jobStartEanble==false) {
			TPILogger.tl.warn("    job.start.enable :: " + jobStartEanble);
			return;
		}
		
		// CPU load %
		DpaaSystemInfo infoVo = new DpaaSystemInfo();
		getDpaaSystemInfoHelper().setCpuUsedRateAndMem(infoVo);
		float cpuLoad = infoVo.getCpu() * 100 ;
		if (cpuLoad > jobStartThreshold ) { // 60% 以上即 abort missing
			TPILogger.tl.warn("cpu load = " + cpuLoad + ", too heigh , abort ApptJob mission");
			return ;
		} 
		
		if (TPILogger.lc != null) {
			// 限流 1 sec 輸出 ApptJob 本體類別 
			LimitLogger.logWithThrottle(String.format("""
				... nodeName = %s
				... %.2f %%... CPU Load
				... getClass().getName()=%s
				... Stacktrace ...
				%s
				""", 
				TPILogger.lc.userName, 
				infoVo.getCpu() * 100, 
				this.getClass().getName(), 
				StackTraceUtil.getStackTraceAsString()), 
					this.getClass().getName(), 
					1);
		}

		try {
			// 週期排程的流程中, 只要有"暫停"、"作廢"...等操作, 都會刪除 ApptJob 的資料, 所以才會需要在執行前判斷資料是否還在
			Boolean isExists = getTsmpDpApptJobDao().existsById(this.tsmpDpApptJob.getApptJobId());
			if (!isExists) {
				Boolean isCancellable = checkCancellable();
				if (isCancellable) {
					jobCancel();
				} else {
					this.logger.trace("工作已不存在, 無法取消也不會執行: " + this.tsmpDpApptJob.getApptJobId());
				}
				// 工作沒有被押上 CANCEL 的話, 就不會回押週期狀態為 ACTIVE, 仍會是 IN_PROCESS
				return;
			}
			
			jobStart();

			// 2020.11.26; Kim, apptJob執行中所拋出的任何例外錯誤，
			// 都應被外層的 catch (Exception e) 所接住，
			// 而不能被外層的 catch (ObjectOptimisticLockingFailureException e) 接住，
			// 因為外層的 ObjectOptimisticLockingFailureException 是為了接住在更新 tsmp_dp_appt_job 時所發生的錯誤
			try {
				final String execResult = runApptJob();
				this.tsmpDpApptJob.setExecResult(execResult);
			} catch (CancellationException e) {
				throw e;	// for interrupting RunLoopJob
			} catch (Exception e) {
				throw new Exception(e);
			}
			
			jobDone();
		} catch (ObjectOptimisticLockingFailureException e) {
			// 失敗: abort，刪除本記錄，最後Refresh MemList
			this.logger.trace("工作已被執行: " + getTsmpDpApptJob().getApptJobId());
			this.logger.warn("\n Maybe other worker has done \n" + StackTraceUtil.logTpiShortStackTrace(e));

		} catch (CancellationException e) {
			jobCancel();
		} catch (Exception e) {
			jobError(e);
		} finally {
			getApptJobDispatcher().resetRefreshSchedule();
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		ApptJob sJob = (ApptJob) source;
		this.tsmpDpApptJob = ServiceUtil.deepCopy(sJob.getTsmpDpApptJob(), TsmpDpApptJob.class);
	}

	@Override
	public TsmpDpApptJob set(TsmpDpApptJob job) {
		return getTsmpDpApptJobDao().saveAndFlush(job);
	}

	@Override
	public List<String> provideReplicableFileNames() {
		return Collections.emptyList();
	}

	private void jobStart() {
		updateStatus(TsmpDpApptJobStatus.RUNNING);
	}

	/**
	 * 工作執行成功 Update status = Done，最後Refresh MemList
	 */
	private void jobDone() {
		updateStatus(TsmpDpApptJobStatus.DONE);
	}

	/**
	 * 工作執行失敗時需Update status = Error， 並發出告警，最後Refresh MemList
	 * @param e
	 */
	private void jobError(Exception exception) {
		final String stackTrace = ServiceUtil.truncateExceptionMessage(exception, MAX_STACK_TRACE_LENGTH);
		this.tsmpDpApptJob.setStackTrace(stackTrace);
		try {
			updateStatus(TsmpDpApptJobStatus.ERROR); // database read only
		} catch (JpaSystemException e) {
			String msg = StackTraceUtil.logTpiShortStackTrace(e);
			if (msg.indexOf("The database is read only") == -1) {
				throw e;
			}
		}
	}

	private void jobCancel() {
		this.logger.debug("把工作押上取消: " + this.tsmpDpApptJob.getPeriodUid() + "::" +
			this.tsmpDpApptJob.getPeriodItemsId() + "::" +
			Optional.ofNullable(this.tsmpDpApptJob.getPeriodNexttime()) //
			.map((millis) -> new Date(millis)) //
			.flatMap((dt) -> DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2)) //
			.orElse("")
		);
		this.tsmpDpApptJob.setExecResult("MANUAL_CANCEL");

		try {
			updateStatus(TsmpDpApptJobStatus.CANCEL);
		} catch (ObjectOptimisticLockingFailureException e) {
			// 失敗: abort，刪除本記錄，最後Refresh MemList
			this.logger.warn("\n Maybe other worker has done \n" + StackTraceUtil.logTpiShortStackTrace(e));
		}
	}

	private void updateStatus(TsmpDpApptJobStatus status) {
		this.tsmpDpApptJob.setStatus(status.value());
		this.tsmpDpApptJob.setUpdateDateTime(DateTimeUtil.now());
		this.tsmpDpApptJob.setUpdateUser(this.tsmpDpApptJob.getCreateUser());
		save();
	}

	/**
	 * 長時工作每執行一個段落需更新step值
	 * @param step
	 */
	protected void step(String step) {
		TPILogger.tl.info("Report Job:: " + step + " ...step()");
		this.tsmpDpApptJob.setJobStep(step);
		this.tsmpDpApptJob.setUpdateDateTime(DateTimeUtil.now());
		this.tsmpDpApptJob.setUpdateUser(tsmpDpApptJob.getCreateUser());
		save();
	}

	public void save() {
			TsmpDpApptJob dbJob = getTsmpDpApptJobDao().saveAndFlush(this.tsmpDpApptJob);
			this.tsmpDpApptJob = ServiceUtil.deepCopy(dbJob, TsmpDpApptJob.class);
	}

	/**
	 * 如果資料庫這筆工作已經被砍掉了, 這裡判斷是否需要再回寫一筆紀錄"已取消"<br>
	 * 如果不是週期排程, 就先不回寫; 若是週期排程工作, 則要看是否已有相同UK存在, 沒有才可回寫
	 * @return
	 */
	private Boolean checkCancellable() {
		if (!isPeriodJob()) {
			return Boolean.FALSE;
		}
		
		String apptRjobId = this.tsmpDpApptJob.getPeriodUid();
		Long apptRjobDId = this.tsmpDpApptJob.getPeriodItemsId();
		Long nextDateTime = this.tsmpDpApptJob.getPeriodNexttime();
		TsmpDpApptJob periodJob = getApptJobDispatcher().getPeriodJob( //
				apptRjobId, apptRjobDId, nextDateTime);

		return (periodJob == null);
	}

	/**
	 * 判斷此實例是否為週期排程工作
	 * @return
	 */
	public boolean isPeriodJob() {
		if (
			StringUtils.hasText(this.tsmpDpApptJob.getPeriodUid()) &&
			this.tsmpDpApptJob.getPeriodItemsId() != null &&
			this.tsmpDpApptJob.getPeriodNexttime() != null
		) {
			return true;
		}
		return false;
	}

	public abstract String runApptJob() throws Exception;

	public TsmpDpApptJob getTsmpDpApptJob() {
		return this.tsmpDpApptJob;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

}
