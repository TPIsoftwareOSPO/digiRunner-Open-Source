package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.component.SmartClientAuthenticator;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartLaunchService;

/**
 * EHR Launch Registration 端點。
 *
 * 背景：EHR 系統在啟動 SMART App 前，呼叫此端點預先登記 launch context，
 * 取得不透明的 launch token，供後續授權流程使用。
 *
 * 規格依據：HL7 SMART App Launch STU2.2 §EHR Launch
 *
 * API：
 * - 路徑：POST /dgrv4/ssotoken/smart/launch
 * - Content-Type：application/x-www-form-urlencoded
 * - 參數：patient_id（選填）、encounter_id（選填，至少需一個）
 * - 成功 200：{"launch": "<uuid>"}
 * - 認證失敗 401：{"error": "invalid_client", "error_description": "..."}
 * - 參數錯誤 400：{"error": "invalid_request", "error_description": "..."}
 * - launchMode 不允許 403：{"error": "unauthorized_client", "error_description": "..."}
 */
@RestController
public class SmartLaunchController {

    private final SmartLaunchService launchService;

    @Autowired
    public SmartLaunchController(SmartLaunchService launchService) {
        this.launchService = launchService;
    }

    /**
     * 登記 EHR Launch 上下文，回傳不透明 launch token。
     *
     * @param params  form body 參數（client_id 或 Authorization header 認證，plus patient_id / encounter_id）
     * @param request HTTP 請求（用於解析 baseUrl 與 Authorization header）
     * @return 成功 200 with {"launch": "..."} 或 錯誤 response
     */
    @PostMapping(value = "/dgrv4/ssotoken/smart/launch",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> launch(@RequestParam Map<String, String> params,
            HttpServletRequest request) {
        TPILogger.tl.info("\n--【SMART EHR Launch Registration】--");

        try {
            String baseUrl = resolveBaseUrl(request);
            String authorizationHeader = request.getHeader("Authorization");

            Map<String, String> response = launchService.registerLaunch(authorizationHeader, params, baseUrl);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (SmartClientAuthenticator.SmartAuthenticationException e) {
            TPILogger.tl.debug("[SMART Launch] Authentication failed: " + e.getMessage());
            return errorResponse(HttpStatus.UNAUTHORIZED, e.getError(), e.getDescription());
        } catch (SmartLaunchService.SmartLaunchException e) {
            TPILogger.tl.debug("[SMART Launch] Launch registration failed: " + e.getMessage());
            HttpStatus status = "unauthorized_client".equals(e.getError())
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
            return errorResponse(status, e.getError(), e.getDescription());
        }
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
