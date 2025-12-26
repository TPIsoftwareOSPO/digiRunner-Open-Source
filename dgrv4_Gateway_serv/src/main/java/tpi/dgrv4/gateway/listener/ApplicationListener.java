package tpi.dgrv4.gateway.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil2;

@Configuration
public class ApplicationListener  {

    @Value(value = "${http2.client.poolsize:0}")
    private int poolsize;
    @Value(value = "${mtls.certificate.verification.enabled:false}")
    private Boolean mtlsVerifyEnabled;
    @Value(value = "${http2.client.max.stream:0}")
    private int maxStream;

    @Value(value = "${httpclient.connection.timeout:0}")
    private int connectTimeout;
    @Value(value = "${httpclient.socket.timeout:0}")
    private int socketTimeout;
    @Value(value = "${httpclient.connection.max-per-route:500}")
    private int connectionMaxPerRoute;
    @Value(value = "${httpclient.connection.max-total:2000}")
    private int connectionMaxTotal;
    @Value(value = "${httpclient.idle.cleanup.seconds:60}")
    private int idleCleanupSeconds;

    @Value(value = "${proxy.enabled:false}")
    private String proxyEnabled;

    @Value(value = "${proxy.httpProxy:1.1.1.1}")
    private String httpProxy;
    @Value(value = "${proxy.httpsProxy:1.1.1.1}")
    private String httpsProxy;
    @Value(value = "${proxy.noProxy:*}")
    private String noProxy;

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationEvent(ApplicationStartedEvent event) {
        setHttpUtil2TimeoutAndClientPoolAndMaxStream(connectTimeout, poolsize, maxStream);
        HttpUtil2.setValidityCheck(mtlsVerifyEnabled);

        HttpUtil2.setRequestTimeout(socketTimeout);

        HttpUtil.initialize(configuration -> {
            configuration
                    .connectTimeout(connectTimeout)
                    .connectionMaxPerRoute(connectionMaxPerRoute)
                    .connectionTotalMax(connectionMaxTotal)
                    .socketTimeout(socketTimeout)
                    .idleCleanupSeconds(idleCleanupSeconds)
            ;

            if (Boolean.parseBoolean(proxyEnabled)) {
                configuration.proxyConfig(new HttpUtil.Configuration.ProxyConfig(httpProxy, httpsProxy, noProxy));
            }
        });
        
    }

    private void setHttpUtil2TimeoutAndClientPoolAndMaxStream(int connectTimeout, int poolsize, int maxStream)  {
        if (connectTimeout > 0 ) {
            HttpUtil2.setThreadPoolAndTimeout(connectTimeout);
        }
        if (poolsize > 0) {
            HttpUtil2.setClientPoolSize(poolsize);
        }
        if (maxStream > 0) {
            HttpUtil2.setMaxStreame(maxStream);
        }
    }
}
