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
import java.util.Arrays;
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
    @RequestMapping(value = "/kibana/**")
    public void resource2(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request,
                          HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {
        service.resource(httpHeaders, request, response, payload);

    }
    String allowlistStr;
    @GetMapping(value = "/kibana/{path:.*}/{appName}")
    public void dashboard(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response, //
                          @CookieValue(name = "expireKey") String value,
                          @PathVariable(name = "path") String path, @PathVariable(name = "appName") String appName
    ) throws IOException, Exception {
        List<String> allowedAppPaths = tsmpSettingService.getVal_KIBANA_REFERER_ALLOWLIST();
        String fullPath = (path.isEmpty() ? "" : "/" + path) + "/" + appName;
        // Check if the requested appName is in the allowed list
        boolean isAllowed = allowedAppPaths.stream()
                .anyMatch(allowedPath -> allowedPath.endsWith(fullPath) ||
                        allowedPath.equals("/" + appName) ||
                        allowedPath.equals(appName));

        if (!isAllowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            response.flushBuffer();
            return;
        }
        // 檢核 效期
        if(ExpireKeyUtil.verifyExpireKey(value) &&
                checkSecFetchSite(httpHeaders.get("sec-fetch-site"), httpHeaders.get(HttpHeaders.REFERER), response)) {
            service.resource(httpHeaders, request, response, "");
        }else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
        }
    }

    private boolean checkSecFetchSite(List<String> secFetchSites, List<String> referers, HttpServletResponse response) {
        List<String> allowList = tsmpSettingService.getVal_KIBANA_REFERER_ALLOWLIST().stream().map(String::trim).collect(Collectors.toList());
        allowList.add("dgrv4");
            if (CollectionUtils.isEmpty(secFetchSites) ||
                secFetchSites.stream().noneMatch("same-origin"::equals) ||
                CollectionUtils.isEmpty(referers) ||
                referers.stream().noneMatch(referer -> allowList.stream().anyMatch(referer::contains))
        ) {
            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
            } catch (IOException e) {
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            }
            TPILogger.tl.warn("referer: " + referers);
            return false;
        }
        return true;
    }
}
