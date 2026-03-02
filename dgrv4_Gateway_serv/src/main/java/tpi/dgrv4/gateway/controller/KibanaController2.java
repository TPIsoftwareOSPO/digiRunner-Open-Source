package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.IKibanaService2;
import tpi.dgrv4.gateway.service.TsmpSettingService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class KibanaController2 {

    private IKibanaService2 service;

    private TsmpSettingService tsmpSettingService;

    @Autowired
    public KibanaController2(@Nullable IKibanaService2 service, TsmpSettingService tsmpSettingService) {
        super();
        this.service = service;
        this.tsmpSettingService = tsmpSettingService;
    }

    @GetMapping(value = "/kibana/login")
    public void login2(@RequestHeader HttpHeaders httpHeaders, @RequestParam String reportURL,
                       @RequestParam String cuuid, @RequestParam(required = false) String capikey, HttpServletRequest request,
                       HttpServletResponse response) throws Throwable {

        httpHeaders.add("cuuid", cuuid);
        httpHeaders.add("capi-key", capikey);

        service.login(httpHeaders, reportURL, request, response);


    }

    @SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
    @RequestMapping(value = {"/kibana/**"})
    public void dashboard(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
                          @CookieValue(name = "kibanaExpireKey") String value,
                          @RequestBody(required = false) String payload
    ) throws Exception {
        String requestUri = request.getRequestURI();

        // --- Get relative path from request ---
        String path = requestUri.substring("/kibana/".length());
        if (path.startsWith("kibana/")) {
            path = path.substring("kibana/".length());
        }
        final String finalPath = normalizePath(path); // finalPath is now normalized (no leading/trailing slashes)

        // --- Check for cookie bypass ---
        List<String> bypassPaths = tsmpSettingService.getVal_KIBANA_COOKIE_BYPASS_PATHS();
        if (bypassPaths == null) {
            bypassPaths = Collections.emptyList();
        }
        // Apply the same normalization to the bypass paths from settings
        boolean isBypassRequest = bypassPaths.stream()
                .map(this::normalizePath)
                .anyMatch(finalPath::startsWith);

        // --- Original 'isAllowed' logic for dashboard paths ---
        List<String> allowedAppPaths = tsmpSettingService.getVal_KIBANA_REFERER_ALLOWLIST().stream()
                .map(this::normalizePath)
                .collect(Collectors.toList());
        allowedAppPaths.add(normalizePath(tsmpSettingService.getVal_KIBANA_STATUS_URL()));

        boolean isAllowed = allowedAppPaths.stream().anyMatch(finalPath::equals);

        if (!isAllowed) {
            service.resource(httpHeaders, request, response, payload);
            return;
        }

        // --- Main security check ---
        // Proceed if the request is in the bypass list OR if the cookie is valid.
        if (isBypassRequest || (ExpireKeyUtil.verifyExpireKey(value) &&
                checkSecFetchSite(httpHeaders.get("sec-fetch-site"), httpHeaders.get(HttpHeaders.REFERER), response))) {
            service.resource(httpHeaders, request, response, payload);
        } else {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Kibana session expired or invalid.");
            }
        }
    }

    private boolean checkSecFetchSite(List<String> secFetchSites, List<String> referers, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(secFetchSites) || secFetchSites.stream().noneMatch("same-origin"::equals)) {
            return sendForbidden(response, "Invalid sec-fetch-site header");
        }

        if (CollectionUtils.isEmpty(referers)) {
            return sendForbidden(response, "Missing referer header");
        }

        List<String> allowList = tsmpSettingService.getVal_KIBANA_REFERER_ALLOWLIST().stream()
                .map(String::trim)
                .map(this::normalizePath)
                .collect(Collectors.toList());
        allowList.add("dgrv4");
//        allowList.add(normalizePath(tsmpSettingService.getVal_KIBANA_STATUS_URL()));

        for (String referer : referers) {
            try {
                URI refererUri = new URI(referer);
                String refererPath = normalizePath(refererUri.getPath());

                if (allowList.stream().anyMatch(refererPath::contains)) {
                    return true;
                }
            } catch (URISyntaxException e) {
                TPILogger.tl.warn("Invalid referer URI: "+ referer);
            }
        }

        return sendForbidden(response, "Referer not in allow list: " + referers);
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String normalized = path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean sendForbidden(HttpServletResponse response, String logMessage) {
        TPILogger.tl.warn(logMessage);
        if (!response.isCommitted()) {
            try {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            }
        }
        return false;
    }
}
