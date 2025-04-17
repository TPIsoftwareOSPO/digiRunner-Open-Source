package tpi.dgrv4.gateway.component.job;

import org.springframework.beans.factory.annotation.Autowired;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DeferrableJobManager extends JobManager {
	
	private ServiceConfig serviceConfig;
	
	@Autowired
	public DeferrableJobManager(ServiceConfig serviceConfig, JobHelperImpl jobHelper) {
		super(jobHelper);
		this.serviceConfig = serviceConfig;
	}

	private Integer poolSize;

	/** 負責把工作從 buff2nd 拿工作出來執行 */
	private ExecutorService executor2ndSingle;

	/** 負責執行工作 */
//	public ExecutorService executor2nd;

	/**
	 * Will be called by @PostConstruct in {@link JobManager}
	 */
	@Override
	public void init() {
		this.poolSize = getValue("scheduler.deferrable.thread-pool-size", Integer::valueOf, 1);
		if (this.poolSize == null || this.poolSize < 1) {
			this.poolSize = 1;
		}
		for (int i = 0; i < this.poolSize; i++) {
			Thread t = new Thread(this);
			t.start();
		}
		this.executor2ndSingle = Executors.newSingleThreadExecutor(r -> {
		    Thread thread = new Thread(r);
		    thread.setName("Job-2nd-runner");
		    return thread;
		});
		
		AtomicInteger threadCounter = new AtomicInteger(1);
//		this.executor2nd = Executors.newFixedThreadPool(20, r -> {
//		    Thread thread = new Thread(r);
//		    thread.setName("deferrable-Job-Queue-" + threadCounter.getAndIncrement());
//		    return thread;
//		});
		
		TPILogger.tl.debugDelay2sec("JobManager(" +this.getClass().getCanonicalName() + ") is initialized with thread-pool-size (" + this.poolSize + ")");
	}

	@Override
	public void run() {
		int i;
		while (true) {
			i = takeDeferrableJob();
			if (i == 0) {
				TPILogger.tl.error("DeferrableJobManager is terminated unexpectedly!");
				break;
			}
		}
	}

	public int takeDeferrableJob() {
		try {
			final DeferrableJob job = (DeferrableJob) take();
			
			this.jobLogs.put(job.getGroupId(), System.currentTimeMillis());
			String traceMsg = "[#JOB#][BEGIN]DeferrableJob: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			TPILogger.tl.trace(traceMsg);
			
			job.runAfter(getJobHelper(), this);
			job.setIsDeferrableJobDone();

			long start = this.jobLogs.remove(job.getGroupId());
			traceMsg = "[#JOB#][END]DeferrableJob: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			traceMsg += ", cost: " + (System.currentTimeMillis() - start) + "ms";
			TPILogger.tl.trace(traceMsg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			TPILogger.tl.warn("takeDeferrableJob was interrupted: " + StackTraceUtil.logStackTrace(e));
			return 0;
		} catch (Exception e) {
			TPILogger.tl.error("takeDeferrableJob error: " + StackTraceUtil.logStackTrace(e));
		}
		return 1;
	}

	@Override
	public void takeAllJobs() {
		synchronized (this.queueLock) {
			try {
				while (peek() != null) {
					takeDeferrableJob();
				}
			} finally {
				this.queueLock.notifyAll();
			}
		}
	}

	/**
	 * @param source
	 * @return 被取代的工作數量
	 */
	public int replaceByGroupId(DeferrableJob source) {
		AtomicInteger cnt = new AtomicInteger();
		
		final String srcGroupId = source.getGroupId();
		Iterator<Job> iterator = iterator();
		//放入二級之前
		while(iterator.hasNext()) {
			final DeferrableJob target = (DeferrableJob) iterator.next();
			if (srcGroupId.equals(target.getGroupId())) {
				target.replace(source);
				source.addRefJob(target);
				cnt.addAndGet(1);
			}
		}

		return cnt.get();
	}

	public void take2ndJob() {
		this.executor2ndSingle.execute(() -> {
			// 判斷要不要做
//			buff2ndWait__(); //2025.3.11, 在高併發時, 一直是 MM leak 的點, 故不使用它

			// buff2nd 取一個, 並移除一個
			Map.Entry<String, Job> entry = null;
			synchronized (this.buff2nd) {
				Iterator<Map.Entry<String, Job>> itr = this.buff2nd.entrySet().iterator();
				if (itr.hasNext()) {
					entry = itr.next(); // NoSuchElementException
					itr.remove(); // ConcurrentModificationException
				}
			}
			
			if (entry == null) return ;
			
			Job valJob = entry.getValue();
			Thread.startVirtualThread(() -> {
				// pause and run 2nd Job, 但要檢查 2nd 中是否有相同的 key
			    ((DeferrableJob) valJob).runAfter(getJobHelper(), this);//這裡會有阻塞約 6 秒
			});

			// 再觸發一次 take2nd Job
			doAgainTake2ndJob();
		});
	}
	
	public int getExecutor2ndQueueSize() {
//		return -1; // UT 時不會啟動它
		return 19;
	}
	
//	public int getExecutor2ndAvalibleSize() {
//		// 查看執行緒池的狀態
//        int activeCount = ((ThreadPoolExecutor) executor2nd).getActiveCount();
//        int poolSize = ((ThreadPoolExecutor) executor2nd).getPoolSize();
//		return poolSize - activeCount;
//	}

	public void doAgainTake2ndJob() {
		if (!this.buff2nd.isEmpty()) {
			executor2ndSingle.execute(() -> {
				take2ndJob(); // 2nd 若有工作就一直 do Job
			});
		}
	}

	// 我有一支 DeferrableJobManager.java 程式, 其中的 buff2ndWait() 在系統會被多個線程執行 wait 的動作, 在長時間高併發下會造成 Memory Leak,
	// 經過分析, 似乎是因為 notifyAll 丟失的問題導致, 故我想增加 notifyAll 的頻率與
	private void buff2ndWait() {
		synchronized (buff2nd) {
			while (buff2nd.isEmpty()) {  // AI:使用while而非if
				try {
					// 經由 Heap dump 的分析, 發現 buff2nd.wait() 會造成多線程進入 buff2nd 無法 GC 的問題, 因此我想在 wait 前後呼叫 notifyAll(), 以此解決信號丟失問題
					// 同時我想增加監控, 了解進入 wait 的次數與離開 wait 的次數最終應該相等, 而非持續增加
					buff2nd.notifyAll();
					//可以在任何地方使用 DeferrableJobManager.buff2ndWaitCount.get() 來獲取當前等待的執行緒數量。
//					buff2ndWaitCount.incrementAndGet(); //++
					buff2nd.wait(1);  // 保留超時, JobManager.java: take1stJob 有做 buff2nd.notifyAll(), 但 GKE 實測仍會停頓於此, 故加入 wait time out 避免停頓太久
//                    buff2ndWaitCount.decrementAndGet(); //--
					buff2nd.notifyAll();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			buff2nd.notifyAll();
		}
	}

	/** This is only for unit test */
	public void take2ndJob(Map<String, Job> buff2nd) {
		// 2nd 若有工作就一直 do Job
		while (!buff2nd.isEmpty()) {
			Map.Entry<String, Job> entry = null;
			Iterator<Map.Entry<String, Job>> itr = buff2nd.entrySet().iterator();
			if (itr.hasNext()) {
				entry = itr.next(); // NoSuchElementException
				itr.remove(); // ConcurrentModificationException
			}
			if (entry == null) {
				return;
			}

			Job valJob = entry.getValue();

			// run Job
			((DeferrableJob) valJob).runAfter(getJobHelper(), this);
		}
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

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
