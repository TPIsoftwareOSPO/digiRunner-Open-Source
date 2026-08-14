package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

/**
 * SMART/OAuth metadata discovery（.well-known）。
 *
 * <p>用於 runtime 解析 token_endpoint / introspection_endpoint：
 * 若呼叫端已提供明確 endpoint 則直接採用；否則抓取設定的 well-known 文件並解析對應欄位。
 * well-known 文件以 URL 為 key 快取（TTL 1 小時）。</p>
 */
@Service
public class SmartProxyDiscoveryService {

	private static final long TTL_MILLIS = 3600_000L; // 1h
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final HttpClient httpClient = buildTrustAllHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

	private record Cached(Map<String, Object> doc, long expireAt) {
	}

	/** 與既有 proxy 轉發一致：trust-all SSLContext（支援自簽憑證 / 內部後端）。 */
	private static HttpClient buildTrustAllHttpClient() {
		try {
			return HttpClient.newBuilder()
					.sslContext(HttpsConnectionChecker.createTrustAllSSLContext())
					.connectTimeout(TIMEOUT)
					.build();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create trust-all SSLContext", e);
		}
	}

	/** 解析後端 auth server 的 token_endpoint。 */
	public String resolveTokenEndpoint(String wellKnownUrl, String explicitEndpoint) {
		if (StringUtils.hasText(explicitEndpoint)) {
			TPILogger.tl.debug("[SMART discovery] using explicit token_endpoint: " + explicitEndpoint);
			return explicitEndpoint;
		}
		return readField(wellKnownUrl, "token_endpoint");
	}

	/** 解析第三方 auth server 的 introspection_endpoint。 */
	public String resolveIntrospectionEndpoint(String wellKnownUrl, String explicitEndpoint) {
		if (StringUtils.hasText(explicitEndpoint)) {
			TPILogger.tl.debug("[SMART discovery] using explicit introspection_endpoint: " + explicitEndpoint);
			return explicitEndpoint;
		}
		return readField(wellKnownUrl, "introspection_endpoint");
	}

	private String readField(String wellKnownUrl, String field) {
		if (!StringUtils.hasText(wellKnownUrl)) {
			TPILogger.tl.error("[SMART discovery] resolve '" + field
					+ "' failed: no explicit endpoint and well-known URL not configured");
			throw new IllegalStateException(
					"discovery failed: no explicit endpoint and well-known URL not configured");
		}
		Map<String, Object> doc = fetchDoc(wellKnownUrl);
		Object v = doc.get(field);
		if (v == null || !StringUtils.hasText(v.toString())) {
			TPILogger.tl.error("[SMART discovery] well-known document missing '" + field + "', url=" + wellKnownUrl
					+ ", available keys=" + doc.keySet());
			throw new IllegalStateException("well-known document missing '" + field + "': " + wellKnownUrl);
		}
		TPILogger.tl.debug("[SMART discovery] resolved '" + field + "' = " + v + " (from " + wellKnownUrl + ")");
		return v.toString();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchDoc(String wellKnownUrl) {
		long now = System.currentTimeMillis();
		Cached c = cache.get(wellKnownUrl);
		if (c != null && now < c.expireAt()) {
			TPILogger.tl.debug("[SMART discovery] well-known cache hit (" + (c.expireAt() - now)
					+ "ms remaining): " + wellKnownUrl);
			return c.doc();
		}
		try {
			HttpRequest req = HttpRequest.newBuilder(URI.create(wellKnownUrl))
					.timeout(TIMEOUT)
					.header("Accept", "application/json")
					.GET()
					.build();
			long start = System.currentTimeMillis();
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			long elapsed = System.currentTimeMillis() - start;
			if (resp.statusCode() / 100 != 2) {
				TPILogger.tl.error("[SMART discovery] well-known fetch HTTP " + resp.statusCode() + " (" + elapsed
						+ "ms), url=" + wellKnownUrl + ", body=" + truncate(resp.body()));
				throw new IllegalStateException("well-known fetch HTTP " + resp.statusCode() + ": " + wellKnownUrl);
			}
			Map<String, Object> doc;
			try {
				doc = objectMapper.readValue(resp.body(), Map.class);
			} catch (Exception pe) {
				TPILogger.tl.error("[SMART discovery] well-known parse failed: " + pe.getMessage() + ", url="
						+ wellKnownUrl + ", body=" + truncate(resp.body()));
				throw pe;
			}
			TPILogger.tl.debug("[SMART discovery] well-known fetched HTTP " + resp.statusCode() + " (" + elapsed
					+ "ms): " + wellKnownUrl + ", keys=" + doc.keySet());
			cache.put(wellKnownUrl, new Cached(doc, now + TTL_MILLIS));
			return doc;
		} catch (IllegalStateException e) {
			throw e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			TPILogger.tl.error("[SMART discovery] well-known fetch interrupted: " + wellKnownUrl);
			throw new IllegalStateException("well-known fetch interrupted: " + wellKnownUrl, e);
		} catch (Exception e) {
			TPILogger.tl.error("[SMART discovery] well-known fetch failed: " + wellKnownUrl + " - " + e.getMessage());
			throw new IllegalStateException("well-known fetch failed: " + wellKnownUrl + " - " + e.getMessage(), e);
		}
	}

	/** log 用：截斷過長的回應內容。 */
	private static String truncate(String s) {
		if (s == null) {
			return "";
		}
		return s.length() > 500 ? s.substring(0, 500) + "...(len=" + s.length() + ")" : s;
	}
}
