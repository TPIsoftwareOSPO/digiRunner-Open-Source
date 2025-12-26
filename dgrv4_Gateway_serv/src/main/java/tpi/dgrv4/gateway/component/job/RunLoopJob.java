package tpi.dgrv4.gateway.component.job;

import org.springframework.util.CollectionUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;

/**
 * 此工作會讓排程狀態將永遠不會變成"D"(完成)，除非預約時間遞延到隔日，或是將設定值改為"隔日不建立新工作"，
 * 否則每次工作結束後，會自動預約下一次執行的時間，並維持排程狀態為"W"(等待)。
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class RunLoopJob extends DeferrableJob {


	private ApptJob targetJob;

	private ApptJobDispatcher apptJobDispatcher;

	private FileHelper fileHelper;

	private final int interval;

	public RunLoopJob(ApptJob apptJob, ApptJobDispatcher apptJobDispatcher, FileHelper fileHelper, int interval) {
		this.targetJob = apptJob;
		this.apptJobDispatcher = apptJobDispatcher;
		this.fileHelper = fileHelper;
		this.interval = interval;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		// 先計算下次預約時間, 避免因工作執行太久, 影響預約週期
		// Set nano-of-second to 0 in order to cooperate with TaskScheduler of ApptJobDispatcher
		int interval = getInterval();
		LocalDateTime currentExecuteTime = getCurrentExecuteTime();
		LocalDateTime nextStartDateTime = currentExecuteTime.plusSeconds(interval).withNano(0);
		
		// 執行工作內容
		getTargetJob().runJobBody();
		
		// 只有"D"(完成)才可進入下一次循環
		TsmpDpApptJob currentJob = getTargetJob().getTsmpDpApptJob();
		String status = currentJob.getStatus();
		if (!TsmpDpApptJobStatus.DONE.isValueEquals(status)) {
			TPILogger.tl.trace("Break job loop for illegal job status '"+status+"', id="+currentJob.getApptJobId());
			return;
		}
		
		boolean createNewWhenOverToday = getCreateNewFlag();	// 超過隔日是否新建工作
		if (createNewWhenOverToday) {
			LocalDateTime todayMax = currentExecuteTime.with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
			boolean isOverToday = (nextStartDateTime.compareTo(todayMax) > 0);	// 下一次預約的時間是否超過今日
			if (isOverToday) {
				// 建立新的循環工作
				TsmpDpApptJob newJob = new TsmpDpApptJob();
				newJob.setRefItemNo(currentJob.getRefItemNo());
				newJob.setRefSubitemNo(currentJob.getRefSubitemNo());
				newJob.setInParams(currentJob.getInParams());
				newJob.setExecOwner(currentJob.getExecOwner());
				newJob.setStartDateTime( toDate(nextStartDateTime) );
				newJob.setFromJobId(currentJob.getApptJobId());
				newJob.setIdentifData(currentJob.getIdentifData());
				newJob.setCreateUser(currentJob.getCreateUser());
				newJob = getApptJobDispatcher().addAndRefresh(newJob);

				// Copy job related attachments
				copyAttachments(currentJob.getApptJobId(), newJob.getApptJobId());

				// 通知其他 node 同步工作(失敗時不應中斷流程)
				getApptJobDispatcher().refreshAllNodes();
				// 刷新本地工作
				getApptJobDispatcher().resetRefreshSchedule();

				TPILogger.tl.trace("Create new appt job loop: "+newJob.getApptJobId()+" and will start at {}"+
					DateTimeUtil.dateTimeToString(newJob.getStartDateTime(), DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));

				return;
			}
		}

		// 讓此工作回到等待(W)狀態
		currentJob.setStartDateTime( toDate(nextStartDateTime) );
		currentJob.setStatus(TsmpDpApptJobStatus.WAIT.value());
		currentJob.setUpdateDateTime(DateTimeUtil.now());
		String updateUser = currentJob.getCreateUser();
		currentJob.setUpdateUser(updateUser);
		getTargetJob().save();
		
		// 通知其他 node 同步工作(失敗時不應中斷流程)
		getApptJobDispatcher().refreshAllNodes();
		// 刷新本地工作
		getApptJobDispatcher().resetRefreshSchedule();
		
		TPILogger.tl.trace("Appt job (id: "+currentJob.getApptJobId()+") enters next loop and will start at {}"+
			DateTimeUtil.dateTimeToString(currentJob.getStartDateTime(), DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
	}

	@Override
	public void replace(DeferrableJob source) {
		RunLoopJob sourceJob = (RunLoopJob) source;
		this.targetJob = sourceJob.getTargetJob();
	}

	private void copyAttachments(Long fromJobId, Long toJobId) {
		String refFileCateCode = TsmpDpFileType.TSMP_DP_APPT_JOB.value();
		boolean isOverride = false;
		try {
			// Copy only job-specific attachments
			final List<String> fileNames = getTargetJob().provideReplicableFileNames();
			getFileHelper().copyFilesByRefFileCateCodeAndRefId(refFileCateCode, fromJobId, //
				refFileCateCode, toJobId, (tsmpDpFile) -> {
					if (CollectionUtils.isEmpty(fileNames)) return true;
					return fileNames.contains(tsmpDpFile.getFileName());
				}, isOverride);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/** 循環間隔(sec) */
	protected int getInterval() {
		return this.interval;
	}

	/** 超過隔日是否新建工作 */
	protected boolean getCreateNewFlag() {
		return Boolean.TRUE;
	}

	/** 取得工作執行當下的時間點 */
	protected LocalDateTime getCurrentExecuteTime() {
		return LocalDateTime.now();
	}

	private Date toDate(LocalDateTime ldt) {
		ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
		Instant instant = zdt.toInstant();
		Date dt = Date.from(instant);
		return dt;
	}

	protected ApptJob getTargetJob() {
		return this.targetJob;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}