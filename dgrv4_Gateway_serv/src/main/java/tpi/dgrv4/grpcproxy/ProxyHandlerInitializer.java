package tpi.dgrv4.grpcproxy;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.adaptive.AdaptiveTimeoutManager;
import tpi.dgrv4.grpcproxy.adaptive.SmartFlowControlManager;
import tpi.dgrv4.grpcproxy.handler.ProxyServerCallHandler;

/**
 * Proxy handler initializer
 * Injects adaptive manager instances into ProxyServerCallHandler when Spring context is refreshed
 */
@Component
public class ProxyHandlerInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final TPILogger logger = TPILogger.tl;
    private final AdaptiveTimeoutManager timeoutManager;
    private final SmartFlowControlManager flowControlManager;

    /**
     * Inject dependencies using constructor
     *
     * @param timeoutManager Adaptive timeout manager
     * @param flowControlManager Smart flow control manager
     */
    public ProxyHandlerInitializer(
            AdaptiveTimeoutManager timeoutManager,
            SmartFlowControlManager flowControlManager) {
        this.timeoutManager = timeoutManager;
        this.flowControlManager = flowControlManager;
    }

    /**
     * Called when Spring context refresh is completed
     *
     * @param event Context refresh event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Injecting adaptive managers into ProxyServerCallHandler");

        // Inject manager instances into ProxyServerCallHandler
        ProxyServerCallHandler.injectManagers(timeoutManager, flowControlManager);

        logger.info("Adaptive managers successfully injected into proxy handler");
    }
}