package tpi.dgrv4.grpcproxy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.adaptive.AdaptiveTimeoutManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Initializes adaptive managers at startup
 */
@Component
public class AdaptiveManagerInitializer implements CommandLineRunner {
    private final TPILogger logger = TPILogger.tl;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    // Inject AdaptiveTimeoutManager instance
    private final AdaptiveTimeoutManager timeoutManager;

    /**
     * Inject dependencies using constructor
     *
     * @param timeoutManager Adaptive timeout manager instance
     */
    public AdaptiveManagerInitializer(AdaptiveTimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing adaptive managers");

        // Clean up expired statistics data every 24 hours
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                logger.info("Cleaning up stale method statistics");
                timeoutManager.cleanupStaleMethodStats(); // Use injected instance
            } catch (Exception e) {
                logger.error("Error cleaning up stale method statistics: " + e.getMessage());
            }
        }, 24, 24, TimeUnit.HOURS);

        // Gracefully shutdown scheduler when application closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down adaptive manager scheduler");
            scheduledExecutor.shutdownNow();
        }));

        logger.info("Adaptive managers initialized successfully");
    }
}