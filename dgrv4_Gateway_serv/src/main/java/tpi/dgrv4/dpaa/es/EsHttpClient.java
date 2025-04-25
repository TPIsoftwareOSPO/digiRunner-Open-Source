package tpi.dgrv4.dpaa.es;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 提供了連接池和 keep-alive 機制,適合高併發場景。
 * 
 * 使用方式:
 * EsHttpClient client = EsHttpClient.getInstance();
 * int httpCode = client.bulkWrite("http://es:9200/_bulk", bulkBody, headers);
 */
public class EsHttpClient {
	// Double-Checked Locking mode has been implemented, but SonarQube may not fully understand the context and issue a warning, so annotate
	// 已經實作雙重檢查鎖定(Double-Checked Locking)模式,但SonarQube可能無法完全理解上下文發出警告,故加註解
	@SuppressWarnings("java:S3077")
    private static volatile EsHttpClient instance;
    
    private final CloseableHttpClient httpClient;
    
    // 將構造函數改為私有
    private EsHttpClient() {
        SSLContextBuilder builder = new SSLContextBuilder();
        Registry<ConnectionSocketFactory> registry = null;
        try {
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build(), NoopHostnameVerifier.INSTANCE);
            
            registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
        }
        
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(4);
        cm.setDefaultMaxPerRoute(2);
        
        httpClient = HttpClients.custom()
            .setConnectionManager(cm)
            .setKeepAliveStrategy((response, context) -> 30 * 1000) // 30秒
            .build();
    }
    
	// Double-Checked Locking mode
	// 雙重檢查鎖定(Double-Checked Locking)模式
    // 獲取實例的方法
    public static EsHttpClient getInstance() {
        if (instance == null) {
            synchronized (EsHttpClient.class) {
                if (instance == null) {
                    instance = new EsHttpClient();
                }
            }
        }
        return instance;
    }
    
    public int bulkWrite(String esUrl, String bulkBody, Map<String, String> headers, Float diskFreeThreshHold, int deletePercent, boolean allowWriteElastic) throws IOException {
       
        // 第一次使用時，需要初始化（通常在應用啟動時）
        ESLogBuffer logBuffer = getESLogBuffer(diskFreeThreshHold, deletePercent, allowWriteElastic);
        // 使用緩衝器寫入日誌
        return logBuffer.bulkWrite(esUrl, bulkBody, headers);
    }
    
    public ESLogBuffer getESLogBuffer(Float diskFreeThreshHold, int deletePercent, boolean allowWriteElastic) {
    	return ESLogBuffer.getInstance(httpClient, diskFreeThreshHold, deletePercent, allowWriteElastic);
    }
}