package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.RelayConfig;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

/**
 * 後端代取 token（SMART on FHIR Proxy outbound relay）。
 *
 * <p>當 proxy 的請求未帶 token 且設定了後端 client credential 時，
 * 以 OAuth2 client_credentials 向後端 authorization server 換取 access token，
 * 注入後端請求的 Authorization。token 以 (tokenEndpoint|clientId|scope) 為 key 短壽快取。</p>
 *
 * <p>註：依主管要求採 client_secret（client_secret_basic / client_secret_post），
 * 非 SMART Backend Services 規範的 private_key_jwt。</p>
 */
@Service
public class SmartProxyBackendTokenService {

	private static final long DEFAULT_TTL_MILLIS = 300_000L; // 無 expires_in 時的保守 TTL
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final HttpClient httpClient = buildTrustAllHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ConcurrentHashMap<String, CachedToken> cache = new ConcurrentHashMap<>();

	private final TsmpTAEASKHelper taeask;
	private final SmartProxyDiscoveryService discovery;

	@Autowired
	public SmartProxyBackendTokenService(TsmpTAEASKHelper taeask, SmartProxyDiscoveryService discovery) {
		this.taeask = taeask;
		this.discovery = discovery;
	}

	private record CachedToken(String token, long expireAt) {
	}

	record ParsedToken(String accessToken, long expiresInSec) {
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

	/**
	 * 取得後端 access token（快取優先）。失敗丟 IllegalStateException，由呼叫端回 502。
	 */
	public String acquire(RelayConfig cfg) {
		String tokenEndpoint = discovery.resolveTokenEndpoint(cfg.wellKnownUrl(), cfg.tokenEndpoint());
		String scope = cfg.scope();
		String cacheKey = tokenEndpoint + "|" + cfg.clientId() + "|" + (scope == null ? "" : scope);

		long now = System.currentTimeMillis();
		CachedToken cached = cache.get(cacheKey);
		if (cached != null && now < cached.expireAt()) {
			TPILogger.tl.debug("[SMART relay] token cache hit (" + (cached.expireAt() - now)
					+ "ms remaining), endpoint=" + tokenEndpoint + ", clientId=" + safe(cfg.clientId()));
			return cached.token();
		}

		ParsedToken pt = requestToken(cfg, tokenEndpoint);
		long ttl = pt.expiresInSec() > 0 ? pt.expiresInSec() * 1000L : DEFAULT_TTL_MILLIS;
		long margin = Math.min(30_000L, (long) (ttl * 0.1));
		cache.put(cacheKey, new CachedToken(pt.accessToken(), now + ttl - margin));
		TPILogger.tl.debug("[SMART relay] token cached: ttl=" + (ttl - margin) + "ms, endpoint=" + tokenEndpoint
				+ ", clientId=" + safe(cfg.clientId()));
		return pt.accessToken();
	}

	/**
	 * 測試代取（診斷用）：不走快取，實際向後端換一次 token，回傳 token endpoint、完整 token 與有效秒數。失敗丟 IllegalStateException。
	 */
	public TokenProbeResult probe(RelayConfig cfg) {
		String tokenEndpoint = discovery.resolveTokenEndpoint(cfg.wellKnownUrl(), cfg.tokenEndpoint());
		ParsedToken pt = requestToken(cfg, tokenEndpoint);
		return new TokenProbeResult(tokenEndpoint, pt.accessToken(), pt.expiresInSec());
	}

	/** 代取測試結果（診斷用，含完整 token）。 */
	public record TokenProbeResult(String tokenEndpoint, String token, long expiresInSec) {
	}

	/** 實際發出 client_credentials 請求並解析（acquire 與 probe 共用）。 */
	private ParsedToken requestToken(RelayConfig cfg, String tokenEndpoint) {
		String secret = StringUtils.hasText(cfg.clientSecretEnc()) ? taeask.decrypt(cfg.clientSecretEnc()) : "";
		String authMethod = StringUtils.hasText(cfg.authMethod()) ? cfg.authMethod() : "client_secret_basic";

		StringBuilder body = new StringBuilder("grant_type=client_credentials");
		if (StringUtils.hasText(cfg.scope())) {
			body.append("&scope=").append(enc(cfg.scope()));
		}

		HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(tokenEndpoint))
				.timeout(TIMEOUT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Accept", "application/json");

		if ("client_secret_post".equalsIgnoreCase(authMethod)) {
			body.append("&client_id=").append(enc(cfg.clientId()))
					.append("&client_secret=").append(enc(secret));
		} else { // 預設 client_secret_basic
			String basic = Base64.getEncoder().encodeToString(
					(safe(cfg.clientId()) + ":" + secret).getBytes(StandardCharsets.UTF_8));
			rb.header("Authorization", "Basic " + basic);
		}

		HttpRequest req = rb.POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
		TPILogger.tl.debug("[SMART relay] requesting token: endpoint=" + tokenEndpoint + ", clientId="
				+ safe(cfg.clientId()) + ", authMethod=" + authMethod + ", scope=" + safe(cfg.scope()));
		long start = System.currentTimeMillis();
		try {
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			long elapsed = System.currentTimeMillis() - start;
			if (resp.statusCode() / 100 != 2) {
				TPILogger.tl.error("[SMART relay] token request failed: HTTP " + resp.statusCode() + " (" + elapsed
						+ "ms), endpoint=" + tokenEndpoint + ", clientId=" + safe(cfg.clientId())
						+ ", authMethod=" + authMethod + ", body=" + truncate(resp.body()));
				throw new IllegalStateException(
						"token endpoint HTTP " + resp.statusCode() + ": " + truncate(resp.body()));
			}
			try {
				ParsedToken pt = parseTokenResponse(resp.body());
				TPILogger.tl.debug("[SMART relay] token acquired: HTTP " + resp.statusCode() + " (" + elapsed
						+ "ms), endpoint=" + tokenEndpoint + ", token=" + mask(pt.accessToken())
						+ ", expires_in=" + pt.expiresInSec());
				return pt;
			} catch (Exception pe) {
				TPILogger.tl.error("[SMART relay] token response parse failed: " + pe.getMessage()
						+ ", endpoint=" + tokenEndpoint + ", body=" + truncate(resp.body()));
				if (pe instanceof IllegalStateException ise) {
					throw ise;
				}
				throw new IllegalStateException("backend token request failed: " + pe.getMessage(), pe);
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			TPILogger.tl.error("[SMART relay] token request interrupted, endpoint=" + tokenEndpoint);
			throw new IllegalStateException("backend token request interrupted: " + tokenEndpoint, e);
		} catch (Exception e) {
			TPILogger.tl.error("[SMART relay] token request failed: " + e.getMessage()
					+ ", endpoint=" + tokenEndpoint);
			throw new IllegalStateException("backend token request failed: " + e.getMessage(), e);
		}
	}

	/** 解析 token endpoint 回應，取 access_token 與 expires_in。package-private 供測試。 */
	ParsedToken parseTokenResponse(String json) throws Exception {
		Map<?, ?> m = objectMapper.readValue(json, Map.class);
		Object at = m.get("access_token");
		if (at == null || !StringUtils.hasText(at.toString())) {
			throw new IllegalStateException("token response missing access_token");
		}
		long exp = 0;
		Object ei = m.get("expires_in");
		if (ei instanceof Number n) {
			exp = n.longValue();
		} else if (ei != null) {
			try {
				exp = Long.parseLong(ei.toString());
			} catch (NumberFormatException ignore) {
				// 保持 0，套用 DEFAULT_TTL
			}
		}
		return new ParsedToken(at.toString(), exp);
	}

	private static String enc(String s) {
		return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static String truncate(String s) {
		if (s == null) {
			return "";
		}
		return s.length() > 200 ? s.substring(0, 200) : s;
	}

	/** log 用：token 遮罩，只顯示前 8 碼與長度。 */
	private static String mask(String token) {
		if (token == null || token.length() <= 8) {
			return "***";
		}
		return token.substring(0, 8) + "...(len=" + token.length() + ")";
	}
}
