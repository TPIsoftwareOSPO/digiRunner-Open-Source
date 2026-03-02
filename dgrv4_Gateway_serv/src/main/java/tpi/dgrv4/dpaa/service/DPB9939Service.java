package tpi.dgrv4.dpaa.service;


import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9939Req;
import tpi.dgrv4.dpaa.vo.DPB9939Resp;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.IKibanaService2;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Service
public class DPB9939Service {
    private static final String EXPIRE_KEY = "kibanaExpireKey";
    private final TsmpSettingService tsmpSettingService;
    private final IKibanaService2 kibanaService2;

    private static final String BASIC = "basic";
    private static final String SESSION = "session";
    private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";
    @Value("${ecosys.lb-url:http://localhost:18080}")
    private String ecosysDgrLb;
    @Autowired
    public DPB9939Service(TsmpSettingService tsmpSettingService, @Nullable IKibanaService2 kibanaService2) {
        super();
        this.tsmpSettingService = tsmpSettingService;
        this.kibanaService2 = kibanaService2;
    }

    public DPB9939Resp testKibanaConnection(DPB9939Req req) {
        DPB9939Resp resp = new DPB9939Resp();
        try {
            // Kibana 設定

            checkSetting(TsmpSettingDao.Key.KIBANA_TRANSFER_PROTOCOL);
            checkSetting(TsmpSettingDao.Key.KIBANA_HOST);
            getTsmpSettingService().getVal_KIBANA_PORT();
            String user = checkSetting(TsmpSettingDao.Key.KIBANA_USER).trim();
            String mima = getTsmpSettingService().getVal_KIBANA_PWD().trim();
            String auth = checkSetting(TsmpSettingDao.Key.KIBANA_AUTH).trim().toLowerCase();
            String statusApi = checkSetting(TsmpSettingDao.Key.KIBANA_STATUS_URL).trim();

            // 檢查 mima
            if (!StringUtils.hasLength(mima))
                throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_PWD);

            String baseUrl = ecosysDgrLb;

            boolean isConnection = false;
            HttpUtil.HttpRespData respData = checkConnection(
                    baseUrl, auth, user, mima, statusApi
            );

            if (respData.statusCode > 0 && respData.statusCode < 400) {
                isConnection = true;
            } else {
                TPILogger.tl.error(respData.getLogStr());
            }

            resp.setAuth(auth);
            resp.setStatusApiUrl(statusApi);
            resp.setConnection(isConnection);
            resp.setResp(respData.respStr);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    private String checkSetting(String id) {
        var val = getTsmpSettingService().getStringVal(id);
        if (!StringUtils.hasLength(val))
            throw TsmpDpAaRtnCode._1474.throwing(id);
        return val;
    }

    protected HttpUtil.HttpRespData checkConnection(
            String url, String auth, String user, String mima, String statusApi
    ) throws Exception {
        String normalizedUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String normalizedStatusApi = statusApi.startsWith("/") ? statusApi.substring(1) : statusApi;
        String localKibanaUrl = normalizedUrl + "/" + normalizedStatusApi;

        TPILogger.tl.debug("Public base URL: " + url);
        TPILogger.tl.debug("Auth type: " + auth);

        if (BASIC.equals(auth)) {
            return checkConnectionByBasicAuth(localKibanaUrl, user, mima);
        }

        if (SESSION.equals(auth)) {
            return checkConnectionBySessionAuth(localKibanaUrl, user, mima);
        }

        throw TsmpDpAaRtnCode._1559.throwing("Error auth type. : " + auth);
    }

    private HttpUtil.HttpRespData checkConnectionByBasicAuth(String localKibanaUrl, String user, String mima) throws Exception {
        TPILogger.tl.debug("Basic Auth: Accessing through resource()");
        return callApi(localKibanaUrl, null, BASIC, user, mima);
    }

    private HttpUtil.HttpRespData checkConnectionBySessionAuth(String localKibanaUrl, String user, String mima)
            throws Exception {
        HttpUtil.HttpRespData validationError = validateKibanaService();
        if (validationError != null) {
            return validationError;
        }

        IKibanaService2 kibanaService = (IKibanaService2) getKibanaService2();
        return performSessionLogin(kibanaService, localKibanaUrl, user, mima);
    }

    private HttpUtil.HttpRespData validateKibanaService() {
        if (getKibanaService2() == null) {
            TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
            return createErrorResponse(404, NO_ENTERPRISE_SERVICE);
        }

        if (!(getKibanaService2() instanceof IKibanaService2)) {
            TPILogger.tl.warn("IKibanaService2 is not KibanaService2 instance");
            return createErrorResponse(500, "Incompatible service implementation");
        }

        return null;
    }

    private HttpUtil.HttpRespData createErrorResponse(int statusCode, String message) {
        HttpUtil.HttpRespData respData = new HttpUtil.HttpRespData();
        respData.statusCode = statusCode;
        respData.respStr = message;
        return respData;
    }

    private HttpUtil.HttpRespData performSessionLogin(IKibanaService2 kibanaService, String localKibanaUrl,
                                                      String user, String mima) throws Exception {
        TPILogger.tl.debug("Session Auth: Step 1 - Login to session");

        HttpResponse<byte[]> loginResp = kibanaService.login_withUrl(user, mima);

        if (isHttpError(loginResp.statusCode())) {
            TPILogger.tl.error("Login failed: " + loginResp.statusCode());
            return createErrorResponse(loginResp.statusCode(), new String(loginResp.body()));
        }

        TPILogger.tl.debug("Login successful");
        TPILogger.tl.debug("Session Auth: Step 2 - Accessing through resource()");

        List<String> cookies = extractCookies(loginResp);
        HttpUtil.HttpRespData respData = callApi(localKibanaUrl, cookies, SESSION, user, mima);

        TPILogger.tl.debug("Response from resource(): " + respData.statusCode);
        return respData;
    }

    private boolean isHttpError(int statusCode) {
        return statusCode < 200 || statusCode >= 400;
    }

    private List<String> extractCookies(HttpResponse<byte[]> response) {
        List<String> cookies = new ArrayList<>();
        response.headers().map().forEach((key, valList) -> {
            if (key != null && key.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                extractCookieValues(valList, cookies);
            }
        });
        return cookies;
    }

    private void extractCookieValues(List<String> setCookieHeaders, List<String> cookies) {
        for (String setCookieValue : setCookieHeaders) {
            String[] parts = setCookieValue.split(";");
            if (parts.length > 0) {
                cookies.add(parts[0].trim());
            }
        }
    }

    protected HttpUtil.HttpRespData callApi(String url, List<String> cookies, String authType, String user, String mima)
            throws Exception {
        if (cookies == null || cookies.isEmpty())
            cookies = new ArrayList<>();
        String expireKey = ExpireKeyUtil.getExpireKey_60sec();
        String cookiePath = url.contains("/kibana") ? "/kibana" : "/";
        ResponseCookie cookie = ResponseCookie.from(EXPIRE_KEY, expireKey) // key & value
                .path(cookiePath)
                .httpOnly(true) // 禁止 JavaScript 存取 cookie, 防止 XSS Attack (Cross-Site Scripting，跨站腳本攻擊)
                .secure(true) // 讓 cookie 只能透過 https 傳遞, 即只有 HTTPS 才能讀與寫
                .sameSite("Lax") // 防止 CSRF Attack (Cross-site request forgery，跨站請求偽造)
                .build();

        cookies.add(cookie.toString());
        Map<String, List<String>> header = new HashMap<>();
        header.put(HttpHeaders.ACCEPT, List.of(MediaType.APPLICATION_JSON_VALUE));
        header.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
        header.put(HttpHeaders.REFERER, List.of(url));
        header.put(HttpHeaders.COOKIE, cookies);

        header.put("sec-fetch-site", List.of("same-origin"));
        header.put(HttpHeaders.CACHE_CONTROL, List.of("no-cache, no-store, must-revalidate"));
        header.put(HttpHeaders.PRAGMA, List.of("no-cache"));

        if (BASIC.equals(authType)) {
            String encodedAuth = Base64Util.base64Encode((user + ":" + mima).getBytes());
            header.put(HttpHeaders.AUTHORIZATION, List.of("Basic " + encodedAuth));
        }

        HttpUtil.HttpRespData resp = HttpUtil.httpReqByGetList(url, header, false, false);
        TPILogger.tl.debug(resp.getLogStr());
        return resp;
    }

    protected TsmpSettingService getTsmpSettingService() {
        return tsmpSettingService;
    }

    protected IKibanaService2 getKibanaService2() {
        return kibanaService2;
    }
}
