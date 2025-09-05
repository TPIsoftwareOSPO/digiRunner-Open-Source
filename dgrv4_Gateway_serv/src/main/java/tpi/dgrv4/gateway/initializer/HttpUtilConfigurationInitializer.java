package tpi.dgrv4.gateway.initializer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtilConfiguration;

@Component
@ConditionalOnProperty(
        value = "proxy.enabled",
        havingValue = "true"
)
public class HttpUtilConfigurationInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        var env = event.getApplicationContext().getEnvironment();
        var httpProxy = env.getProperty("proxy.httpProxy");
        var httpsProxy = env.getProperty("proxy.httpsProxy");
        var noProxy = env.getProperty("proxy.noProxy");
        HttpUtil.configuration.setProxyConfig(
                new HttpUtilConfiguration.ProxyConfig(httpProxy, httpsProxy, noProxy)
        );
    }
}
