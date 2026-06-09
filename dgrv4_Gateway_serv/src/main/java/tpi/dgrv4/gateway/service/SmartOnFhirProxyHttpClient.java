package tpi.dgrv4.gateway.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

/**
 * Smart on FHIR Proxy 專屬的 HTTP 客戶端。
 *
 * <p>使用 {@code java.net.http.HttpClient} 取代 Apache HttpClient（HttpUtil），
 * 提供統一的 HTTP 轉發入口，支援所有 HTTP 方法。</p>
 *
 * <p>效能改進：
 * <ul>
 *   <li>所有 HTTP 方法統一入口（不再 GET/POST/PUT/DELETE/PATCH 分開處理）</li>
 *   <li>Response 以 {@link InputStream} 回傳，支援串流或緩衝</li>
 *   <li>內建 hop-by-hop header 過濾</li>
 *   <li>HttpClient 使用 virtual thread executor，內部連線管理、SSL 交握等 I/O 操作皆在虛擬執行緒上執行</li>
 * </ul>
 */
@Component
public class SmartOnFhirProxyHttpClient {

	/** 不應轉發的 hop-by-hop headers（RFC 2616 Section 13.5.1） */
	private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
		"connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
		"te", "trailer", "transfer-encoding", "upgrade",
		"host", "content-length"
	);

	private final HttpClient httpClient;

	public SmartOnFhirProxyHttpClient() {
		SSLContext sslContext;
		try {
			sslContext = HttpsConnectionChecker.createTrustAllSSLContext();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create trust-all SSLContext", e);
		}

		this.httpClient = HttpClient.newBuilder()
			.sslContext(sslContext)
			.connectTimeout(Duration.ofSeconds(30))
			.followRedirects(HttpClient.Redirect.NEVER)
			.executor(Executors.newVirtualThreadPerTaskExecutor())
			.build();
	}

	/**
	 * 統一轉發入口：所有 HTTP 方法共用。
	 *
	 * <p>回傳 {@link SmartOnFhirProxyResponse}（含 InputStream），
	 * 呼叫端可選擇串流或緩衝消費。</p>
	 *
	 * @param targetUrl      後端完整 URL（含 query string）
	 * @param method         HTTP 方法（GET、POST、PUT、DELETE、PATCH 等）
	 * @param requestHeaders 請求 headers（會自動過濾 hop-by-hop headers）
	 * @param requestBody    請求 body（GET 時可傳 null 或空陣列）
	 * @return proxy response（呼叫端需負責 close）
	 */
	public SmartOnFhirProxyResponse forward(
			String targetUrl,
			String method,
			Map<String, List<String>> requestHeaders,
			byte[] requestBody) throws IOException, InterruptedException {

		HttpResponse<InputStream> resp = sendRequest(targetUrl, method, requestHeaders, requestBody);

		// 若為 scheme-only 升級（http→https，路徑不變），靜默重試一次
		if (isSchemeUpgradeRedirect(resp)) {
			resp.body().close();
			String location = resp.headers().firstValue("location").orElse(null);
			resp = sendRequest(location, method, requestHeaders, requestBody);
		}

		return new SmartOnFhirProxyResponse(
			resp.statusCode(),
			toMutableHeaders(resp.headers().map()),
			resp.body());
	}

	/**
	 * 發送單一 HTTP 請求。
	 */
	private HttpResponse<InputStream> sendRequest(
			String targetUrl, String method,
			Map<String, List<String>> requestHeaders,
			byte[] requestBody) throws IOException, InterruptedException {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(URI.create(targetUrl))
			.timeout(Duration.ofSeconds(60));

		copyHeaders(requestHeaders, builder);

		HttpRequest.BodyPublisher bodyPublisher = (requestBody != null && requestBody.length > 0)
			? HttpRequest.BodyPublishers.ofByteArray(requestBody)
			: HttpRequest.BodyPublishers.noBody();
		builder.method(method.toUpperCase(), bodyPublisher);

		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
	}

	/**
	 * 判斷回應是否為 scheme-only redirect（僅 http↔https，host/path/query 完全不變）。
	 */
	private static boolean isSchemeUpgradeRedirect(HttpResponse<?> resp) {
		int status = resp.statusCode();
		if (status < 300 || status >= 400) {
			return false;
		}
		String location = resp.headers().firstValue("location").orElse(null);
		if (location == null) {
			return false;
		}
		try {
			URI original = resp.request().uri();
			URI target = URI.create(location);
			String s1 = original.getScheme().toLowerCase();
			String s2 = target.getScheme().toLowerCase();
			// scheme 必須不同，且限定 http↔https
			if (s1.equals(s2)) {
				return false;
			}
			if (!("http".equals(s1) && "https".equals(s2))
					&& !("https".equals(s1) && "http".equals(s2))) {
				return false;
			}
			// host、path、query 必須完全相同（port 不比較：scheme 升級本來就會改變預設 port）
			return original.getHost().equalsIgnoreCase(target.getHost())
				&& equals(original.getRawPath(), target.getRawPath())
				&& equals(original.getRawQuery(), target.getRawQuery());
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean equals(String a, String b) {
		return (a == null) ? (b == null) : a.equals(b);
	}

	/**
	 * 複製請求 headers 到 HttpRequest.Builder，跳過 hop-by-hop headers。
	 */
	private void copyHeaders(Map<String, List<String>> headers, HttpRequest.Builder builder) {
		if (headers == null) {
			return;
		}
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			if (key == null || HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
				continue;
			}
			for (String value : entry.getValue()) {
				builder.header(key, value);
			}
		}
	}

	/**
	 * 將 HttpHeaders 的不可變 Map 轉換為可變 Map。
	 * （SmartOnFhirProxyResponseRewriter 需要修改 headers）
	 *
	 * <p>同時過濾 HTTP/2 pseudo-headers（以 ":" 開頭，如 :status、:method 等）。
	 * java.net.http.HttpClient 會將這些協議層 pseudo-headers 放進 headers map，
	 * 但它們不是一般的 HTTP header，不應被轉發到下游回應中。</p>
	 */
	private Map<String, List<String>> toMutableHeaders(Map<String, List<String>> immutableHeaders) {
		Map<String, List<String>> mutable = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : immutableHeaders.entrySet()) {
			String key = entry.getKey();
			if (key != null && !key.startsWith(":")) {
				mutable.put(key, new ArrayList<>(entry.getValue()));
			}
		}
		return mutable;
	}

	/**
	 * Proxy 回應，封裝 status code、headers、body InputStream。
	 *
	 * <p>實作 {@link Closeable} 以確保 InputStream 被正確關閉。
	 * 建議搭配 try-with-resources 使用。</p>
	 */
	public record SmartOnFhirProxyResponse(
			int statusCode,
			Map<String, List<String>> headers,
			InputStream body) implements Closeable {

		@Override
		public void close() throws IOException {
			if (body != null) {
				body.close();
			}
		}
	}
}
