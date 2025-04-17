package tpi.dgrv4.gateway.component.job;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class JobHelperImpl implements JobHelper {

	@Autowired
	private TPILogger logger;

	/**
	 * 需要初始Queue的大小，故在 {@link #JobConfig} 使用 {@link Autowired} by name
	 */
	@Autowired
	private JobManager mainJobManager;

	/**
	 * 預設Queue大小為Integer.MAX_VALUE<br>
	 * 因為此類別有兩個 {@link DeferrableJobManager}<br>
	 * 故在 {@link #JobConfig} 使用{@link Autowired} by name
	 */
	@Autowired
	private DeferrableJobManager deferrableJobManager;

	/**
	 * {@link RefreshCacheJob}專用的jobManager<br>
	 * 因為此類別有兩個 {@link DeferrableJobManager}<br>
	 * 故在 {@link #JobConfig} 使用{@link Autowired} by name
	 */
	@Autowired
	private DeferrableJobManager refreshCacheJobManager;
	
	public JobHelperImpl(TPILogger logger) {
		this.logger = logger;
	}
	
	public static boolean STRESS_MODE = true;

	@Override
	public void add(Job job) throws DgrException {
		try {
			
			// 壓測造成太多 RefreshCaheJob 了所以不放入, 這只會影響到快取的更新
			if (JobHelperImpl.STRESS_MODE && getMainJobManager().size() > 100
					&& (job instanceof RefreshCacheJob || job instanceof DummyJob)) {
				return ; 
			}
			
			//System.out.println("排入主要佇列：" + job.getId());
			getMainJobManager().put(job);
		} catch (InterruptedException e) {
			logger.error("JobAdd InterruptedException:\n" + StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * [1] = MainJobManager.size<br/>
	 * [2] = DeferrableJobManager.size<br/>
	 * [3] = RefreshCacheJobManager.size<br/>
	 * @param types
	 * @return
	 */
	@Override
	public long getJobQueueSize(int...types) {
		long sum = 0;
		if (types != null && types.length > 0) {
			for (int type : types) {
				if (type == 1) {
					sum += getMainJobManager().size();
				} else if (type == 2) {
//					sum += getDeferrableJobManager().size();
					sum += getDeferrableJobManager().size() + getDeferrableJobManager().buff1st.size() + 
							getDeferrableJobManager().buff2nd.size();
				} else if (type == 3) {
//					sum += getRefreshCacheJobManager().size();
					sum += getDeferrableJobManager().buff1st.size() + 
							getDeferrableJobManager().buff2nd.size() + 
							getDeferrableJobManager().size();
				}
			}
		}
		return sum;
	}
	
	@Override
	public String getQueueStatus(int type) {
		String status = "(";
		if (type == 1) {
			status += getMainJobManager().size() + ", ";
			status += getMainJobManager().buff1st.size() + ", ";
			status += getMainJobManager().buff2nd.size() + ")";
		} else if (type == 2) {
			status += getDeferrableJobManager().size() + ", ";
			status += getDeferrableJobManager().buff1st.size() + ", ";
			status += getDeferrableJobManager().buff2nd.size() + ")";
		} else if (type == 3) {
			status += getRefreshCacheJobManager().size() + ", ";
			status += getRefreshCacheJobManager().buff1st.size() + ", ";
			status += getRefreshCacheJobManager().buff2nd.size() + ")";
		}
		clear3Q();
		return status;
	}
	
	@Override
	public void clear3Q() {
		this.mainJobManager.clear();
		this.deferrableJobManager.clear();
		this.refreshCacheJobManager.clear();
	}

	@Override
	public List<String> getJobQueueDump(int type) {
		if (type == 1) {
			return getMainJobManager().dump();
		} else if (type == 2) {
//			return getDeferrableJobManager().dump();
			List<String> list = new LinkedList<String>();
			for (Map.Entry<String, Job> entry : getDeferrableJobManager().buff2nd.entrySet()) {
				Job job = entry.getValue();
				list.add(job.getGroupId() + " - " + job.getClass().getSimpleName());
			}
			return list;
		} else if (type == 3) {
//			return getRefreshCacheJobManager().dump();
			List<String> list = new LinkedList<String>();
			for (Map.Entry<String, Job> entry : getRefreshCacheJobManager().buff2nd.entrySet()) {
				Job job = entry.getValue();
				list.add(job.getGroupId() + " - " + job.getClass().getSimpleName());
			}
			return list;
		}
		return Collections.emptyList();
	}

	ExecutorService executor1st = Executors.newSingleThreadExecutor(r -> {
	    Thread thread = new Thread(r);
	    thread.setName("Job-1st-runner");
	    return thread;
	});
	protected void take1stJob(DeferrableJobManager jm) {
		executor1st.execute(() -> {
			jm.take1stJob();
			//take1stJob__(jm.buff1st, jm.buff2nd);
			// 觸發 2nd take
			jm.take2ndJob();
		});
	}


	public void defer(DeferrableJob job) {
		DeferrableJobManager jm = getDeferrableJobManagerByType(job);
		synchronized (jm.buff1st) {
			jm.buff1st.put(job.getGroupId(), job);
		}
		
		// take Job 只有一個 Thread 在取用
		take1stJob(jm);
		
		
		// replace 2級 or 放入
//		try {
//			//System.out.print("排入延遲佇列：" + job.getId());
//			// 取代延遲佇列中相同GroupId的工作內容
//			DeferrableJobManager jm = getDeferrableJobManagerByType(job);
//			final int replaceCnt = jm.replaceByGroupId(job);
//			if (replaceCnt == 0) {
//				jm.put(job);
//			} else {
//				this.logger.trace("[#JOB#]A deferrable job has replaced " + replaceCnt + " same job(s) in deferrable queue: " + job.getGroupId());
//			}
//			//System.out.println(" OK");
//		} catch (InterruptedException e) {
//			logger.error("JobAdd InterruptedException:\n" + StackTraceUtil.logStackTrace(e));
//			Thread.currentThread().interrupt();
//		}
	}

	private DeferrableJobManager getDeferrableJobManagerByType(DeferrableJob job) {
		if (job instanceof RefreshCacheJob || job instanceof DummyJob) {
			return getRefreshCacheJobManager();
		}
		return getDeferrableJobManager();
	}
	

	protected JobManager getMainJobManager() {
		return this.mainJobManager;
	}

	protected DeferrableJobManager getDeferrableJobManager() {
		return this.deferrableJobManager;
	}

	protected DeferrableJobManager getRefreshCacheJobManager() {
		return this.refreshCacheJobManager;
	}

}