package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.IntrospectConfig;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

/**
 * 入站 third-party token 驗證（RFC 7662 introspection）。
 *
 * <p>依主管要求採 introspection 外呼（非本地 jwks 驗章）。驗證項：active、exp、aud（若回應有提供）。
 * 信任模型：先以 issuer 白名單前置過濾（JWT token 才能取 iss；opaque token 則依賴設定的
 * introspection_endpoint 本身即受信任 AS）。active 結果短快取至 token exp 或 +60s（取小）。</p>
 */
@Service
public class SmartProxyInboundIntrospector {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final long ACTIVE_CACHE_CAP_MILLIS = 60_000L;

	private final HttpClient httpClient = buildTrustAllHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	/** tokenHash → active 結果有效到期時間(ms) */
	private final ConcurrentHashMap<String, Long> activeCache = new ConcurrentHashMap<>();

	private final TsmpTAEASKHelper taeask;
	private final SmartProxyDiscoveryService discovery;

	@Autowired
	public SmartProxyInboundIntrospector(TsmpTAEASKHelper taeask, SmartProxyDiscoveryService discovery) {
		this.taeask = taeask;
		this.discovery = discovery;
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
	 * 驗證入站 token。
	 *
	 * @return null 表示通過；非 null 為失敗原因（呼叫端回 401 invalid_token）
	 */
	public String validate(IntrospectConfig cfg, String bearerToken, String resourceType) {
		if (!StringUtils.hasText(bearerToken)) {
			return "missing access token";
		}

		// 1. issuer 白名單前置過濾（僅 JWT 可取 iss；opaque 交由受信任 endpoint 把關）
		List<String> allowed = cfg.allowedIssuers();
		if (allowed != null && !allowed.isEmpty()) {
			String iss = tryParseIssuer(bearerToken);
			if (iss != null && !allowed.contains(iss)) {
				TPILogger.tl.warn("[SMART introspect] issuer rejected: iss=" + iss + ", allowed=" + allowed);
				return "issuer not allowed: " + iss;
			}
		}

		// 2. active 結果短快取
		String hash = sha256(bearerToken);
		long now = System.currentTimeMillis();
		Long cachedUntil = activeCache.get(hash);
		if (cachedUntil != null && now < cachedUntil) {
			TPILogger.tl.debug("[SMART introspect] active cache hit (" + (cachedUntil - now)
					+ "ms remaining), tokenHash=" + hashPrefix(hash));
			return null;
		}

		// 3. 解析 introspection endpoint
		String endpoint;
		try {
			endpoint = discovery.resolveIntrospectionEndpoint(cfg.wellKnownUrl(), cfg.introspectionEndpoint());
		} catch (Exception e) {
			TPILogger.tl.error("[SMART introspect] endpoint resolve failed: " + e.getMessage()
					+ ", wellKnown=" + cfg.wellKnownUrl());
			return "introspection endpoint resolve failed: " + e.getMessage();
		}

		// 4. 外呼 introspection
		Map<String, Object> resp;
		try {
			resp = callIntrospect(endpoint, cfg, bearerToken);
		} catch (Exception e) {
			TPILogger.tl.error("[SMART introspect] introspection call failed: " + e.getMessage()
					+ ", endpoint=" + endpoint + ", tokenHash=" + hashPrefix(hash));
			return "introspection call failed: " + e.getMessage();
		}

		// 5. active
		Object active = resp.get("active");
		if (!(active instanceof Boolean) || !((Boolean) active)) {
			TPILogger.tl.warn("[SMART introspect] token inactive: active=" + active
					+ ", tokenHash=" + hashPrefix(hash));
			return "token inactive";
		}

		// 6. exp
		Long tokenExpMillis = null;
		Object expClaim = resp.get("exp");
		if (expClaim instanceof Number n) {
			tokenExpMillis = n.longValue() * 1000L;
			if (now >= tokenExpMillis) {
				TPILogger.tl.warn("[SMART introspect] token expired: exp=" + expClaim
						+ ", tokenHash=" + hashPrefix(hash));
				return "token expired";
			}
		}

		// 7. aud（RFC7662 不保證帶 aud；有則強制，無則無法強制，依賴 issuer 專屬本 RS 的設定假設）
		if (StringUtils.hasText(cfg.expectedAud())) {
			Object aud = resp.get("aud");
			if (aud != null) {
				if (!audMatches(aud, cfg.expectedAud())) {
					TPILogger.tl.warn("[SMART introspect] aud mismatch: expected=" + cfg.expectedAud()
							+ ", actual=" + aud + ", tokenHash=" + hashPrefix(hash));
					return "aud mismatch";
				}
			} else {
				TPILogger.tl.debug("[SMART introspect] response has no aud; cannot enforce expectedAud="
						+ cfg.expectedAud());
			}
		}

		// 8. 快取 active 結果
		long until = (tokenExpMillis != null)
				? Math.min(tokenExpMillis, now + ACTIVE_CACHE_CAP_MILLIS)
				: now + ACTIVE_CACHE_CAP_MILLIS;
		activeCache.put(hash, until);
		TPILogger.tl.debug("[SMART introspect] token active, cached for " + (until - now)
				+ "ms, tokenHash=" + hashPrefix(hash));
		return null;
	}

	private Map<String, Object> callIntrospect(String endpoint, IntrospectConfig cfg, String token) throws Exception {
		// 預設：有設定 client_id 時為 client_secret_basic，未設定時為 none（避免送出空 Basic 認證）
		String defaultAuth = StringUtils.hasText(cfg.clientId()) ? "client_secret_basic" : "none";
		String authMethod = StringUtils.hasText(cfg.introspectionAuth()) ? cfg.introspectionAuth() : defaultAuth;
		String body = "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

		HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(endpoint))
				.timeout(TIMEOUT)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Accept", "application/json");

		if ("client_secret_basic".equalsIgnoreCase(authMethod)) {
			String secret = StringUtils.hasText(cfg.clientSecretEnc()) ? taeask.decrypt(cfg.clientSecretEnc()) : "";
			String basic = Base64.getEncoder().encodeToString(
					(safe(cfg.clientId()) + ":" + secret).getBytes(StandardCharsets.UTF_8));
			rb.header("Authorization", "Basic " + basic);
		} else if ("bearer".equalsIgnoreCase(authMethod)) {
			String secret = StringUtils.hasText(cfg.clientSecretEnc()) ? taeask.decrypt(cfg.clientSecretEnc()) : "";
			rb.header("Authorization", "Bearer " + secret);
		}
		// none → 不帶認證

		TPILogger.tl.debug("[SMART introspect] calling introspection: endpoint=" + endpoint
				+ ", authMethod=" + authMethod);
		long start = System.currentTimeMillis();
		HttpResponse<String> resp = httpClient.send(
				rb.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
				HttpResponse.BodyHandlers.ofString());
		long elapsed = System.currentTimeMillis() - start;
		if (resp.statusCode() / 100 != 2) {
			// 對外例外訊息僅帶狀態碼；回應內容只進 log
			TPILogger.tl.error("[SMART introspect] introspection HTTP " + resp.statusCode() + " (" + elapsed
					+ "ms), endpoint=" + endpoint + ", body=" + truncate(resp.body()));
			throw new IllegalStateException("introspection HTTP " + resp.statusCode());
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = objectMapper.readValue(resp.body(), Map.class);
			TPILogger.tl.debug("[SMART introspect] introspection HTTP " + resp.statusCode() + " (" + elapsed
					+ "ms), endpoint=" + endpoint + ", response=" + truncate(resp.body()));
			return m;
		} catch (Exception pe) {
			TPILogger.tl.error("[SMART introspect] introspection response parse failed: " + pe.getMessage()
					+ ", endpoint=" + endpoint + ", body=" + truncate(resp.body()));
			throw pe;
		}
	}

	/** aud 可能為 String 或 array，命中其一即可。package-private 供測試。 */
	boolean audMatches(Object aud, String expected) {
		if (aud instanceof String s) {
			return s.equals(expected);
		}
		if (aud instanceof List<?> list) {
			for (Object o : list) {
				if (expected.equals(String.valueOf(o))) {
					return true;
				}
			}
		}
		return false;
	}

	/** 解析 JWT 的 iss；非 JWT 或失敗回 null。package-private 供測試。 */
	String tryParseIssuer(String token) {
		try {
			return SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
		} catch (Exception e) {
			return null;
		}
	}

	private static String sha256(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			return s;
		}
	}

	/** log 用：token 雜湊前 12 碼，供同一 token 的多筆 log 關聯，不落原文。 */
	private static String hashPrefix(String hash) {
		return (hash != null && hash.length() > 12) ? hash.substring(0, 12) : String.valueOf(hash);
	}

	/** log 用：截斷過長的回應內容。 */
	private static String truncate(String s) {
		if (s == null) {
			return "";
		}
		return s.length() > 500 ? s.substring(0, 500) + "...(len=" + s.length() + ")" : s;
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}
}
