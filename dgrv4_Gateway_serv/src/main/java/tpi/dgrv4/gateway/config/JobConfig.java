package tpi.dgrv4.gateway.config;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.cache.core.CacheValueAdapter;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.component.job.DeferrableJobManager;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Configuration
@EnableScheduling
public class JobConfig {

	private TPILogger logger = TPILogger.tl;
	private final ServiceConfig serviceConfig;
	private final GenericCache genericCache;
	private final JobHelperImpl jobHelper;
	
	@Autowired
	public JobConfig(ServiceConfig serviceConfig, GenericCache genericCache, JobHelperImpl jobHelper) {
		super();
		this.serviceConfig = serviceConfig;
		this.genericCache = genericCache;
		this.jobHelper = jobHelper;
	}

	@Bean
	public JobManager mainJobManager() {
		return new JobManager(this.jobHelper);
	}

	@Bean
	public DeferrableJobManager deferrableJobManager() {
		return new DeferrableJobManager(serviceConfig, jobHelper);
	}

	@Bean
	public DeferrableJobManager refreshCacheJobManager() {
		return new DeferrableJobManager(serviceConfig, jobHelper);
	}

	@Bean
	@Scope(value = "prototype")
	public RefreshCacheJob refreshCacheJob(String key, Supplier<?> supplier, CacheValueAdapter adapter) {
		return new RefreshCacheJob(key, supplier, adapter, logger, this.genericCache);
	}

}