package tpi.dgrv4.gateway.component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.component.cache.proxy.OauthClientDetailsCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART client 認證元件。
 *
 * 背景：SMART App Launch 規範支援多種 client 認證方式（public、client_secret_basic、
 * client_secret_post、private_key_jwt）。本元件根據 DgrSmartClient 的設定，
 * 解析 Authorization header 或 body 參數，選擇對應的認證方式進行驗證。
 *
 * 認證流程：
 * 1. 從 Authorization header（Basic）或 body 參數識別 client_id
 * 2. 查詢 DgrSmartClient 取得該 client 的認證方式設定
 * 3. 依 clientType 分派到對應的驗證邏輯：
 *    - public：不驗密碼，確認 client 存在即可
 *    - confidential-symmetric（client_secret_basic / client_secret_post）：BCrypt 比對密碼
 *    - confidential-asymmetric（private_key_jwt）：JWT assertion 驗簽 + 過期檢查 + JTI 重放防護
 */
@Component
public class SmartClientAuthenticator {

    private static final String BASIC_PREFIX = "Basic ";
    private static final String JWT_BEARER_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    /** exp 上限：5 分鐘（毫秒）。SMART Backend Services §7.1 */
    private static final long MAX_EXP_OFFSET_MS = 5 * 60 * 1000L;

    /** JWKS HTTP 連線 / 讀取 timeout（毫秒） */
    private static final int HTTP_TIMEOUT_MS = 5_000;

    /** 預設 JWKS 快取時間，當 HTTP 回應無 Cache-Control 時使用（秒） */
    private static final long DEFAULT_CACHE_SECONDS = 300L;

    /** JWKS HTTP 回應 body 大小上限（256KB） */
    private static final int MAX_RESPONSE_BYTES = 256 * 1024;

    private final DgrSmartClientDao smartClientDao;
    private final OauthClientDetailsCacheProxy oauthClientDetailsCacheProxy;
    private final SmartJtiStore jtiStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** JWKS 快取：key=jwksUri, value=快取的 JWKSet 與過期時間 */
    private final ConcurrentHashMap<String, CachedJwks> jwksCache = new ConcurrentHashMap<>();

    /** HTTP fetch 策略，可在測試中替換為假實作 */
    private JwksFetcher jwksFetcher = this::defaultHttpFetch;

    @Autowired
    public SmartClientAuthenticator(DgrSmartClientDao smartClientDao,
            OauthClientDetailsCacheProxy oauthClientDetailsCacheProxy,
            SmartJtiStore jtiStore) {
        this.smartClientDao = smartClientDao;
        this.oauthClientDetailsCacheProxy = oauthClientDetailsCacheProxy;
        this.jtiStore = jtiStore;
    }

    /**
     * 替換 HTTP fetch 策略（僅供測試使用）。
     */
    public void setJwksFetcher(JwksFetcher jwksFetcher) {
        this.jwksFetcher = jwksFetcher;
    }

    /**
     * 認證 SMART client。
     *
     * @param authorizationHeader HTTP Authorization header（可為 null）
     * @param params              form body 參數
     * @param tokenEndpointUrl    token 端點完整 URL（private_key_jwt aud 驗證用）
     * @return 認證通過的 client_id
     * @throws SmartAuthenticationException 認證失敗
     */
    public String authenticate(String authorizationHeader, Map<String, String> params, String tokenEndpointUrl) {
        // 1. 判斷認證方式並取得 client_id
        String clientId = resolveClientId(authorizationHeader, params);

        // 2. 查詢 SMART client 設定
        DgrSmartClient smartClient = smartClientDao.findByClientId(clientId)
            .orElseThrow(() -> new SmartAuthenticationException("invalid_client",
                "SMART client not found: " + clientId));

        // 3. 依 clientType 分派驗證
        String clientType = smartClient.getClientType();
        if ("public".equals(clientType)) {
            return clientId;
        }

        String authMethod = smartClient.getTokenEndpointAuthMethod();
        if ("client_secret_basic".equals(authMethod)) {
            verifyClientSecretBasic(clientId, authorizationHeader);
        } else if ("client_secret_post".equals(authMethod)) {
            verifyClientSecretPost(clientId, params);
        } else if ("private_key_jwt".equals(authMethod)) {
            verifyPrivateKeyJwt(clientId, smartClient, params, tokenEndpointUrl);
        } else {
            throw new SmartAuthenticationException("invalid_client",
                "Unsupported token_endpoint_auth_method: " + authMethod);
        }

        return clientId;
    }

    // ==================== Client ID 解析 ====================

    /**
     * 從 Authorization header 或 body 參數中解析 client_id。
     *
     * 優先順序：
     * 1. client_assertion 中的 iss claim
     * 2. Basic Auth header 中的 username
     * 3. body 參數中的 client_id
     */
    private String resolveClientId(String authorizationHeader, Map<String, String> params) {
        // 嘗試從 client_assertion 解析 iss
        String assertionType = params.get("client_assertion_type");
        String assertion = params.get("client_assertion");
        if (StringUtils.hasText(assertion)) {
            if (!JWT_BEARER_TYPE.equals(assertionType)) {
                throw new SmartAuthenticationException("invalid_client",
                    "Invalid client_assertion_type");
            }
            try {
                SignedJWT jwt = SignedJWT.parse(assertion);
                String iss = jwt.getJWTClaimsSet().getIssuer();
                if (StringUtils.hasText(iss)) {
                    return iss;
                }
            } catch (Exception e) {
                throw new SmartAuthenticationException("invalid_client",
                    "Failed to parse client_assertion JWT");
            }
        }

        // 嘗試從 Basic Auth 解析
        if (authorizationHeader != null && authorizationHeader.startsWith(BASIC_PREFIX)) {
            String[] credentials = decodeBasicAuth(authorizationHeader);
            return credentials[0];
        }

        // 嘗試從 body 參數解析
        String clientId = params.get("client_id");
        if (StringUtils.hasText(clientId)) {
            return clientId;
        }

        throw new SmartAuthenticationException("invalid_client",
            "Unable to determine client_id from request");
    }

    // ==================== client_secret_basic ====================

    private void verifyClientSecretBasic(String clientId, String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
            throw new SmartAuthenticationException("invalid_client",
                "Missing Basic Authorization header");
        }

        String[] credentials = decodeBasicAuth(authorizationHeader);
        String secret = credentials[1];

        verifyBcryptSecret(clientId, secret);
    }

    /**
     * 解碼 Basic Auth header，回傳 [client_id, client_secret]。
     */
    private String[] decodeBasicAuth(String authorizationHeader) {
        try {
            String encoded = authorizationHeader.substring(BASIC_PREFIX.length()).trim();
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            int colonIndex = decoded.indexOf(':');
            if (colonIndex < 0) {
                throw new SmartAuthenticationException("invalid_client",
                    "Malformed Basic Authorization header: missing colon separator");
            }
            return new String[] { decoded.substring(0, colonIndex), decoded.substring(colonIndex + 1) };
        } catch (SmartAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartAuthenticationException("invalid_client",
                "Failed to decode Basic Authorization header");
        }
    }

    // ==================== client_secret_post ====================

    private void verifyClientSecretPost(String clientId, Map<String, String> params) {
        String secret = params.get("client_secret");
        if (!StringUtils.hasText(secret)) {
            throw new SmartAuthenticationException("invalid_client",
                "Missing client_secret in request body");
        }

        verifyBcryptSecret(clientId, secret);
    }

    // ==================== BCrypt 共用驗證 ====================

    private void verifyBcryptSecret(String clientId, String rawSecret) {
        Optional<OauthClientDetails> oauthOpt = oauthClientDetailsCacheProxy.findById(clientId);
        if (oauthOpt.isEmpty()) {
            throw new SmartAuthenticationException("invalid_client",
                "OAuth client details not found: " + clientId);
        }

        String storedHash = oauthOpt.get().getClientSecret();
        if (!passwordEncoder.matches(rawSecret, storedHash)) {
            throw new SmartAuthenticationException("invalid_client",
                "Client secret mismatch");
        }
    }

    // ==================== private_key_jwt ====================

    private void verifyPrivateKeyJwt(String clientId, DgrSmartClient smartClient, Map<String, String> params,
            String tokenEndpointUrl) {
        String assertion = params.get("client_assertion");
        if (!StringUtils.hasText(assertion)) {
            throw new SmartAuthenticationException("invalid_client",
                "Missing client_assertion");
        }

        try {
            // 1. 解析 JWT assertion
            SignedJWT signedJWT = SignedJWT.parse(assertion);

            // 2. 從 DgrSmartClient 取得公鑰
            RSAPublicKey publicKey = resolveClientPublicKey(smartClient, signedJWT);

            // 3. 驗簽
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion JWT signature verification failed");
            }

            // 4. 檢查過期（exp 在過去）
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date now = new Date();
            if (claims.getExpirationTime() != null && now.after(claims.getExpirationTime())) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion JWT has expired");
            }

            // 4a. 檢查 exp 不超過 now + 5 分鐘（SMART Backend Services §7.1）
            if (claims.getExpirationTime() != null
                    && claims.getExpirationTime().getTime() - now.getTime() > MAX_EXP_OFFSET_MS) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion exp must not exceed 5 minutes from now");
            }

            // 4b. 驗證 jti 非空且未被使用（重放防護，原子操作）
            String jti = claims.getJWTID();
            if (!StringUtils.hasText(jti)) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion must contain a non-empty jti claim");
            }
            Date jtiExpiration = claims.getExpirationTime() != null
                ? claims.getExpirationTime()
                : new Date(now.getTime() + MAX_EXP_OFFSET_MS);
            if (!jtiStore.markIfUnused(jti, jtiExpiration)) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion jti has already been used: " + jti);
            }

            // 5. 驗證 sub == client_id（RFC 7523 §3）
            if (!clientId.equals(claims.getSubject())) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion sub claim must equal client_id");
            }

            // 6. 驗證 aud 包含 token endpoint URL（RFC 7523 §3）
            List<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(tokenEndpointUrl)) {
                throw new SmartAuthenticationException("invalid_client",
                    "Client assertion aud claim must contain the token endpoint URL");
            }

        } catch (SmartAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.debug("[SMART Auth] private_key_jwt verification failed: " + e.getMessage());
            throw new SmartAuthenticationException("invalid_client",
                "Client assertion verification error: " + e.getMessage());
        }
    }

    /**
     * 從 DgrSmartClient 的 jwks 或 jwksUri 取得 client 公鑰。
     *
     * 優先順序：
     * 1. 有 jwks → 直接解析 JWKSet
     * 2. 有 jwksUri → 查快取，未命中或過期則 HTTP GET fetch，解析 Cache-Control max-age 決定快取時間
     *
     * 若 JWT 有 kid header，嘗試用 kid 比對 JWKS 中的 key；
     * 若沒有 kid 或只有一把 key，取第一把 RSA key。
     */
    private RSAPublicKey resolveClientPublicKey(DgrSmartClient smartClient, SignedJWT signedJWT) throws Exception {
        String jwksJson = smartClient.getJwks();
        String jwksUri = smartClient.getJwksUri();

        if (!StringUtils.hasText(jwksJson) && !StringUtils.hasText(jwksUri)) {
            throw new SmartAuthenticationException("invalid_client",
                "No JWKS configured for client: " + smartClient.getClientId());
        }

        JWKSet jwkSet;

        if (StringUtils.hasText(jwksJson)) {
            // 優先使用 jwks（直接存放）
            jwkSet = JWKSet.parse(jwksJson);
        } else {
            // 從 jwksUri HTTP fetch，使用快取
            jwkSet = fetchJwksWithCache(jwksUri);
        }

        return extractRsaPublicKey(jwkSet, signedJWT);
    }

    /**
     * 從 jwksUri 取得 JWKSet，優先使用快取。
     *
     * @param jwksUri JWKS URL
     * @return JWKSet
     * @throws Exception fetch 或解析失敗
     */
    private JWKSet fetchJwksWithCache(String jwksUri) throws Exception {
        long now = System.currentTimeMillis();

        // 查快取
        CachedJwks cached = jwksCache.get(jwksUri);
        if (cached != null && cached.expireAt() > now) {
            return cached.jwkSet();
        }

        // 快取未命中或過期，執行 HTTP fetch
        FetchResult result = jwksFetcher.fetch(jwksUri);
        if (result.statusCode() != 200) {
            throw new SmartAuthenticationException("invalid_client",
                "Failed to fetch JWKS from " + jwksUri + ": HTTP " + result.statusCode());
        }

        JWKSet jwkSet = JWKSet.parse(result.body());

        // 解析快取時間
        long cacheSeconds = parseCacheMaxAge(result.cacheControl());
        long expireAt = now + cacheSeconds * 1000L;
        jwksCache.put(jwksUri, new CachedJwks(jwkSet, expireAt));

        return jwkSet;
    }

    /**
     * 解析 Cache-Control header 的 max-age 值（秒）。
     * 無 header 或無法解析時回傳預設值 300 秒。
     *
     * @param cacheControl Cache-Control header 值（可為 null）
     * @return max-age 秒數
     */
    private long parseCacheMaxAge(String cacheControl) {
        if (!StringUtils.hasText(cacheControl)) {
            return DEFAULT_CACHE_SECONDS;
        }
        for (String directive : cacheControl.split(",")) {
            String trimmed = directive.trim();
            if (trimmed.startsWith("max-age=")) {
                try {
                    return Long.parseLong(trimmed.substring("max-age=".length()).trim());
                } catch (NumberFormatException e) {
                    return DEFAULT_CACHE_SECONDS;
                }
            }
        }
        return DEFAULT_CACHE_SECONDS;
    }

    /**
     * 預設的 HTTP fetch 實作，使用 HttpURLConnection（connect/read timeout 5 秒）。
     */
    private FetchResult defaultHttpFetch(String jwksUri) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(jwksUri).openConnection();
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int statusCode = conn.getResponseCode();
        String cacheControl = conn.getHeaderField("Cache-Control");

        if (statusCode != 200) {
            return new FetchResult(statusCode, null, cacheControl);
        }

        String body;
        try (java.io.InputStream is = conn.getInputStream()) {
            byte[] data = is.readNBytes(MAX_RESPONSE_BYTES + 1);
            if (data.length > MAX_RESPONSE_BYTES) {
                throw new SmartAuthenticationException("invalid_client",
                    "JWKS response exceeds maximum size of " + MAX_RESPONSE_BYTES + " bytes");
            }
            body = new String(data, StandardCharsets.UTF_8);
        }
        return new FetchResult(statusCode, body, cacheControl);
    }

    /**
     * 從 JWKSet 取得 RSA 公鑰，優先用 kid 比對。
     */
    private RSAPublicKey extractRsaPublicKey(JWKSet jwkSet, SignedJWT signedJWT) throws Exception {
        List<JWK> keys = jwkSet.getKeys();
        if (keys.isEmpty()) {
            throw new SmartAuthenticationException("invalid_client",
                "JWKS contains no keys");
        }

        // 嘗試用 kid 比對
        String kid = signedJWT.getHeader().getKeyID();
        for (JWK jwk : keys) {
            if (jwk instanceof RSAKey) {
                if (kid == null || kid.equals(jwk.getKeyID())) {
                    return ((RSAKey) jwk).toRSAPublicKey();
                }
            }
        }

        // 如果沒找到匹配的 kid，取第一把 RSA key
        for (JWK jwk : keys) {
            if (jwk instanceof RSAKey) {
                return ((RSAKey) jwk).toRSAPublicKey();
            }
        }

        throw new SmartAuthenticationException("invalid_client",
            "No RSA public key found in client JWKS");
    }

    // ==================== 內部型別定義 ====================

    /**
     * HTTP fetch JWKS 的策略介面，允許在測試中替換為假實作。
     */
    @FunctionalInterface
    public interface JwksFetcher {
        FetchResult fetch(String url) throws Exception;
    }

    /**
     * HTTP fetch 的回應結果。
     *
     * @param statusCode   HTTP 狀態碼
     * @param body         回應 body（可為 null，當非 200 時）
     * @param cacheControl Cache-Control header 值（可為 null）
     */
    public record FetchResult(int statusCode, String body, String cacheControl) {}

    /**
     * 快取的 JWKSet 與過期時間。
     *
     * @param jwkSet   JWK Set
     * @param expireAt 過期時間戳（毫秒，System.currentTimeMillis() 格式）
     */
    record CachedJwks(JWKSet jwkSet, long expireAt) {}

    /**
     * SMART client 認證失敗例外。
     */
    public static class SmartAuthenticationException extends RuntimeException {
        private final String error;
        private final String description;

        public SmartAuthenticationException(String error, String description) {
            super(description);
            this.error = error;
            this.description = description;
        }

        public String getError() {
            return error;
        }

        public String getDescription() {
            return description;
        }
    }
}
