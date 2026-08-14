package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.component.SmartClientAuthenticator;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartRevokeService;

/**
 * SMART Token Revocation 端點（RFC 7009）。
 *
 * discovery 的 revocation_endpoint 即指向此端點。
 * token 參數必填；token_type_hint 選填且不採用（型別由 SmartRevokeService 以 token 本身判斷）。
 * 依 RFC 7009 §2.2，查無或無法撤銷的 token 一律回 200。
 */
@SuppressWarnings("java:S5122")//Allow any domain for sonarqube scan
@CrossOrigin
@RestController
public class SmartRevokeController {

    private final SmartRevokeService revokeService;
    private final SmartClientAuthenticator authenticator;

    @Autowired
    public SmartRevokeController(SmartRevokeService revokeService,
            SmartClientAuthenticator authenticator) {
        this.revokeService = revokeService;
        this.authenticator = authenticator;
    }

    @PostMapping(value = "/dgrv4/ssotoken/smart/revoke",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> revoke(@RequestParam Map<String, String> params,
            HttpServletRequest request) {
        TPILogger.tl.info("\n--【SMART Revoke】--");

        try {
            String baseUrl = resolveBaseUrl(request);
            String revokeUrl = baseUrl + "/dgrv4/ssotoken/smart/revoke";
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            // 呼叫者可能是未註冊 SMART client 的 backend service，不查註冊但一律驗憑證
            String clientId = authenticator.authenticateWithoutSmartRegistration(
                    authorizationHeader, params, revokeUrl);

            String token = params.get("token");
            if (!StringUtils.hasText(token)) {
                return errorResponse(HttpStatus.BAD_REQUEST, "invalid_request",
                        "token parameter is required");
            }

            revokeService.revoke(token, clientId);
            return ResponseEntity.ok().build();

        } catch (SmartClientAuthenticator.SmartAuthenticationException e) {
            TPILogger.tl.debug("[SMART Revoke] Authentication failed: " + e.getMessage());
            return errorResponse(HttpStatus.UNAUTHORIZED, e.getError(), e.getDescription());
        }
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status,
            String error, String description) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) scheme = request.getScheme();
        if (scheme.contains(",")) scheme = scheme.split(",")[0].trim();

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port != 80 && port != 443) host += ":" + port;
        }
        if (host.contains(",")) host = host.split(",")[0].trim();

        return scheme + "://" + host;
    }
}
