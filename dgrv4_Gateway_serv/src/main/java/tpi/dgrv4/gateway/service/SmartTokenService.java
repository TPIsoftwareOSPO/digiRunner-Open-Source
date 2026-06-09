package tpi.dgrv4.gateway.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.net.URI;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrSmartAuthSessionDao;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.component.OIDCWellKnownHelper;
import tpi.dgrv4.gateway.component.SmartKeyProvider;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.constant.DgrTokenVersion;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART token 端點的授權碼交換服務。
 *
 * 背景：SMART App Launch 的 token endpoint 處理 authorization_code grant，
 * 驗證 auth code、PKCE S256、redirect_uri、client_id 後，
 * 用 SmartKeyProvider 的 RSA 私鑰簽發 RS256 JWT access token。
 *
 * 設計決策：
 * - JWT scope claim 是 JSON array（不是空格分隔字串），與 SmartOnFhirTokenClaims 一致
 * - token response body 的 scope 是空格分隔字串（RFC 6749 §3.3）
 * - baseUrl 由呼叫方（Controller）從 HTTP 請求動態解析後傳入
 * - auth code 一次性：交換後 session.phase 改為 EXCHANGED

 */
@Service
public class SmartTokenService {

    private static final int ACCESS_TOKEN_LIFETIME_SECONDS = 3600;
    private static final int BACKEND_SERVICES_TOKEN_LIFETIME_SECONDS = 300;
    private static final long REFRESH_TOKEN_LIFETIME_MS = 30L * 24 * 3600 * 1000; // 30 天

    private final SecureRandom secureRandom = new SecureRandom();

    private final DgrSmartAuthSessionDao sessionDao;
    private final DgrSmartClientDao clientDao;
    private final SmartKeyProvider keyProvider;
    private final TokenHelper tokenHelper;
    private final TsmpSettingService tsmpSettingService;

    @Autowired
    public SmartTokenService(DgrSmartAuthSessionDao sessionDao, DgrSmartClientDao clientDao,
            SmartKeyProvider keyProvider, TokenHelper tokenHelper, TsmpSettingService tsmpSettingService) {
        this.sessionDao = sessionDao;
        this.clientDao = clientDao;
        this.keyProvider = keyProvider;
        this.tokenHelper = tokenHelper;
        this.tsmpSettingService = tsmpSettingService;
    }

    /**
     * 交換授權碼為 JWT access token。
     *
     * @param authCode     授權碼
     * @param codeVerifier PKCE code_verifier（可為 null，但若 session 有 code_challenge 則必填）
     * @param redirectUri  必須與授權時的 redirect_uri 一致
     * @param clientId     必須與授權時的 client_id 一致
     * @param baseUrl      JWT issuer（從 HTTP 請求動態解析）
     * @return token response Map（access_token、token_type、expires_in、scope、patient、encounter）
     * @throws SmartTokenException 驗證失敗
     */
    @Transactional
    public Map<String, Object> exchangeAuthorizationCode(String authCode, String codeVerifier,
            String redirectUri, String clientId, String baseUrl) {

        // 1. 查詢 session
        DgrSmartAuthSession session = sessionDao.findByAuthCode(authCode)
            .orElseThrow(() -> new SmartTokenException("invalid_grant", "Authorization code not found"));

        // 2. 驗證 phase 必須為 APPROVED
        if (!"APPROVED".equals(session.getPhase())) {
            throw new SmartTokenException("invalid_grant", "Authorization code has already been used");
        }

        // 3. 驗證 auth code 是否過期
        if (session.getAuthCodeExpire() != null && System.currentTimeMillis() > session.getAuthCodeExpire()) {
            throw new SmartTokenException("invalid_grant", "Authorization code has expired");
        }

        // 4. 驗證 client_id
        if (!session.getClientId().equals(clientId)) {
            throw new SmartTokenException("invalid_grant", "Client ID mismatch");
        }

        // 5. 驗證 redirect_uri
        if (!session.getRedirectUri().equals(redirectUri)) {
            throw new SmartTokenException("invalid_grant", "Redirect URI mismatch");
        }

        // 6. PKCE S256 驗證
        verifyPkce(session, codeVerifier);

        // 6.5 錯誤模擬：token_* 系列
        injectSimulatedTokenError(session.getSimError());

        // 7. 簽發 JWT access token
        String accessToken = buildJwtAccessToken(session);

        // 8. 更新 session phase 為 EXCHANGED；若 scope 含 offline_access 則簽發 refresh token
        session.setPhase("EXCHANGED");
        if (hasScopeToken(session.getGrantedScope(), "offline_access")) {
            issueRefreshToken(session);
        }
        sessionDao.save(session);

        // 9. 組裝 token response
        Map<String, Object> response = buildTokenResponse(session, accessToken, session.getGrantedScope(), baseUrl);

        // 10. 寫入 TSMP_TOKEN_HISTORY，讓 checkAccessTokenRevoked 查得到這筆 token
        writeSmartTokenHistory(session.getUserName(), session.getClientId(), accessToken,
                String.join(" ", session.getGrantedScope()), (String) response.get("id_token"),
                session.getRefreshToken(), session.getRefreshTokenExpire());

        return response;
    }

    // ==================== Refresh Token Grant ====================

    /**
     * 處理 refresh_token grant，驗證並輪換 refresh token，簽發新 access token。
     *
     * @param refreshTokenValue 用戶端提供的 refresh token
     * @param clientId          用戶端 ID（必須與 session 一致）
     * @param requestedScope    請求的 scope（null 或空字串時沿用 grantedScope，不可超出原始範圍）
     * @param baseUrl           JWT issuer（從 HTTP 請求動態解析）
     * @return token response Map（含新 access_token、新 refresh_token）
     * @throws SmartTokenException 驗證失敗
     */
    @Transactional
    public Map<String, Object> refreshToken(String refreshTokenValue, String clientId, String requestedScope,
            String baseUrl) {

        // 1. 查 session
        DgrSmartAuthSession session = sessionDao.findByRefreshToken(refreshTokenValue)
            .orElseThrow(() -> new SmartTokenException("invalid_grant", "Refresh token not found"));

        // 2. 驗過期
        if (session.getRefreshTokenExpire() == null
                || System.currentTimeMillis() > session.getRefreshTokenExpire()) {
            throw new SmartTokenException("invalid_grant", "Refresh token has expired");
        }

        // 3. 驗 client_id
        if (!session.getClientId().equals(clientId)) {
            throw new SmartTokenException("invalid_grant", "Client ID mismatch");
        }

        // 4. 驗 scope 是 grantedScope 的子集
        List<String> effectiveScope;
        if (StringUtils.hasText(requestedScope)) {
            validateScopeSubset(requestedScope, session.getGrantedScope());
            effectiveScope = List.of(requestedScope.trim().split("\\s+"));
        } else {
            effectiveScope = session.getGrantedScope();
        }

        // 4.5 錯誤模擬
        injectSimulatedRefreshError(session.getSimError());

        // 5. 簽發新 JWT access token（使用 effectiveScope 覆蓋）
        String accessToken = buildJwtAccessTokenWithScope(session, effectiveScope);

        // 6. Rotation：產生新 refresh token，覆蓋舊值
        issueRefreshToken(session);

        // 7. 儲存 session
        sessionDao.save(session);

        // 8. 組裝 response
        Map<String, Object> response = buildTokenResponse(session, accessToken, effectiveScope, baseUrl);

        // 9. 寫入 TSMP_TOKEN_HISTORY
        writeSmartTokenHistory(session.getUserName(), session.getClientId(), accessToken,
                String.join(" ", effectiveScope), (String) response.get("id_token"),
                session.getRefreshToken(), session.getRefreshTokenExpire());

        return response;
    }

    // ==================== Client Credentials Grant ====================

    /**
     * 處理 client_credentials grant，為 Backend Service 簽發 system/ 層級的 JWT access token。
     *
     * 規格依據：SMART Backend Services（HL7 SMART App Launch STU2.2 §Backend Services）
     *
     * @param clientId 用戶端 ID（已通過 SmartClientAuthenticator 驗證）
     * @param scope    請求的 scope（空格分隔，僅允許 system/ 開頭）
     * @param baseUrl  JWT issuer（從 HTTP 請求動態解析）
     * @return token response Map（access_token、token_type、expires_in、scope）
     * @throws SmartTokenException scope 不合法或 clientType 不符
     */
    public Map<String, Object> issueClientCredentialsToken(String clientId, String scope,
            String baseUrl) {

        // 1. scope 不可為空
        if (!StringUtils.hasText(scope)) {
            throw new SmartTokenException("invalid_request", "Missing required parameter: scope");
        }

        // 2. 查詢 client 設定
        DgrSmartClient client = clientDao.findByClientId(clientId)
            .orElseThrow(() -> new SmartTokenException("invalid_client", "Client not found: " + clientId));

        // 3. 驗證 clientType 必須為 confidential-asymmetric
        if (!"confidential-asymmetric".equals(client.getClientType())) {
            throw new SmartTokenException("unauthorized_client",
                "client_credentials grant requires confidential-asymmetric client");
        }

        // 4. 驗證每個 scope token：只允許 system/ 開頭
        String[] scopeTokens = scope.split("\\s+");
        for (String token : scopeTokens) {
            validateSystemScopeToken(token);
        }

        // 5. 驗證 scope 在 allowedScopes 範圍內
        Set<String> allowed = Set.copyOf(client.getAllowedScopes());
        for (String token : scopeTokens) {
            if (!allowed.contains(token)) {
                throw new SmartTokenException("invalid_scope",
                    "Requested scope is not in allowed scopes: " + token);
            }
        }

        // 6. 簽發 JWT access token
        String accessToken = buildBackendServicesJwt(clientId, scope, baseUrl);

        // 7. 組裝 response（不含 refresh_token、patient、encounter）
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", BACKEND_SERVICES_TOKEN_LIFETIME_SECONDS);
        response.put("scope", scope);

        // 8. 寫入 TSMP_TOKEN_HISTORY
        writeSmartTokenHistory(clientId, clientId, accessToken, scope, null, null, null);

        return response;
    }

    /**
     * 驗證單一 scope token 是否符合 Backend Services 規則。
     * 只允許 system/ 開頭；明確拒絕 patient/、user/、launch、launch/、offline_access。
     */
    private void validateSystemScopeToken(String token) {
        if (!token.startsWith("system/")) {
            throw new SmartTokenException("invalid_scope",
                "Only system/ scopes are allowed for client_credentials grant: " + token);
        }
    }

    // ==================== Refresh Token 工具 ====================

    /**
     * 產生 opaque refresh token 並寫入 session。
     * 格式：16 bytes SecureRandom → URL-safe Base64 無填充（22 字元）
     */
    private void issueRefreshToken(DgrSmartAuthSession session) {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setRefreshToken(token);
        session.setRefreshTokenExpire(System.currentTimeMillis() + REFRESH_TOKEN_LIFETIME_MS);
    }

    /**
     * 驗證 requestedScope 是否為 grantedScope 的子集。
     * 若有任何 token 不在 grantedScope 內，則拋出 invalid_scope。
     */
    private void validateScopeSubset(String requestedScope, List<String> grantedScope) {
        Set<String> granted = new HashSet<>(grantedScope);
        for (String token : requestedScope.split("\\s+")) {
            if (!granted.contains(token)) {
                throw new SmartTokenException("invalid_scope",
                    "Requested scope exceeds originally granted scope: " + token);
            }
        }
    }

    /**
     * 檢查 scope 字串（空格分隔）是否包含指定 scope token。
     */
    private boolean hasScopeToken(List<String> scope, String token) {
        if (scope == null || scope.isEmpty()) {
            return false;
        }
        return scope.contains(token);
    }

    // ==================== PKCE 驗證 ====================

    /**
     * PKCE S256 驗證：Base64URL(SHA-256(code_verifier)) 與 session.codeChallenge 比對。
     */
    private void verifyPkce(DgrSmartAuthSession session, String codeVerifier) {
        String storedChallenge = session.getCodeChallenge();
        if (!StringUtils.hasText(storedChallenge)) {
            // session 沒有 code_challenge，不需要驗證 PKCE
            return;
        }

        if (!StringUtils.hasText(codeVerifier)) {
            throw new SmartTokenException("invalid_grant",
                "code_verifier is required when code_challenge was provided");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String computedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            if (!computedChallenge.equals(storedChallenge)) {
                throw new SmartTokenException("invalid_grant", "PKCE code_verifier mismatch");
            }
        } catch (SmartTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartTokenException("invalid_grant", "PKCE verification error: " + e.getMessage());
        }
    }

    // ==================== JWT 簽發 ====================

    /**
     * 為 Backend Services（client_credentials grant）簽發 RS256 JWT access token。
     *
     * Claims：iss=baseUrl、sub=clientId、aud=baseUrl、exp（300 秒）、iat、jti、client_id、scope（JSON array）
     * Header：alg=RS256、kid（與 JWKS 端點一致）
     * 不含 patient、encounter claim。
     */
    private String buildBackendServicesJwt(String clientId, String scope, String baseUrl) {
        try {
            KeyPair keyPair = keyProvider.getSigningKeyPair();
            String kid = keyProvider.getKid();

            Date now = new Date();
            Date exp = new Date(now.getTime() + BACKEND_SERVICES_TOKEN_LIFETIME_SECONDS * 1000L);

            String[] scopeArray = scope.split("\\s+");

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .subject(clientId)
                .audience(baseUrl)
                .expirationTime(exp)
                .issueTime(now)
                .jwtID(UUID.randomUUID().toString())
                .claim("client_id", clientId)
                .claim("scope", Arrays.asList(scopeArray))
                .build();

            URI jwksEndpoint = URI.create(baseUrl + "/dgrv4/ssotoken/smart/jwks");
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(kid)
                .jwkURL(jwksEndpoint)
                .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner((RSAPrivateKey) keyPair.getPrivate()));

            return jwt.serialize();

        } catch (Exception e) {
            TPILogger.tl.error("[SMART Token] Failed to sign Backend Services JWT: " + e.getMessage());
            throw new SmartTokenException("server_error", "Failed to generate access token");
        }
    }

    /**
     * 簽發 RS256 JWT access token，scope 取自 session.grantedScope。
     */
    private String buildJwtAccessToken(DgrSmartAuthSession session) {
        return buildJwtAccessTokenWithScope(session, session.getGrantedScope());
    }

    /**
     * 簽發 RS256 JWT access token，scope 由呼叫方指定（refresh 時可縮小）。
     *
     * 治標說明：access token 會被 {@code TokenHelper.verifyApiForBearer} 當成 OIDC token 驗：
     * 用 GtwIdP JWKS 驗章、iss 需為 OIDC issuer、aud 需含 {@code AUDIENCE_OIDC}。
     * 故此處改用 GtwIdP 金鑰（GTW_IDP_JWK1）簽章，並對齊 iss / aud，
     * 與 {@code OAuthTokenService} 核發的 OIDC token 一致，方能通過驗證鏈。
     *
     * Claims：iss（OIDC issuer）、sub、aud（AUDIENCE_OIDC）、exp、iat、jti、client_id、scope（JSON array）、patient、encounter
     * Header：alg=RS256、kid（GtwIdP JWK 的 kid，與 GtwIdP JWKS 一致）
     */
    private String buildJwtAccessTokenWithScope(DgrSmartAuthSession session, List<String> scope) {
        try {
            // 用 GtwIdP 金鑰簽章，使 access token 可通過 GtwIdP JWKS 驗章
            JWK gtwJwk = JWK.parse(tsmpSettingService.getVal_GTW_IDP_JWK1());
            RSAPrivateKey privateKey = (RSAPrivateKey) gtwJwk.toRSAKey().toKeyPair().getPrivate();
            String kid = gtwJwk.getKeyID();

            // iss 對齊驗證鏈 TokenHelper.validateOidcJwtClaims 的期望值（同源於設定的 public domain/port）
            String schemeAndDomainAndPort = OIDCWellKnownHelper.getSchemeAndDomainAndPort(
                tsmpSettingService.getVal_DGR_PUBLIC_DOMAIN(),
                tsmpSettingService.getVal_DGR_PUBLIC_PORT());
            String issuer = OIDCWellKnownHelper.getIssuer(
                schemeAndDomainAndPort, DgrIdPType.OIDC, DgrTokenVersion.PATH_V2);

            Date now = new Date();
            Date exp = new Date(now.getTime() + ACCESS_TOKEN_LIFETIME_SECONDS * 1000L);

            // scope 轉為 JSON array
            List<String> scopeList = (scope != null) ? scope : List.of();

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(session.getUserName())
                .audience(OAuthTokenService.AUDIENCE_OIDC)
                .expirationTime(exp)
                .issueTime(now)
                .jwtID(UUID.randomUUID().toString())
                .claim("client_id", session.getClientId())
                .claim("scope", scopeList);

            // 條件式加入 launch context
            if (StringUtils.hasText(session.getPatientId())) {
                claimsBuilder.claim("patient", session.getPatientId());
            }
            if (StringUtils.hasText(session.getEncounterId())) {
                claimsBuilder.claim("encounter", session.getEncounterId());
            }
            if (StringUtils.hasText(session.getSimError())
                    && session.getSimError().startsWith("request_")) {
                claimsBuilder.claim("sim_error", session.getSimError());
            }

            JWTClaimsSet claims = claimsBuilder.build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(kid)
                .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));

            return jwt.serialize();

        } catch (Exception e) {
            TPILogger.tl.error("[SMART Token] Failed to sign JWT: " + e.getMessage());
            throw new SmartTokenException("server_error", "Failed to generate access token");
        }
    }

    /**
     * 簽發 OIDC id_token（RS256）。
     *
     * aud 指向 client_id（OIDC Core §2），與 access_token 的 aud（FHIR Server URL）不同。
     * sub 以 userName 為主；兩者皆 null 時回傳 null，呼叫端不放入 response。
     *
     * scope 含 profile 時額外加入 name（userAlias 優先，fallback userName）與 email（userEmail）claims。
     * scope 含 fhirUser 時記錄 WARN——fhirUser claim 尚未實作，需要 FHIR User mapping。
     *
     * @param session       session 含 clientId、userName、userEmail 等身份資訊
     * @param effectiveScope 本次有效 scope（空格分隔），用於判斷是否加入 profile/fhirUser
     * @param baseUrl       JWT issuer，與 access_token 一致
     * @return 序列化的 JWT 字串；sub 無法取得時為 null
     */
    private String buildIdToken(DgrSmartAuthSession session, List<String> effectiveScope, String baseUrl) {
        // sub 取 userName；userName 為 null 時 fallback 到 userEmail
        String sub = StringUtils.hasText(session.getUserName())
            ? session.getUserName()
            : session.getUserEmail();

        if (!StringUtils.hasText(sub)) {
            TPILogger.tl.warn("[SMART Token] 無法取得 userName 或 userEmail，跳過 id_token 簽發");
            return null;
        }

        try {
            KeyPair keyPair = keyProvider.getSigningKeyPair();
            String kid = keyProvider.getKid();

            Date now = new Date();
            Date exp = new Date(now.getTime() + ACCESS_TOKEN_LIFETIME_SECONDS * 1000L);

            // OIDC 規格：aud 必須包含 client_id，與 access_token 的 aud（FHIR Server URL）不同
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .subject(sub)
                .audience(session.getClientId())
                .expirationTime(exp)
                .issueTime(now);

            // scope 含 profile 時依 OIDC Core §5.1 加入 name 與 email
            if (hasScopeToken(effectiveScope, "profile")) {
                String displayName = StringUtils.hasText(session.getUserAlias())
                    ? session.getUserAlias() : session.getUserName();
                if (StringUtils.hasText(displayName)) {
                    claimsBuilder.claim("name", displayName);
                }
                if (StringUtils.hasText(session.getUserEmail())) {
                    claimsBuilder.claim("email", session.getUserEmail());
                }
            }

            // scope 含 fhirUser 時加入 FHIR Reference URL（依 SMART App Launch STU2.2 §id_token claims）
            if (hasScopeToken(effectiveScope, "fhirUser")) {
                String fhirUser = session.getFhirUser();
                if (StringUtils.hasText(fhirUser)) {
                    String fhirUserUrl = baseUrl + "/smart-on-fhir/" + session.getWebsiteName()
                            + normalizeFhirBasePath(session.getFhirBasePath()) + "/" + fhirUser;
                    claimsBuilder.claim("fhirUser", fhirUserUrl);
                } else {
                    TPILogger.tl.warn("[SMART Token] fhirUser scope granted but session.fhirUser is empty");
                }
            }

            URI jwksUri = URI.create(baseUrl + "/dgrv4/ssotoken/smart/jwks");
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(kid)
                .jwkURL(jwksUri)
                .build();

            SignedJWT jwt = new SignedJWT(header, claimsBuilder.build());
            jwt.sign(new RSASSASigner((RSAPrivateKey) keyPair.getPrivate()));

            return jwt.serialize();

        } catch (Exception e) {
            TPILogger.tl.error("[SMART Token] Failed to sign id_token: " + e.getMessage());
            throw new SmartTokenException("server_error", "Failed to generate id_token");
        }
    }

    /**
     * 將 fhirBasePath 正規化：null/"/" 回傳空字串，避免組出 //Practitioner/xxx。
     */
    private String normalizeFhirBasePath(String fhirBasePath) {
        if (!StringUtils.hasText(fhirBasePath) || "/".equals(fhirBasePath)) {
            return "";
        }
        return fhirBasePath;
    }

    // ==================== Token Response 組裝 ====================

    private Map<String, Object> buildTokenResponse(DgrSmartAuthSession session, String accessToken,
            List<String> effectiveScope, String baseUrl) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", ACCESS_TOKEN_LIFETIME_SECONDS);
        response.put("scope", String.join(" ", effectiveScope));

        // scope 含 openid 時才簽發 id_token（OIDC Core §2）
        if (hasScopeToken(effectiveScope, "openid")) {
            String idToken = buildIdToken(session, effectiveScope, baseUrl);
            if (idToken != null) {
                response.put("id_token", idToken);
            }
        }

        // 條件式加入 refresh_token
        if (StringUtils.hasText(session.getRefreshToken())) {
            response.put("refresh_token", session.getRefreshToken());
        }

        // 條件式加入 launch context
        if (StringUtils.hasText(session.getPatientId())) {
            response.put("patient", session.getPatientId());
        }
        if (StringUtils.hasText(session.getEncounterId())) {
            response.put("encounter", session.getEncounterId());
        }

        return response;
    }

    // ==================== Token History ====================

    private void writeSmartTokenHistory(String userName, String clientId, String accessTokenJwt,
            String scope, String idTokenJwt, String refreshToken, Long refreshTokenExpireMs) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessTokenJwt);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String jti = claims.getJWTID();
            Date expiredAt = claims.getExpirationTime();

            // 沿用 OAuthTokenService 的慣例：
            // 沒有 refresh token 時，retoken_jti / reexpired_at 填 access token 的值
            String retokenJti;
            Date reexpiredAt;
            if (StringUtils.hasText(refreshToken) && refreshTokenExpireMs != null) {
                retokenJti = refreshToken;
                reexpiredAt = new Date(refreshTokenExpireMs);
            } else {
                retokenJti = jti;
                reexpiredAt = expiredAt;
            }

            tokenHelper.createTsmpTokenHistory(
                    userName, clientId, jti, scope, expiredAt,
                    retokenJti, reexpiredAt, null,
                    null, 0L, null, 0L,
                    "SMART", idTokenJwt, refreshToken, null);
        } catch (Exception e) {
            TPILogger.tl.error("[SMART Token] Failed to write token history: " + e.getMessage());
            throw new SmartTokenException("server_error", "Failed to record token history");
        }
    }

    // ==================== 錯誤模擬 ====================

    private void injectSimulatedTokenError(String simError) {
        if (simError == null || simError.isEmpty()) return;
        switch (simError) {
            case "token_invalid_token":
                throw new SmartTokenException("invalid_grant",
                        "Simulated error: invalid token");
            case "token_invalid_scope":
                throw new SmartTokenException("invalid_scope",
                        "Simulated error: invalid scope");
            default:
                break;
        }
    }

    private void injectSimulatedRefreshError(String simError) {
        if (simError == null || simError.isEmpty()) return;
        if ("token_expired_refresh_token".equals(simError)) {
            throw new SmartTokenException("invalid_grant",
                    "Simulated error: refresh token has expired");
        }
        injectSimulatedTokenError(simError);
    }

    // ==================== 例外 ====================

    /**
     * SMART token 端點業務例外。
     * error 和 description 對應 RFC 6749 §5.2 的 error response。
     */
    public static class SmartTokenException extends RuntimeException {
        private final String error;
        private final String description;

        public SmartTokenException(String error, String description) {
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
