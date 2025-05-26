package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.SafeHttpMethod;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.composer.ComposerService;
import tpi.dgrv4.dpaa.vo.AA0312Req;
import tpi.dgrv4.dpaa.vo.AA0312Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AA0312Service {

	private TPILogger logger = TPILogger.tl;

	private ComposerService composerService;
	private ObjectMapper objectMapper;

	@Autowired
	public AA0312Service(ComposerService composerService, ObjectMapper objectMapper) {
		super();
		this.composerService = composerService;
		this.objectMapper = objectMapper;
	}

	public AA0312Resp testAPI(TsmpAuthorization auth, AA0312Req req) {
		AA0312Resp resp = null;
		try {
			checkParams(req);
			String reqUrl = getUrl(req.getTestURL());
			Map<String, String> httpHeader = getHeaders(req.getHeaderList());
			HttpRespData httpRespData = getHttpRespData(req, httpHeader, reqUrl);
			
			TPILogger.tl.debug(httpRespData.getLogStr());
			
			resp = getAA0312Resp(httpRespData);
 
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected SafeHttpMethod checkParams(AA0312Req req) {
		String method = req.getMethod();
		SafeHttpMethod httpMethod = null;
		try{
			httpMethod = SafeHttpMethod.safeValueOf(method, true);
		}catch (IllegalArgumentException e){
			throw TsmpDpAaRtnCode._1352.throwing("{{method}}");
		}
		return httpMethod;
	}

	protected String getUrl(String testURL) {
		if (testURL.startsWith("http")) {
			return testURL;
		}
		String host = getComposerIP();
		return String.format("%s/%s", //
			host.replaceAll("/$", ""),
			testURL.replaceAll("^/", "")
		);
	}

	protected String getComposerIP() {
		List<String> composerIPs = getComposerService().getComposerIPs();
		if (CollectionUtils.isEmpty(composerIPs)) {
			this.logger.debug("Cannot get composer IPs");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		for(String host : composerIPs) {
			if (!StringUtils.isEmpty(host)) {
				return host;
			}
		}
		this.logger.debug("No avaliable composer IP");
		throw TsmpDpAaRtnCode._1297.throwing();
	}

	protected Map<String, String> getHeaders(List<Map<String, String>> headerList) {
		Map<String, String> headers = new HashMap<>();
		if (!CollectionUtils.isEmpty(headerList)) {
			try {
				this.logger.debug(String.format("HEADERS: \n%s", getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerList)));
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}

			headerList.forEach((map) -> {
				map.forEach((key, value) -> {
					headers.put(key, value);
				});
			});
		}
		return headers;
	}
	
	/**
	 * 取得request的種類:
	 * (1)GET : reqType=1 (httpReqByGet), 沒有請求表身
	 * (2)POST/PUT...etc
	 *   Body : reqType=2 (httpReqByRawData), 請求表身(純文字) - bodyText
	 *   Form : reqType=3 (httpReqByFormData), 請求表身(表單) - paramList
	 */
	protected Integer getReqType(AA0312Req req) {
		String httpMethod = req.getMethod();
		String bodyText = req.getBodyText();
		List<Map<String, String>> paramList = req.getParamList();
		
		if(HttpMethod.GET.name().equals(httpMethod)) {
			return 1;
		}else if (!StringUtils.isEmpty(bodyText)) {//請求表身(純文字)
			return 2;
		} else if (!CollectionUtils.isEmpty(paramList)) {//請求表身(表單)
			return 3;
		} else {//沒有請求表身
			return 2;
		}
	}
	
	protected HttpRespData getHttpRespData(AA0312Req req, Map<String, String> httpHeader, String reqUrl) throws Exception {
		HttpRespData httpRespData = null;
		String httpMethod = req.getMethod();
		Integer reqType = getReqType(req);
		if(reqType == 1) {
			httpRespData = HttpUtil.httpReqByGet(reqUrl, httpHeader, false);
			//System.err.println("\n<< CallApi Response Data >>\n" + httpRespData.respStr);
			
		}else if(reqType == 2) {
			String rawData = req.getBodyText();
			if(rawData == null) {
				rawData = "";
			}
			httpRespData = HttpUtil.httpReqByRawData(reqUrl, httpMethod, rawData, httpHeader, false);
			
		}else if(reqType == 3) {
			List<Map<String, String>> paramList = req.getParamList();
			try {
				this.logger.debug(String.format("PARAMS: \n%s", getObjectMapper().writerWithDefaultPrettyPrinter().
						writeValueAsString(paramList)));
			} catch (Exception e) {}
			Map<String, String> partContentTypes = new HashMap<>();
			Map<String, String> formData = new HashMap<>();
			paramList.forEach((param) -> 
				param.forEach((key, value) -> {
					formData.put(key, value);
					partContentTypes.put(key, "");
				})
			);
			
			httpRespData = HttpUtil.httpReqByFormData(reqUrl, httpMethod, formData, httpHeader, false, partContentTypes);
		}
		
		

		return httpRespData;
	}
	
	protected AA0312Resp getAA0312Resp(HttpRespData httpRespData) {
		AA0312Resp resp = new AA0312Resp();
		List<Map<String, List<String>>> headerList = new ArrayList<>();
		if(httpRespData.respHeader != null) {
			List<String> temp = httpRespData.respHeader.remove(null);
			if(temp != null) {
				httpRespData.respHeader.put("null", temp);
			}
		}
		headerList.add(httpRespData.respHeader);
		
		resp.setHeaderList(headerList);
		resp.setResBody(httpRespData.respStr);
		resp.setResStatus(httpRespData.statusCode);
		return resp;
	}
 
	protected ComposerService getComposerService() {
		return this.composerService;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
}