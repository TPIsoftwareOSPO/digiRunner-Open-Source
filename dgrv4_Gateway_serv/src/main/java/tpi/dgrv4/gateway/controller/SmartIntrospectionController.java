package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.component.SmartClientAuthenticator;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartIntrospectionService;

/**
 * SMART Token Introspection 端點（RFC 7662）。
 *
 * Resource server 可呼叫此端點驗證 access token 的有效性。
 * 呼叫者必須通過 client 認證。
 */
@CrossOrigin
@RestController
public class SmartIntrospectionController {

    private final SmartIntrospectionService introspectionService;
    private final SmartClientAuthenticator authenticator;

    @Autowired
    public SmartIntrospectionController(SmartIntrospectionService introspectionService,
            SmartClientAuthenticator authenticator) {
        this.introspectionService = introspectionService;
        this.authenticator = authenticator;
    }

    @PostMapping(value = "/dgrv4/ssotoken/smart/introspect",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspect(@RequestParam Map<String, String> params,
            HttpServletRequest request) {
        TPILogger.tl.info("\n--【SMART Introspect】--");

        try {
            String baseUrl = resolveBaseUrl(request);
            String introspectUrl = baseUrl + "/dgrv4/ssotoken/smart/introspect";
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            authenticator.authenticate(authorizationHeader, params, introspectUrl);

            String token = params.get("token");
            Map<String, Object> result = introspectionService.introspect(token);
            return ResponseEntity.ok(result);

        } catch (SmartClientAuthenticator.SmartAuthenticationException e) {
            TPILogger.tl.debug("[SMART Introspect] Authentication failed: " + e.getMessage());
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", e.getError());
            error.put("error_description", e.getDescription());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
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
