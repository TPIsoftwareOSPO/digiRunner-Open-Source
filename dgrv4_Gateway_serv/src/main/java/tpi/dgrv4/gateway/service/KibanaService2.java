package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;


@Service
public class KibanaService2 {

    private TsmpSettingCacheProxy tsmpSettingCacheProxy;
    private CApiKeyService capiKeyService;
    private TsmpSettingService tsmpSettingService;
    
    @Value("${digi.url.prefix:}")
    private String urlPrefix;

    public static HashMap<String, HttpResponse<byte[]>> cacheMap;
    private static String kibanaUser = null;
    private static String kibanaPwd = null;
    private static String auth = null;

    @Autowired
    public KibanaService2(TsmpSettingCacheProxy tsmpSettingCacheProxy, CApiKeyService capiKeyService,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.capiKeyService = capiKeyService;
		this.tsmpSettingService = tsmpSettingService;
	}


	public void login(HttpHeaders httpHeaders, String reportURL, HttpServletRequest request,
                      HttpServletResponse response) {
        try {

            try {
                // 驗證CApiKey
                capiKeyService.verifyCApiKey(httpHeaders, false, false);
                kibanaUser = getTsmpSettingService().getVal_KIBANA_USER();
                if (!StringUtils.hasLength(kibanaUser)) {
                    throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_USER);
                }
                kibanaPwd = getTsmpSettingService().getVal_KIBANA_PWD();
                if (!StringUtils.hasLength(kibanaPwd)) {
                    throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_PWD);
                }
            } catch (Exception e) {
                ByteArrayInputStream bi = new ByteArrayInputStream(
                        TsmpDpAaRtnCode._1522.getDefaultMessage().getBytes());
                response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
                IOUtils.copy(bi, response.getOutputStream());
                throw TsmpDpAaRtnCode._1522.throwing();
            }
            auth = getTsmpSettingService().getVal_KIBANA_AUTH();
            if (!StringUtils.hasLength(auth)) {
                throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_AUTH);
            }
            if ("session".equalsIgnoreCase(auth)) {
                // 取得Kibana登入授權資料
                loginKbn(response);
            }

            // 直接轉導 Kibana URL
            String localUrl = urlPrefix;
            if(!StringUtils.hasText(localUrl)) {
            	//checkmarx, ReDoS From Regex Injection ,所以用replaceAll會有問題, 改用沒Regex, 已通過中風險
            	localUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            }
            String redirecturl = localUrl + reportURL;
            response.sendRedirect(redirecturl);

            TPILogger.tl.debug("Redirect to " + redirecturl);
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new TsmpDpAaException("kibana error", e);
        }
    }

    StringBuffer loginsb = new StringBuffer();

    private HttpResponse<byte[]> loginKbn(HttpServletResponse response) throws IOException, NoSuchAlgorithmException, InterruptedException, KeyManagementException {
        loginsb = new StringBuffer();
        HttpResponse<byte[]> kbnResponse = login_withUrl(kibanaUser, kibanaPwd);


        loginsb.append("\nstatusCode : " + kbnResponse.statusCode());
        // Kibana 連線逾時
        if (kbnResponse.statusCode() == -1) {
            ByteArrayInputStream bi = new ByteArrayInputStream(TsmpDpAaRtnCode._1525.getDefaultMessage().getBytes());
            response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
            IOUtils.copy(bi, response.getOutputStream());
            throw TsmpDpAaRtnCode._1525.throwing();
        }

        // Kibana 未經授權
        if (kbnResponse.statusCode() == 401) {
            ByteArrayInputStream bi = new ByteArrayInputStream(TsmpDpAaRtnCode._1526.getDefaultMessage().getBytes());
            response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
            IOUtils.copy(bi, response.getOutputStream());
            throw TsmpDpAaRtnCode._1526.throwing();
        }

        // 發生其他錯誤
        if (kbnResponse.statusCode() >= 400) {
            ByteArrayInputStream bi = new ByteArrayInputStream(TsmpDpAaRtnCode._1297.getDefaultMessage().getBytes());
            response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
            IOUtils.copy(bi, response.getOutputStream());
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        loginsb.append("\nresponse headers : ");
        // 將Kibana登入授權資料取出來放在header，會用在請求Kibana URL
        kbnResponse.headers().map().forEach((key, valList) -> valList.forEach((val) -> {
            if (key != null) {
                loginsb.append("\n\t" + key + " : " + val);

                if (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                    if (!val.startsWith("jti")) {
                        response.addHeader(HttpHeaders.SET_COOKIE, val);
                    }
                }
            }
        }));

        loginsb.append("\n==============login================\n");
        TPILogger.tl.debug(loginsb.toString());
        return kbnResponse;
    }


    public HttpResponse<byte[]> login_withUrl(String kibanaUser, String kibanaPwd) throws
            IOException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {

        loginsb.append("\n==============login================\n");
        String loginUri = getTsmpSettingService().getVal_KIBANA_LOGIN_URL();


        if (!StringUtils.hasLength(loginUri)) {
            throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_LOGIN_URL);
        }
        if (!loginUri.startsWith("/")) {
            loginUri += "/";
        }
        String reqUrl = getKibanaURL() + loginUri;
        loginsb.append("reqUrl :　" + reqUrl);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", kibanaUser);
        requestBody.put("password", kibanaPwd);
        String reqBody = getTsmpSettingService().getVal_KIBANA_LOGIN_REQUESTBODY();
        if (!StringUtils.hasLength(reqBody)) {
            throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_LOGIN_REQUESTBODY);
        }
        loginsb.append("\nreqBody(Will not print credentials.) :　" + reqBody);

        reqBody = ServiceUtil.buildContent(reqBody, requestBody);


        HttpResponse<byte[]> httpResponse;
        URI targetUri = URI.create(reqUrl);
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().uri(targetUri).version(Version.HTTP_2);
        httpRequestBuilder.setHeader("kbn-xsrf", "true");
        httpRequestBuilder.setHeader("osd-xsrf", "true");
        httpRequestBuilder.setHeader("Content-Type", "application/json");
        httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(reqBody));
        HttpClient httpClient = HttpClient.newBuilder().sslContext(HttpUtil.disableWssValidation()).followRedirects(Redirect.NEVER)
                .build();

        HttpRequest httpRequest = httpRequestBuilder.build();

        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        loginsb.append("\nreqHeader :　\n" + httpRequest.headers().map().entrySet().stream()
                .map(entry -> "\t" + entry.getKey() + " = " + String.join(",", entry.getValue()))
                .collect(Collectors.joining("\n")));
        TPILogger.tl.debug(loginsb.toString());
        return httpResponse;
    }

    protected String getKibanaURL() {

        String transferProtocol = getTsmpSettingService().getVal_KIBANA_TRANSFER_PROTOCOL();
        String kibanaHost = getTsmpSettingService().getVal_KIBANA_HOST();
        String kibanaPort = getTsmpSettingService().getVal_KIBANA_PORT();

        String strUrl = transferProtocol + "://" + kibanaHost + ":" + kibanaPort;
        URL url;
        try {
            url = new URL(strUrl);
            strUrl = HttpUtil.removeDefaultPort(url);
        } catch (MalformedURLException e1) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e1));
        }
        return strUrl;
    }

    public void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
                         String payload) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n ===============================================");

        if (cacheMap == null) {
            cacheMap = new HashMap<String, HttpResponse<byte[]>>();
        }

        try {
            String kibanaPrefix = getTsmpSettingService().getVal_KIBANA_REPORTURL_PREFIX();
            String path =  request.getRequestURI();
            if (path.contains(kibanaPrefix)) {
                path = path.replaceFirst(kibanaPrefix, "");
            }
            String resourceURL = getKibanaURL() + path;
            // 去掉 /kibana2
            URI uri = new URI(resourceURL);

            resourceURL = HttpUtil.removeDefaultPort(resourceURL);
            String querString = request.getQueryString();
            if (querString != null) {
                resourceURL = resourceURL + "?" + querString;
            }
            sb.append("\nrequrli:　" + resourceURL);

            String method = request.getMethod();
            sb.append("\nmethod : " + method);

            HttpResponse<byte[]> httpResponse;
            if (cacheMap.containsKey(resourceURL)) {
                httpResponse = cacheMap.get(resourceURL);
            } else {
                // 請求Kibana URL
                URI targetUri = URI.create(resourceURL);
                HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().uri(targetUri).version(Version.HTTP_2);
                try {

                    if (method.equalsIgnoreCase("GET")) {
                        httpRequestBuilder.GET();
                    } else {
                        if (StringUtils.hasLength(payload)) {
                            sb.append("\npayload : \n" + payload);
                            httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload));
                        } else {
                            httpRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());

                        }

                    }
                } catch (Exception e) {
                    TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
                }

                Enumeration<String> httpHeaderKeys = request.getHeaderNames();
                //
                while (httpHeaderKeys.hasMoreElements()) {
                    String key = httpHeaderKeys.nextElement();
                    List<String> valueList = httpHeaders.get(key);
                    if (!CollectionUtils.isEmpty(valueList)) {
                        if (key.equalsIgnoreCase(HttpHeaders.COOKIE)) {
                            valueList.forEach(v -> {
                                if (!v.startsWith("jti")) {
                                    httpRequestBuilder.setHeader(key, v);
                                    sb.append("\n" + key + " :　" + v);
                                }
                            });

                        } else if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !key.equalsIgnoreCase(HttpHeaders.CONNECTION)
                                && !key.equalsIgnoreCase(HttpHeaders.HOST) && !key.equalsIgnoreCase("Keep-Alive")
                                && !key.equalsIgnoreCase("Transfer-Encoding")) {

                            String v = valueList.stream()
                                    .collect(Collectors.joining(", "));
                            httpRequestBuilder.setHeader(key, v);

                        }
                    }
                }
                response.setHeader("kbn-xsrf", "true");
                // 使用 basic auth
                if ("basic".equalsIgnoreCase(auth)) {
                    String encodUNPW = Base64Util.base64Encode((kibanaUser + ":" + kibanaPwd).getBytes());
                    httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodUNPW);
                }


                // 不能自動轉導
                HttpClient httpClient = HttpClient.newBuilder().sslContext(HttpUtil.disableWssValidation()).followRedirects(Redirect.NEVER)
                        .build();

                HttpRequest httpRequest = httpRequestBuilder.build();
                sb.append("\n ---- req Herder ---- ");
                httpRequest.headers().map().entrySet().forEach(m -> {
                    sb.append("\n" + m.getKey() + " :　" + m.getValue().stream()
                            .collect(Collectors.joining(", ")));
                });

                sb.append("\n ---- req Herder end ---- ");
                httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

                if (resourceURL.endsWith(".js") || resourceURL.endsWith(".woff2")) {
                    cacheMap.put(resourceURL, httpResponse);
                }
            }


            sb.append("\nstatus : " + httpResponse.statusCode());
            sb.append("\nHTTP protocol : " + httpResponse.version());
            sb.append("\n ---- resp Herder ---- ");

            // 將請求完成的header複製一份到response
            java.net.http.HttpHeaders headerNames = httpResponse.headers();
            headerNames.map().entrySet().forEach(m -> {
                String key = m.getKey();
                if (m.getKey() != null) {
                    m.getValue().forEach(v -> {
                        if (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                            if (!v.startsWith("jti")) {
                                response.setHeader(HttpHeaders.SET_COOKIE, v);
                            }
                        } else if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !":status".equals(key)
                                && !key.equalsIgnoreCase("Transfer-Encoding")) {
                            response.addHeader(key, v);
                            sb.append("\n" + key + " :　" + v);
                        }

                    });

                }
            });

            sb.append("\n ---- resp Herder end ---- ");
            response.setStatus(httpResponse.statusCode());
            response.setHeader("kbn-xsrf", "true");
        	//checkmarx, Missing HSTS Header
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
            

            // 將Kibana URL內容輸出
            OutputStream outputStream = response.getOutputStream();
            // 2024-10-21, Kibana 在 Menu 中連點會報出 null
            // org.springframework.web.context.request.async.AsyncRequestNotUsableException: ServletOutputStream failed to write: null
            // outputStream.write( ) 是由這個方法發出
            try {
            	//checkmarx, Reflected XSS All Clients
            	CheckmarxCommUtils.sanitizeForCheckmarx(httpResponse, outputStream);
            } catch (AsyncRequestNotUsableException e) {
                long total = 0;
                for (HttpResponse<byte[]> res : cacheMap.values()) {
                	//checkmarx, Reflected XSS All Clients
                    byte[] b = CheckmarxUtils.sanitizeForCheckmarx(res);
                    total += b.length;
                }

                StringBuffer errsb = new StringBuffer();
                errsb.append("\nkibana uri=" + resourceURL);
                errsb.append("AsyncRequestNotUsableException = " + StackTraceUtil.logTpiShortStackTrace(e) + "\n\n");
                errsb.append("current byte total = " + total + "\n\n");
                TPILogger.tl.warn(errsb.toString());
                cacheMap.clear();
                TPILogger.tl.info("Clear Kibana cache");
                return;
            }
            outputStream.flush();
            sb.append("\n resp body len : " + CheckmarxUtils.sanitizeForCheckmarx(httpResponse).length);

            sb.append("\n ===============================================");
            if (httpResponse.statusCode() >= 400 || httpResponse.statusCode() <= 0) {
                TPILogger.tl.error(sb.toString());
                if (cacheMap != null) {
                    cacheMap.clear();
                    TPILogger.tl.info("Clear Kibana cache");
                }
            }
            TPILogger.tl.trace(sb.toString());

            return;

        } catch (Exception e) {
            cacheMap.clear();
            TPILogger.tl.info("Clear Kibana cache");
            TPILogger.tl.error(sb.toString());
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            Thread.currentThread().interrupt();
        }
    }

    protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
        return tsmpSettingCacheProxy;
    }

    protected CApiKeyService getCapiKeyService() {
        return capiKeyService;
    }

    protected TsmpSettingService getTsmpSettingService() {
        return tsmpSettingService;
    }

}
