package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.IKibanaService2;
import tpi.dgrv4.gateway.service.TsmpSettingService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

        String allowlistStr = "dgrv4," + tsmpSettingService.getVal_KIBANA_REFERER_ALLOWLIST();
        String[] allowlist = Arrays.stream(allowlistStr.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        List<String> referers = httpHeaders.get(HttpHeaders.REFERER);
        // 檢查 referer 阻擋不明來源的請求
        // referer 需要包含 dgrv4 或 /app/dashboards
        // check referer contains dgrv4 or /app/dashboards
        if (referers == null || referers.isEmpty() || referers.stream().noneMatch(referer ->
                Arrays.stream(allowlist).anyMatch(referer::contains))) {
            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
            } catch (IOException e) {
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            }
            TPILogger.tl.warn("referer: " + referers);
            return;
        }
        service.resource(httpHeaders, request, response, payload);

    }
}
