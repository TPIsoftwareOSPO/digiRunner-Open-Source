package tpi.dgrv4.gateway.component.job;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class JobManager implements Runnable {

	protected JobHelperImpl jobHelper;

	private final int capacity;

	private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<>(200);
	
	// 使用 LinkedHashMap 的子類來限制大小
	public final Map<String, Job> buff1st = Collections.synchronizedMap(
	    new LinkedHashMap<String, Job>() {
	        private static final int MAX_SIZE = 200; // 設定最大大小
	        
	        @Override
	        protected boolean removeEldestEntry(Map.Entry<String, Job> eldest) {
	            return size() > MAX_SIZE;
	        }
	    }
	);
	public final Map<String, Job> buff2nd = Collections.synchronizedMap(
	    new LinkedHashMap<String, Job>() {
	        private static final int MAX_SIZE = 50; // 設定最大大小
	        
	        @Override
	        protected boolean removeEldestEntry(Map.Entry<String, Job> eldest) {
	            return size() > MAX_SIZE;
	        }
	    }
	);
//	public static final AtomicInteger buff2ndWaitCount = new AtomicInteger(0); // 使用一個 AtomicInteger


	protected final Object queueLock = new Object();

	protected final ConcurrentHashMap<String, Long> jobLogs;

	@Autowired
	public JobManager(JobHelperImpl jobHelper) {
//		this(Integer.MAX_VALUE, logger);
		this(100, jobHelper);
	}

	public JobManager(int capacity, JobHelperImpl jobHelper) {
		this.capacity = capacity;
//		this.queue = new LinkedBlockingQueue<>(this.capacity);
		
		this.jobLogs = new ConcurrentHashMap<String, Long>();
		TPILogger.tl.debugDelay2sec("JobManager(" + this.getClass().getCanonicalName() +") is initialized with capacity (" + this.capacity + ")");
		this.jobHelper = jobHelper;
	}

	@PostConstruct
	public void init() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		int i;
		while(true) {
			i = takeJob();
			if (i == 0) {
				TPILogger.tl.error("JobManager is terminated unexpectedly!");
				break;
			}
		}
	}

	public int takeJob() {
		try {
			// Take out a job from queue
			final Job job = this.take();
			
			this.jobLogs.put(job.getGroupId(), System.currentTimeMillis());
			String traceMsg = "[#JOB#][BEGIN]Job: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			TPILogger.tl.trace(traceMsg);
			
			job.run(getJobHelper(), this);
			job.setIsDone();
			
			long start = this.jobLogs.remove(job.getGroupId());
			traceMsg = "[#JOB#][END]Job: " + job.getGroupId();
			if (job instanceof ApptJob apptJob) {
				traceMsg += ", appt_id: " + apptJob.getTsmpDpApptJob().getApptJobId();
			}
			traceMsg += ", cost: " + (System.currentTimeMillis() - start) + "ms";
			TPILogger.tl.trace(traceMsg);
		}catch (InterruptedException e) {
			TPILogger.tl.debug("takeJob error: \n" + StackTraceUtil.logStackTrace(e));
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		}catch (Exception e) {
			TPILogger.tl.debug("takeJob error: \n" + StackTraceUtil.logStackTrace(e));
		}
		return 1;
	}
	
	/**
	 * 把 queue 裡的工作都拿出來做完
	 */
	public void takeAllJobs() {
		synchronized (this.queueLock) {
			try {
				while (peek() != null) {
					takeJob();
				}
			} finally {
				this.queueLock.notifyAll();
			}
		}
	}

	public void removeByGroupId(final String groupId) {
		Iterator<Job> iterator = iterator();
		Job job = null;
		int i = 0;
		int removedCount = 0;
		//從一級拿出來之後
		while(iterator.hasNext()) {
			if (i++ == 200) {
				break; // 為了不要讓 單一 cpu 跑太多迴圈
			}
			job = iterator.next();
			if (groupId.equals(job.getGroupId())) {
				boolean removed = this.queue.remove(job);
				if (removed) {
					removedCount++;
					TPILogger.tl.debug("Job removed: " + job.getId());
				} else {
					// 元素未能被移除，可能需要進行錯誤處理或日誌記錄
					TPILogger.tl.warn("Failed to remove job: " + job.getId());
				}
			}
		}
		TPILogger.tl.info("Removed " + removedCount + " jobs for group: " + groupId);
	}

	public void put(Job job) throws InterruptedException, DgrException {
		job.setTimestamp();
		
	    if (queue.remainingCapacity() == 0) {  // 隊列已滿
	        queue.poll();  // 移除隊首元素
	    }
	    queue.offer(job);  // 添加新元素到隊尾
	    
	}

	public Job take() throws InterruptedException {
		return this.queue.take();
	}

	protected Job peek() {
		return this.queue.peek();
	}

	protected Iterator<Job> iterator() {
		return this.queue.iterator();
	}

	public int size() {
		return this.queue.size();
	}
	
	public void clear() {
		this.queue.clear();
		this.buff1st.clear();
		this.buff2nd.clear();
	}

	public int count(String groupId) {
		AtomicInteger cnt = new AtomicInteger();
		Iterator<Job> iterator = iterator();
		while(iterator.hasNext()) {
			final Job job = iterator.next();
			if (groupId.equals(job.getGroupId())) {
				cnt.addAndGet(1);
			}
		}
		return cnt.get();
	}

	public List<String> dump() {
		List<String> detail = new ArrayList<>();
		// wait for process
		Iterator<Job> it = iterator();
		while (it.hasNext()) {
			Job job = it.next();
			String groupId = job.getGroupId();
			detail.add(String.format("%s", groupId));
		}
		// current process
		Iterator<Map.Entry<String, Long>> it2 = this.jobLogs.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<String, Long> entry = it2.next();
			String groupId = entry.getKey();
			String processTime = DateTimeUtil.dateTimeToString(new Date(entry.getValue()), DateTimeFormatEnum.西元年月日時分秒毫秒_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
			detail.add(String.format("%s; process_time: %s", groupId, processTime));
		}
		return detail;
	}
	
	public void take1stJob() {
		Map.Entry<String, Job> entry = null;
		// 取出第一個元素 (取出 1st job 放入 2nd)
		synchronized (buff1st) {
			Iterator<Map.Entry<String, Job>> itr = buff1st.entrySet().iterator();
			if (itr.hasNext()) {
				entry = itr.next(); //NoSuchElementException 
				itr.remove(); // ConcurrentModificationException
			}
		}
		
		if (entry==null) {return;}
		
		Job valJob = entry.getValue();
		if (valJob instanceof RefreshCacheJob refreshCacheJob) {
			// 放入 2nd 前, 給予 timestamp
			refreshCacheJob.putIn2ndTime = System.currentTimeMillis();
		}
		
		// put 2nd Queue
		synchronized (buff2nd) {
			buff2nd.put(valJob.getGroupId(), valJob);
			buff2nd.notifyAll();
		}
	}

	protected JobHelperImpl getJobHelper() {
		return this.jobHelper;
	}
	
	public void doAgainTake2ndJob() {
		//為了被 override
	}
	
}