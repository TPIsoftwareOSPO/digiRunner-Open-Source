package tpi.dgrv4.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.function.Supplier;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {


    @Value("${async.max-pool-size:1000}")
    Integer maxPoolSize;

    @Value("${async.highway-pool-size-rate:0.1}")
    Float highwayPoolSize;


    @Value("${async.thread-name-prefix:api-async-workers-}")
    String threadNamePrefix;

    private static final String systemThreadNamePrefix = "system-";

    private static final Supplier<ThreadPoolTaskExecutor> newExecutor = ThreadPoolTaskExecutor::new;

    @Bean(name = "async-workers-healthcheck")
    public ThreadPoolTaskExecutor asyncHealthCheckWorkerExecutor()  {
        var executor = newExecutor.get();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix(systemThreadNamePrefix + "healthcheck-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "async-workers")
    public ThreadPoolTaskExecutor asyncWorkerExecutor()  {
        var executor = newExecutor.get();
        var poolSize = (int) (maxPoolSize - (maxPoolSize * highwayPoolSize));
        var commonWorkerPoolSize = Math.max(poolSize, 1);

        executor.setCorePoolSize(commonWorkerPoolSize);
        executor.setMaxPoolSize(commonWorkerPoolSize);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix(threadNamePrefix + "country-road-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "async-workers-highway")
    public ThreadPoolTaskExecutor asyncHighwayWorkerExecutor()  {
        var executor = newExecutor.get();
        var poolSize = (int) (maxPoolSize * highwayPoolSize);
        var highwaySize = Math.max(poolSize, 1);

        executor.setCorePoolSize(highwaySize);
        executor.setMaxPoolSize(highwaySize);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix(threadNamePrefix + "highway-");
        executor.initialize();
        return executor;
    }
}
