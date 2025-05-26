package tpi.dgrv4.gateway.component.job.appt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpRjobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 只有週期排程發出的工作才會使用此類別<br>
 * 由 {@link ApptJobDispatcher#dispatchCacheJobs()} 發出
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class SerialApptJob extends DeferrableJob {

	private TPILogger logger;

	private List<Long> jobHistory;

	private ApptJob active;

	private TsmpDpApptJob activeApptJob;

	private TsmpDpApptJobDao tsmpDpApptJobDao;

	private TsmpDpApptRjobDao tsmpDpApptRjobDao;

	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;

	private ApptJobDispatcher apptJobDispatcher;

	private ApptRjobDispatcher apptRjobDispatcher;

	@Autowired
	public SerialApptJob(ApptJob apptJob, TsmpDpApptJobDao tsmpDpApptJobDao, //
			TsmpDpApptRjobDao tsmpDpApptRjobDao, TsmpDpApptRjobDDao tsmpDpApptRjobDDao, //
			ApptJobDispatcher apptJobDispatcher, ApptRjobDispatcher apptRjobDispatcher,
			TPILogger logger) {
		// 2021.08.10 若不設定, 則使用預設的群組ID會造成多個 SerialApptJob 同時排入二級佇列時互相取代
		super(apptJob.getTsmpDpApptJob().getPeriodUid());

		this.jobHistory = new ArrayList<>();
		this.active = apptJob;
		setActiveApptJob();
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.apptJobDispatcher = apptJobDispatcher;
		this.apptRjobDispatcher = apptRjobDispatcher;
		this.logger = logger;
	}

	private void setActiveApptJob() {
		this.activeApptJob = ServiceUtil.deepCopy(this.active.getTsmpDpApptJob(), TsmpDpApptJob.class);
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		// 先排入下一週期的工作, 同時更改週期狀態為"執行中"
		// 2021.01.12 如果已預定下次週期的工作, 就不用再排入
		boolean isSchedulable = isSchedulable( //
			this.activeApptJob.getApptJobId(), //
			this.activeApptJob.getPeriodUid(), //
			this.activeApptJob.getPeriodNexttime());
		if (isSchedulable) {
			scheduleNextPeriod(this.activeApptJob.getPeriodUid(), "SYS");
		}
		while (this.active != null) {
			writeLog();
			this.active.runJob(jobHelper, jobManager);
			scheduleNextRjobD();
		}
		// 恢復週期排程狀態
		String rjobStatus = restoreRjobStatus();
		
		Date periodNexttime = new Date(this.activeApptJob.getPeriodNexttime());
		this.logger.trace("結束序列排程" + this.jobHistory + ", 週期狀態為(" + rjobStatus +"): " +
			this.activeApptJob.getPeriodUid() + "::" + this.activeApptJob.getPeriodItemsId() + "::" + 
			DateTimeUtil.dateTimeToString(periodNexttime, DateTimeFormatEnum.西元年月日時分秒_2).orElse(""));
	}

	@Override
	public void replace(DeferrableJob source) {
		SerialApptJob sourceJob = (SerialApptJob) source;
		Collections.copy(this.jobHistory, sourceJob.getJobHistory());
		this.active.replace(sourceJob.getActive());
	}

	private boolean isSchedulable(Long apptJobId, String periodUid, Long periodNexttime) {
		TsmpDpApptJob appointment = this.tsmpDpApptJobDao //
			.findFirstByPeriodUidAndPeriodNexttimeGreaterThan(periodUid, periodNexttime);
		if (appointment != null) {
			this.logger.debugDelay2sec("已預約下次週期, 故不推移週期排程" + apptJobId);
			return false;
		}
		return true;
	}

	/**
	 * 寫入下一周期的工作 (SerialApptJob)
	 * @return
	 */
	private TsmpDpApptJob scheduleNextPeriod(String apptRjobId, String updateUser) {
		try {
			return this.apptRjobDispatcher.step(apptRjobId, updateUser, true);
		} catch (DgrException e) {
			this.logger.warn("\nMaybe other worker has done \n" + StackTraceUtil.logStackTrace(e) + "\n");
		} catch (Exception e) {
			this.logger.error("無法產生下次週期的工作\n" + StackTraceUtil.logStackTrace(e));
		}
		return null;
	}

	/**
	 * 依照週期排程原本的狀態決定變更的狀態<br>
	 * 原先狀態若非執行中, 這邊還是會更新updateDateTime的目的是為了做一個標記,紀錄本次週期已結束
	 */
	private String restoreRjobStatus() {
		final String apptRjobId = this.activeApptJob.getPeriodUid();
		Optional<TsmpDpApptRjob> opt = this.tsmpDpApptRjobDao.findById(apptRjobId);
		if (opt.isPresent()) {
			TsmpDpApptRjob rjob = opt.get();
			String status = rjob.getStatus();
			if (TsmpDpRjobStatus.IN_PROGRESS.value().equals(status)) {
				// 如果不是執行中, 就保留原本的狀態(有可能被其他主機押上暫停或停用了)
				rjob.setStatus(TsmpDpRjobStatus.ACTIVE.value());
			}
			rjob.setUpdateDateTime(DateTimeUtil.now());
			rjob.setUpdateUser("SYS");
			try {
				rjob = this.tsmpDpApptRjobDao.save(rjob); //JpaSystemException: could not execute statement [The database is read only
			} catch (JpaSystemException e) {
				String msg = StackTraceUtil.logTpiShortStackTrace(e);
				if (msg.indexOf("The database is read only") == -1) {
					throw e;
				}
			}
			return rjob.getStatus();
		}
		return null;
	}

	private void writeLog() {
		final Long apptJobId = this.activeApptJob.getApptJobId();
		this.jobHistory.add(apptJobId);
		this.logger.trace("準備執行序列排程: " + apptJobId);
	}

	private synchronized void scheduleNextRjobD() {
		setActiveApptJob();
		this.active = null;
		
		// 本次工作執行完成才可接續執行
		final String status = this.activeApptJob.getStatus();
		this.logger.trace("序列排程執行結果(" + this.activeApptJob.getApptJobId() + "): " + status);
		if (!TsmpDpApptJobStatus.DONE.isValueEquals(status)) return;

		// 找出下一個接續的工作再執行
		final String apptRjobId = this.activeApptJob.getPeriodUid();
		final Long apptRjobDId = this.activeApptJob.getPeriodItemsId();
		TsmpDpApptRjobD rjobD = this.tsmpDpApptRjobDDao.queryNextRjobD(apptRjobId, apptRjobDId);
		if (rjobD == null) return;
		
		TsmpDpApptJob next = createNext(rjobD);
		this.active = createApptJob(next);
		setActiveApptJob();
	}

	private TsmpDpApptJob createNext(TsmpDpApptRjobD rjobD) {
		TsmpDpApptJob next = new TsmpDpApptJob();
		next.setRefItemNo(rjobD.getRefItemNo());
		next.setRefSubitemNo(rjobD.getRefSubitemNo());
		next.setInParams(rjobD.getInParams());
		next.setIdentifData(rjobD.getIdentifData());
		next.setStartDateTime(this.activeApptJob.getStartDateTime());
		next.setPeriodUid(rjobD.getApptRjobId());
		next.setPeriodItemsId(rjobD.getApptRjobDId());
		next.setPeriodNexttime(this.activeApptJob.getPeriodNexttime());
		next.setCreateDateTime(DateTimeUtil.now());
		next.setCreateUser("SYS");
		this.logger.trace("主動排入下個週期排程項目: rjobId=" + next.getPeriodUid() + ", rjobDId=" + next.getPeriodItemsId()
				+ ", nextDt=" + DateTimeUtil.dateTimeToString(new Date(next.getPeriodNexttime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String())
			    + ", inParams=" + next.getInParams());
		try {
			next = this.tsmpDpApptJobDao.save(next);
		} catch (DataIntegrityViolationException e) {
			this.logger.debug("已寫入相同的週期排程工作: " + next.getPeriodUid() + "::"+next.getPeriodItemsId() + "::" 
		        + DateTimeUtil.dateTimeToString(new Date(next.getPeriodNexttime()), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String()));
			next = null;
		} 
		return next;
	}

	private ApptJob createApptJob(TsmpDpApptJob next) {
		if (next == null) {
			return null;
		}
		ApptJob apptJob = null;
		String beanName = null;
		try {
			beanName = this.apptJobDispatcher.getBeanName(next);
			apptJob = this.apptJobDispatcher.getBeanByName(beanName, next);
		} catch (Exception e) {
			this.logger.error("無法建立名為 \"" + beanName + "\" 的排程工作\n" + StackTraceUtil.logStackTrace(e));
			// 更新DB
			String eMsg = ServiceUtil.truncateExceptionMessage(e, ApptJob.MAX_STACK_TRACE_LENGTH);
			next.setStackTrace(eMsg);
			next.setStatus(TsmpDpApptJobStatus.ERROR.value());
			next.setUpdateDateTime(DateTimeUtil.now());
			next.setUpdateUser(next.getCreateUser());
			this.tsmpDpApptJobDao.save(next);
		}

		return apptJob;
	}

	public List<Long> getJobHistory() {
		return jobHistory;
	}

	public void setJobHistory(List<Long> jobHistory) {
		this.jobHistory = jobHistory;
	}

	public ApptJob getActive() {
		return active;
	}

	public void setActive(ApptJob active) {
		this.active = active;
	}

}
