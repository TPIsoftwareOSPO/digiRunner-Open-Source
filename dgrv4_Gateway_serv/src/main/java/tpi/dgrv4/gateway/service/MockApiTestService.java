package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.controller.VersionController;
import tpi.dgrv4.dpaa.vo.AA0302KeyVal;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.gateway.component.ResponseHandler;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public final class MockApiTestService {

	public static final String HTTP_HEADER_MOCK_TEST = "dgr-mock-test";

	private CommForwardProcService commForwardProcService;
	private VersionController versionController;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private ObjectMapper objectMapper;
    @Setter(onMethod_ = @Autowired)
    private ResponseHandler responseHandler;
	@Autowired
	public MockApiTestService(CommForwardProcService commForwardProcService, VersionController versionController,
			TsmpApiCacheProxy tsmpApiCacheProxy, ObjectMapper objectMapper) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.versionController = versionController;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.objectMapper = objectMapper;
	}

	public boolean checkIfMockTest(HttpHeaders httpHeaders) {
		List<String> values = httpHeaders.get(HTTP_HEADER_MOCK_TEST);
		return !CollectionUtils.isEmpty(values) && values.get(0).equals(Boolean.TRUE.toString());
	}

	public ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload, TsmpApiLogReq dgrReqVo,
			TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg) throws Exception {

		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, false);
		String reqMbody = "";
		HttpRespData respObj = callForwardApi(header, httpRes, srcUrl, dgrReqVo, reqMbody, uuid, false, httpReq, tsmpApiReg);

		httpRes = getCommForwardProcService().getConvertResponse(respObj.respHeader, respObj.statusCode, httpRes, tsmpApiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				respObj.httpRespArray, respObj.respStr);
		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");

//		ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);

		// http InputStream copy into Array
//		IOUtils.copy(bi, httpRes.getOutputStream());


        int contentLength = 0;
        if (httpArray != null) {
        		contentLength = httpArray.length;
            responseHandler
                    .response(httpRes)
                    .handle(httpArray)
                    .write();
        }

		// print
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, contentLength, null,
				httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End MOCK】--\n" + resLog.toString());

		// 第一組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrReqVo, httpRespStr, contentLength);
		// 第一組 RDB Resp
		getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrReqVo_rdb, httpRespStr, contentLength);

		return null;
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, String srcUrl,
			TsmpApiLogReq dgrReqVo, String reqMbody, String uuid, boolean isFixedCache, HttpServletRequest httpReq,
			TsmpApiReg tsmpApiReg) throws Exception {

		StringBuffer sb = new StringBuffer();
		sb.append("\n--【LOGUUID】【" + uuid + "】【Start Mock-to-Backend】--");
		sb.append("\n--【LOGUUID】【" + uuid + "】【End Mock-from-Backend】--\n");

		// 第二組ES REQ
		TsmpApiLogReq newApiLogReq = ServiceUtil.deepCopy(dgrReqVo, TsmpApiLogReq.class);
		newApiLogReq.setIgnore(true);
		TsmpApiLogReq backendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(newApiLogReq, header, srcUrl,
				reqMbody);

		HttpRespData respObj = getHttpRespData(header, srcUrl, dgrReqVo);
		sb.append(respObj.getLogStr());
		TPILogger.tl.debug(sb.toString()); // can't read resp String

		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
		Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid, srcUrl, respObj.respStr)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes, tsmpApiReg); // handle HTTP resp HEADER

		byte[] httpArray = respObj.respStr.getBytes();

		// 第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, backendReqVo, httpArray.length);

		return respObj;
	}

	private HttpRespData getHttpRespData(Map<String, List<String>> httpHeader, String reqUrl, TsmpApiLogReq dgrReqVo) throws Exception {
		HttpRespData respObj = new HttpRespData();
		
		// print
		respObj.logger("--【URL】--");
		respObj.logger("/dgrv4/version");
		respObj.logger("--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		respObj.logger("【GET】\r\n");
		
		// print Req header
		respObj.logger("--【Http Req Header】--");
		respObj.logger("--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		//自訂改掉這一段以下的code(可能用if..else),若mockStatusCode無值則執行queryVersion,有值自動帶自訂的的格式
		
		//自訂,moduleName和apiId從TsmpApiLogReq取得(此方法多一個參數傳入),資料從tsmp_api取得
		//,改respObj.respHeader,respObj.statusCode,respObj.respStr,LOG也要照印
		
		// Get Api Info from TsmpApiDao by ModuleName And Id
		String moduleName = dgrReqVo.getModuleName();
		String id = dgrReqVo.getTxid();
		TsmpApi api = geTsmpApiCacheProxy().findByModuleNameAndApiKey(moduleName, id);
		// Get ResponseEntity From Api or QueryVersion
		String mockStatusCode = api.getMockStatusCode();
		ResponseEntity<?> responseEntity;
		if (StringUtils.hasText(mockStatusCode)) {

			String mockHeadersStr = api.getMockHeaders();
			List<AA0302KeyVal> mockHeadersVo = getHeaders(mockHeadersStr);
			MultiValueMap<String, String> mockheadersMap = getMultiValueMapHeader(mockHeadersVo);

			String mockBody = api.getMockBody();
			int code = Integer.parseInt(mockStatusCode);
			
			if (!(StringUtils.hasText(mockBody))) {
				mockBody = "";
			}

			responseEntity = new ResponseEntity<>(mockBody, mockheadersMap, code);
		} else {
			responseEntity = getVersionController().queryVersion(null, null);
		}

		// print
		respObj.logger("--【Http Resp Header】--");
		respObj.logger("\tKey: http code, Value: " + responseEntity.getStatusCodeValue());
		
		HttpHeaders httpHeaders = responseEntity.getHeaders();
		respObj.respHeader = new HashMap<String, List<String>>(httpHeaders);
		for (Map.Entry<String, List<String>> entry : respObj.respHeader.entrySet()) {
			respObj.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		String server = Optional.ofNullable(httpHeaders.get("Server")).map(vList -> vList.get(0)).orElse(null);
		if (StringUtils.hasLength(server)) {
			respObj.logger("Server - " + server);
		}
		respObj.logger("--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		// prepare Http Result
		try {
			respObj.statusCode = responseEntity.getStatusCodeValue();
		} catch (Exception e) {
			StackTraceElement[] elements = e.getStackTrace();
			respObj.respStr = e.toString() + "\n\t" + elements[0].toString() + "\n\t" + elements[1].toString();
			return respObj;
		}
		
		// print
		respObj.logger("--【Http status code】--");
		respObj.logger("--" + respObj.statusCode);
		respObj.logger("--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		// get Stream
		if (responseEntity.hasBody()) {
			Object body = responseEntity.getBody();
			respObj.respStr = String.valueOf(body);	// It must be in JSON format
			respObj.httpRespArray = respObj.respStr.getBytes();
		}
		
		// print
		respObj.logger("--【Resp payload....Return....】--");
		return respObj;
	}

	private List<AA0302KeyVal> getHeaders(String json){

		List<AA0302KeyVal> list = null;
		if (json == null) {
			return list;
		}
		try {
			list = getObjectMapper().readValue(json, new TypeReference<List<AA0302KeyVal>>() {
			});
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return list;
	}
	
	private MultiValueMap<String, String> getMultiValueMapHeader(List<AA0302KeyVal> list){
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		if (list == null || list.isEmpty()) {
			return map;
		}

		list.forEach(aa0302 -> {
			map.add(aa0302.getKey(), aa0302.getValue());
		});
		return map;
	}
	protected TsmpApiCacheProxy geTsmpApiCacheProxy() {
		return this.tsmpApiCacheProxy;
	}
	
	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected VersionController getVersionController() {
		return versionController;
	}


}
