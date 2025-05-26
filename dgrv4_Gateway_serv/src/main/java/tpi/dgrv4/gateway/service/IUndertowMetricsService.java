package tpi.dgrv4.gateway.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface IUndertowMetricsService {
	public String webserverProperties() ;
	
	public ThreadPoolTaskExecutor getAsyncWorkerPool();
	
	public ThreadPoolTaskExecutor getAsyncWorkerHighwayPool();
}
