package tpi.dgrv4.gateway.cache;

import java.util.function.Supplier;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.gateway.component.job.DummyJob;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.RefreshCacheJob;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
@CacheConfig(cacheNames = {"dao"})
public class DaoCacheService {

	public static final String CACHE_NAME = "dao";

	private final JobHelper jobHelper;

	@Cacheable(key = "#cacheKey")
	public <ReturnType> ReturnType executeCache(String cacheKey, Supplier<ReturnType> supplier) {
		final String groupId = RefreshCacheJob.GROUP_ID.concat("-").concat(cacheKey);
		DummyJob job = new DummyJob(groupId, 0);
		getJobHelper().add(job);

		return supplier.get();
	}
}
