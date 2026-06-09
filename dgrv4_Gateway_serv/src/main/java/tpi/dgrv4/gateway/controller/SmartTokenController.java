package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.CrossOrigin;

import tpi.dgrv4.gateway.component.SmartClientAuthenticator;
import tpi.dgrv4.gateway.constant.DgrTokenVersion;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.service.SmartTokenService;

/**
 * SMART App Launch Token 端點。
 *
 * 背景：SMART App Launch 規範要求 token 端點接收 POST 請求，
 * Content-Type 為 application/x-www-form-urlencoded，
 * 依 grant_type 分派到對應的 token 交換邏輯。
 *
 * 分派策略（混合）：
 * - authorization_code / refresh_token：self-handling，走 SmartTokenService（保留 SMART 專屬處理）。
 * - client_credentials：委派 OAuthTokenService.getOAuthToken（PATH_V2，與平台統一）。
 *
 * 規格依據：
 * - HL7 SMART App Launch STU2.2 §Token Exchange
 * - RFC 6749 §4.1.3（Authorization Code Grant - Access Token Request）
 * - RFC 6749 §5.1（Successful Response：Cache-Control: no-store, Pragma: no-cache）
 */
@CrossOrigin
@RestController
public class SmartTokenController {

    private final SmartTokenService tokenService;
    private final SmartClientAuthenticator authenticator;
    private final OAuthTokenService oauthTokenService;

    @Autowired
    public SmartTokenController(SmartTokenService tokenService,
            SmartClientAuthenticator authenticator,
            OAuthTokenService oauthTokenService) {
        this.tokenService = tokenService;
        this.authenticator = authenticator;
        this.oauthTokenService = oauthTokenService;
    }

    /**
     * 處理 Token 請求。
     *
     * 流程：
     * 1. 從 HTTP 請求動態解析 baseUrl
     * 2. 認證 client（SmartClientAuthenticator）
     * 3. 驗證 grant_type
     * 4. 依 grant_type 分派：authorization_code / refresh_token 自行處理；
     *    client_credentials 委派 OAuthTokenService
     * 5. 回傳 JSON response + Cache-Control / Pragma headers
     */
    @PostMapping(value = "/dgrv4/ssotoken/smart/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(@RequestParam Map<String, String> params,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader HttpHeaders headers) {
        TPILogger.tl.info("\n--【SMART Token】--");

        try {
            // 1. 從 HTTP 請求動態解析 baseUrl
            String baseUrl = resolveBaseUrl(request);
            String tokenEndpointUrl = baseUrl + "/dgrv4/ssotoken/smart/token";

            // 2. Client 認證
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            String clientId = authenticator.authenticate(authorizationHeader, params, tokenEndpointUrl);

            // 3. 驗證 grant_type
            String grantType = params.get("grant_type");
            if (!StringUtils.hasText(grantType)) {
                return errorResponse(HttpStatus.BAD_REQUEST, "invalid_request", "Missing required parameter: grant_type");
            }

            // 4. 分派處理
            if ("authorization_code".equals(grantType)) {
                return handleAuthorizationCodeGrant(params, clientId, baseUrl);
            } else if ("refresh_token".equals(grantType)) {
                return handleRefreshTokenGrant(params, clientId, baseUrl);
            } else if ("client_credentials".equals(grantType)) {
                // 委派平台統一的 OAuth token 核發路徑（PATH_V2）
                return oauthTokenService.getOAuthToken(request, headers, response, DgrTokenVersion.PATH_V2);
            } else {
                return errorResponse(HttpStatus.BAD_REQUEST, "unsupported_grant_type",
                    "Grant type not supported: " + grantType);
            }

        } catch (SmartClientAuthenticator.SmartAuthenticationException e) {
            TPILogger.tl.debug("[SMART Token] Authentication failed: " + e.getMessage());
            return errorResponse(HttpStatus.UNAUTHORIZED, e.getError(), e.getDescription());
        } catch (SmartTokenService.SmartTokenException e) {
            TPILogger.tl.debug("[SMART Token] Token exchange failed: " + e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getError(), e.getDescription());
        }
    }

    // ==================== authorization_code grant ====================

    private ResponseEntity<?> handleAuthorizationCodeGrant(Map<String, String> params, String clientId,
            String baseUrl) {
        String code = params.get("code");
        String redirectUri = params.get("redirect_uri");
        String codeVerifier = params.get("code_verifier");

        Map<String, Object> tokenResponse = tokenService.exchangeAuthorizationCode(
            code, codeVerifier, redirectUri, clientId, baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store");
        headers.setPragma("no-cache");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);
    }

    // ==================== refresh_token grant ====================

    private ResponseEntity<?> handleRefreshTokenGrant(Map<String, String> params, String clientId,
            String baseUrl) {
        String refreshToken = params.get("refresh_token");
        String scope = params.get("scope");

        Map<String, Object> tokenResponse = tokenService.refreshToken(refreshToken, clientId, scope, baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store");
        headers.setPragma("no-cache");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(tokenResponse, headers, HttpStatus.OK);
    }

    // ==================== URL Resolution ====================

    private String resolveBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }
        if (scheme.contains(",")) {
            scheme = scheme.split(",")[0].trim();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = request.getHeader("Host");
        }
        if (host == null || host.isEmpty()) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port != 80 && port != 443) {
                host += ":" + port;
            }
        }
        if (host.contains(",")) {
            host = host.split(",")[0].trim();
        }

        return scheme + "://" + host;
    }

    // ==================== Error Response ====================

    /**
     * 組裝 RFC 6749 §5.2 格式的 error response。
     */
    private ResponseEntity<?> errorResponse(HttpStatus status, String error, String description) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }
}
