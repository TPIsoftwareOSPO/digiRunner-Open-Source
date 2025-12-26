package tpi.dgrv4.gateway.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class CustomizeTomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${digi.server.request.idle-timeout.ms:60000}")
    Integer digiServerRequestTimeoutMs;

    @Value("${server.ssl.enabled:false}")
    Boolean serverSslEnabled;

    @Value("${server.http2.enable:true}")
    Boolean serverHttp2Enabled;

    private ExecutorService virtualThreadExecutor;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        // 配置 Virtual Thread Executor
        virtualThreadExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("VIO-", 1)
                        .factory());

        factory.addConnectorCustomizers(connector -> {
            // HTTP/2 支援（需要在 application.properties 中設定 server.http2.enable=true）
            if (serverHttp2Enabled) {
                connector.addUpgradeProtocol(new Http2Protocol());
            }

            ProtocolHandler protocolHandler = connector.getProtocolHandler();

            if (protocolHandler instanceof AbstractHttp11Protocol<?>) {
                AbstractHttp11Protocol<?> http11Protocol = (AbstractHttp11Protocol<?>) protocolHandler;

                // 設定 Virtual Thread Executor
                http11Protocol.setExecutor(virtualThreadExecutor);

                // 請求中允許的最大 Header 數量
                http11Protocol.setMaxHeaderCount(2048);

                // 設定連線超時時間（毫秒）
                http11Protocol.setConnectionTimeout(digiServerRequestTimeoutMs);

                // Keep-Alive 相關設定
                http11Protocol.setKeepAliveTimeout(digiServerRequestTimeoutMs);
                http11Protocol.setMaxKeepAliveRequests(100);

                // 允許 URL 中的特殊字符（對應 ALLOW_UNESCAPED_CHARACTERS_IN_URL）
                connector.setProperty("relaxedQueryChars", "[]|{}^&#x5c;&#x60;&quot;&lt;&gt;");
                connector.setProperty("relaxedPathChars", "[]|");
            }

            // 最大 Cookie 數量（Tomcat 預設無限制，可透過 maxCookieCount 設定）
            connector.setProperty("maxCookieCount", "2048");

            // 允許 Cookie 值中包含等號
            connector.setProperty("allowEqualsInCookieValue", "true");
        });

        // 輸出配置資訊
        int port = factory.getPort();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        String freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB";
        String totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB";
        String maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB";

        var serverInfo = """
                Customized Tomcat configuration:
                    - HTTP/2 Enabled: %b (set via server.http2.enable)
                    - SSL Enabled: %b
                    - HTTP Port: %d
                    - Max Parameters: %d
                    - Max Headers: %d
                    - Max Cookies: %d
                    - Allow Special Characters in URL: true
                    - Keep-Alive Timeout: %d ms
                    - Allow Equals in Cookie Value: true
                    - CPU runtime core: %d
                    - Memory(free/total/Max): %s / %s / %s
                    - Connection Timeout: %d ms
                """.formatted(
                serverHttp2Enabled, // HTTP/2 需透過 application.properties 啟用
                serverSslEnabled,
                port,
                10000, // maxParameters
                2048, // maxHeaders
                2048, // maxCookies
                digiServerRequestTimeoutMs,
                cpuCores,
                freeMemory,
                totalMemory,
                maxMemory,
                digiServerRequestTimeoutMs);

        // TomcatConfigInfo.setTomcatInfo(serverInfo);
        System.out.println(serverInfo);
    }

    @PreDestroy
    public void cleanup() {
        if (virtualThreadExecutor != null) {
            virtualThreadExecutor.shutdown();
        }
    }
}
