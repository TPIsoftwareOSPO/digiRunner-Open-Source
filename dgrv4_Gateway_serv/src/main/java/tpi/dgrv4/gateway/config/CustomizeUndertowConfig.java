package tpi.dgrv4.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.undertow.UndertowOptions;
import tpi.dgrv4.dpaa.vo.UndertowConfigInfo;

@Component
public class CustomizeUndertowConfig implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    private Environment env;
    
    @Autowired
    public CustomizeUndertowConfig(Environment env) {
		super();
		this.env = env;
	}

	@Override
    public void customize(UndertowServletWebServerFactory factory) {
        factory.addBuilderCustomizers(builder -> {
            // 是否啟用 HTTP/2 協議。如果啟用,Server將支援 HTTP/2,提供更好的性能。
            boolean enableHttp2 = true;

            // 請求中允許的最大參數數量。超過此限制的請求將被拒絕,以防止過多的參數導致的性能問題。
            int maxParameters = 10000;

            // 請求中允許的最大Header數量。超過此限制的請求將被拒絕,以防止過多的頭部導致的性能問題。
            int maxHeaders = 2048;

            // 請求中允許的最大 Cookie 數量。超過此限制的請求將被拒絕,以防止過多的 Cookie 導致的性能問題。
            int maxCookies = 2048;

            // 是否允許 URL 中包含非轉義的字符。啟用此選項可能導致安全風險,建議禁用。
            boolean allowUnescapedCharactersInUrl = true;

            // 是否總是設定 Keep-Alive Header。啟用此選項可以提高性能,但可能會占用更多的伺服器資源。
            boolean alwaysSetKeepAlive = false;

            // 是否總是設定 Date Header，啟用此選項可以提供更準確的日期,但可能會略微影響性能。
            boolean alwaysSetDate = true;

            // 是否記錄請求的開始時間。啟用此選項可以幫助進行性能分析和監控。
            boolean recordRequestStartTime = false;

            // 是否允許 Cookie 值中包含等號。啟用此選項可能導致安全風險,建議禁用。
            boolean allowEqualsInCookieValue = true;

            // 是否啟用伺服器的統計功能。啟用此選項可以提供有關伺服器性能和使用情況的統計資訊。
            boolean enableStatistics = false;

            // 服務器監聽的Port number。客戶端需要連接到此端口才能與伺服器通訊。
            int port = factory.getPort();

            // 獲取CPU核心數
            int cpuCores = Runtime.getRuntime().availableProcessors();
			String freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB";
			String totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB";
			String maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB";

            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, enableHttp2);
            builder.setServerOption(UndertowOptions.MAX_PARAMETERS, maxParameters);
            builder.setServerOption(UndertowOptions.MAX_HEADERS, maxHeaders);
            builder.setServerOption(UndertowOptions.MAX_COOKIES, maxCookies);
            builder.setServerOption(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, allowUnescapedCharactersInUrl);
            builder.setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, alwaysSetKeepAlive);
            builder.setServerOption(UndertowOptions.ALWAYS_SET_DATE, alwaysSetDate);
            builder.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, recordRequestStartTime);
            builder.setServerOption(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, allowEqualsInCookieValue);
            builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, enableStatistics);

            // SSL 是否啟用。如果啟用,服務器將支援 HTTPS,提供更安全的通訊。
            boolean sslEnabled = factory.getSsl() != null;
            String sslInfo = sslEnabled ? "Enabled" : "Disabled";
            sslInfo = env.getProperty("server.ssl.enabled");
            

            String cfg = String.format("Customized Undertow configuration:\n"
                            + " - HTTP/2 Enabled: %b\n"
                            + " - SSL: %s\n"
                            + " - HTTP Port: %d\n"
                            + " - Max Parameters: %d\n"
                            + " - Max Headers: %d\n"
                            + " - Max Cookies: %d\n"
                            + " - Allow Unescaped Characters in URL: %b\n"
                            + " - Always Set Keep-Alive: %b\n"
                            + " - Always Set Date: %b\n"
                            + " - Record Request Start Time: %b\n"
                            + " - Allow Equals in Cookie Value: %b\n"
                            + " - Enable Statistics: %b\n"
                            + " - CPU runtime core: %d\n"
                            + " - Memory(free/total/Max): %s / %s / %s\n",
                    enableHttp2, sslInfo, port, maxParameters, maxHeaders, maxCookies,
                    allowUnescapedCharactersInUrl, alwaysSetKeepAlive, alwaysSetDate,
                    recordRequestStartTime, allowEqualsInCookieValue,
                    enableStatistics, cpuCores, freeMemory, totalMemory, maxMemory
            );

            UndertowConfigInfo.setUndertowInfo(cfg);
            System.out.println(cfg);
        });
    }
}