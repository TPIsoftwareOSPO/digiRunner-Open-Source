package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class KibanaService {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private CApiKeyService capiKeyService;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public KibanaService(TsmpSettingCacheProxy tsmpSettingCacheProxy, CApiKeyService capiKeyService,
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
			} catch (Exception e) {
				ByteArrayInputStream bi = new ByteArrayInputStream(
						TsmpDpAaRtnCode._1522.getDefaultMessage().getBytes());
				response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
				//checkmarx, Missing HSTS Header
				response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
		        
				IOUtils.copy(bi, response.getOutputStream());
				throw TsmpDpAaRtnCode._1522.throwing();
			}

			// 直接轉導 Kibana URL 不再登入
			response.addHeader("kbn-xsrf", "true");
			response.setStatus(HttpServletResponse.SC_FOUND);
			//checkmarx, Missing HSTS Header
			response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	        
			response.sendRedirect(reportURL);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	public HttpServletResponse getConvertResponse(Map<String, List<String>> respHeader, int status,
			HttpServletResponse httpRes) {

		httpRes.setStatus(status);
		respHeader.forEach((k, vs) -> {
			vs.forEach((v) -> {
				if (k != null) {
					if (!k.equalsIgnoreCase("Transfer-Encoding")) {
						httpRes.addHeader(k, v);
					}
				}
			});
		});
		return httpRes;
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
			logger.error(StackTraceUtil.logStackTrace(e1));
		}
		return strUrl;
	}

	public void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
			String payload) {
		try {
			String resourceURL = getKibanaURL() + request.getRequestURI();
			String querString = request.getQueryString();
			if (querString != null) {
				resourceURL = resourceURL + "?" + querString;
			}

			Enumeration<String> httpHeaderKeys = request.getHeaderNames();
			Map<String, List<String>> headers = new HashMap<>();

			// 將Kibana登入授權資料取出來放在header，會用在請求Kibana URL
			while (httpHeaderKeys.hasMoreElements()) {
				String key = httpHeaderKeys.nextElement();
				List<String> valueList = httpHeaders.get(key);
				headers.put(key, valueList);
			}
			// 使用 basic auth
			String un = getTsmpSettingService().getVal_KIBANA_USER();
			String pw = getTsmpSettingService().getVal_KIBANA_PWD();
			String encodUNPW = Base64Util.base64Encode((un + ":" + pw).getBytes());

			headers.put(HttpHeaders.AUTHORIZATION, Arrays.asList("Basic " + encodUNPW));

			String method = request.getMethod();
			HttpRespData respObj = null;

			// 請求Kibana URL
			if (method.equalsIgnoreCase("GET")) {
				respObj = HttpUtil.httpReqByGetList(resourceURL, headers, true, false);
			} else {
				respObj = HttpUtil.httpReqByRawDataList(resourceURL, "POST", payload, headers, true, false);
			}
			respObj.fetchByte(); // because Enable inputStream

			// 將請求完成的header複製一份到response
			response = getConvertResponse(respObj.respHeader, respObj.statusCode, response);

			// 因為HttpUtil增加Keepalive header會造成respObj.httpRespArray是null
			if (respObj.httpRespArray != null) {
				// 將Kibana URL內容輸出
				ByteArrayInputStream bi = new ByteArrayInputStream(respObj.httpRespArray);
				IOUtils.copy(bi, response.getOutputStream());
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
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
