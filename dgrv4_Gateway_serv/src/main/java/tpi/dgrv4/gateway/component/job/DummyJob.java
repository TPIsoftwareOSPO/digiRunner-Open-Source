package tpi.dgrv4.gateway.component.job;

import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class DummyJob extends DeferrableJob {

	private long interval;	// in million seconds

	public DummyJob(String groupId, long interval) {
		super(groupId);
		this.interval = interval;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			TPILogger.tl.trace("Dummy job - start (" + this.getId() +")");
			Thread.sleep(this.interval);
			TPILogger.tl.trace("Dummy job - finish (" + this.getId() + ")");
		}catch (InterruptedException e) {
			TPILogger.tl.error("Dummy job exception!");
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		} catch (Exception e) {
			TPILogger.tl.error("Dummy job exception!");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.interval = ((DummyJob) source).getInterval();
	}

	public long getInterval() {
		return this.interval;
	}

}
