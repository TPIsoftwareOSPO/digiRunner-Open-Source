package tpi.dgrv4.gateway.component.job;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public abstract class DeferrableJob extends Job {

	/**
	 * 被丟入Deferrable佇列的時間點
	 */
	private long deferTimestamp;

	private AtomicBoolean isDeferrableJobDone = new AtomicBoolean(false);

	/**
	 * 此instance的內容被拿去取代延遲佇列中的工作後<br>
	 * "被取代"的那個Job會放到refJob中做為參考<br>
	 * 用以觀察工作是否完成(isDeferrableJobDone)
	 */
	private LinkedList<DeferrableJob> refJobList = new LinkedList<>();

	public DeferrableJob() {
		super();
	}

	public DeferrableJob(String groupId) {
		super(groupId);
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		boolean isDeferred = deferIfSingleton(jobHelper, jobManager);

		// 如果第一次檢查時, 沒有相同 Group 的工作, 就看是否需要再等一會兒
		// 20230119因為永遠是false,而被sonarQube掃到,所以註解整段
		/*if (!isDeferred) {
			try {
				// 計算現在距離 JOB 被排入佇列的時間, 是否已超過 1秒, 若已超過則不等待, 直接再次檢查
				long diff = -1;
				do {
					diff = new Date().getTime() - getTimestamp();
					if (diff < 1000) {
						Thread.sleep(1000);
					}
				} while(diff < 1000);
			}catch (InterruptedException e) {
				this.logger.error("Wait error! ("+ this.getId() +")");
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			}catch (Exception e) {
				this.logger.error("Wait error! ("+ this.getId() +")");
			}

			// 再確認一次是否有其他相同 Group 的工作排入
			deferIfSingleton(jobHelper, jobManager);
		}*/
	}

	public void runAfter(JobHelperImpl jobHelper, JobManager jobManager) {
		final int cnt = jobManager.count(this.getGroupId());
		if (cnt <= 0) {
			try {
				runJob(jobHelper, jobManager);
			} catch(ObjectOptimisticLockingFailureException e) {
				TPILogger.tl.warn(StackTraceUtil.logTpiShortStackTrace(e));
			}
		}
		
		// 2025.3.7 - All DeferrableJob 在執行完也要 notify, 以免高併發, 長時間堆積
		synchronized (jobManager.buff2nd) {
			jobManager.buff2nd.notifyAll();
		}
	}

	public long getDeferTimestamp() {
		return this.deferTimestamp;
	}

	public void setIsDeferrableJobDone() {
		this.isDeferrableJobDone.set(true);
	}

//	public boolean isDeferrableJobDone() {
//		if (isReplaced()) {
//			boolean isRefJobDone = true;
//			for(DeferrableJob refjob : refJobList) {
//				isRefJobDone &= refjob.isDeferrableJobDone();
//			}
//			return isRefJobDone;
//		}
//		return this.isDeferrableJobDone.get();
//	}

	public boolean isReplaced() {
		return !this.refJobList.isEmpty();
	}

	public void addRefJob(DeferrableJob refJob) {
		this.refJobList.add(refJob);
	}

	public abstract void runJob(JobHelperImpl jobHelper, JobManager jobManager);

	public abstract void replace(DeferrableJob source);

	/**
	 * 若無相同群組的工作則排入延遲佇列
	 * @param jobHelper
	 * @param jobManager
	 * @param gId
	 * @return
	 */
	private boolean deferIfSingleton(JobHelperImpl jobHelper, JobManager jobManager) {
		/* 2023.01.10 檢查一級佇列是否有相同群組的同時，順便將同群組的工作從佇列移除，等於自己就是唯一的工作，再把自己丟入二級佇列。
		 * 此舉與原先只將佇列中同群組的最後一個工作丟入二級佇列的意義是相同的。
		final int cnt = jobManager.count(this.getGroupId());
        if (cnt <= 0) {
        	this.deferTimestamp = ZonedDateTime.now().toInstant().toEpochMilli();
			jobHelper.defer(this);
			TPILogger.tl.trace("[#JOB#]" + this.getClass() + "落入二級佇列");
			return true;
		}
        return false;
        */
		
		// 不必移除一級了, 改由 buff 處理
//		jobManager.removeByGroupId(getGroupId());
		
		this.deferTimestamp = ZonedDateTime.now().toInstant().toEpochMilli();
		TPILogger.tl.trace("[#JOB#]" + this.getClass() + "落入二級佇列" + ", this MEM ref:" + this);
		jobHelper.defer(this);
		return true;
	}
}