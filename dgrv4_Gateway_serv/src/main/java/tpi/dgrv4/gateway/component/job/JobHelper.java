package tpi.dgrv4.gateway.component.job;

import java.util.List;

import tpi.dgrv4.entity.exceptions.DgrException;

public interface JobHelper {

	public void add(Job job) throws DgrException;

	public long getJobQueueSize(int...types);
	
	default void clear3Q() {}
	
	default String getQueueStatus(int type) {return null;}

	public List<String> getJobQueueDump(int type);

}