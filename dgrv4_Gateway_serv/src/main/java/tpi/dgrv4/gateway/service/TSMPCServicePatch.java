package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
@Deprecated
@Service
public class TSMPCServicePatch {

	private CommForwardProcService commForwardProcService;
	private MockApiTestService mockApiTestService;
	private ObjectMapper objectMapper;
	
	private HashMap<String, String> maskInfo ;
	
	@Autowired
	public TSMPCServicePatch(CommForwardProcService commForwardProcService, MockApiTestService mockApiTestService,
			ObjectMapper objectMapper) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.mockApiTestService = mockApiTestService;
		this.objectMapper = objectMapper;
	}

	public ResponseEntity<?> forwardToPatch(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
			String payload) throws Exception {
		
		if(payload == null) {
			payload = "";
		}
		
		String reqUrl = httpReq.getRequestURI();

		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
		String tsmpcPatch_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		
		// 1. req header / body
		// print log
		String uuid = UUID.randomUUID().toString();
		//判斷是否需要cApikey
		boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(tsmpcPatch_moduleName, apiId);
		String aType = "R";
		if(cApiKeySwitch) {
			aType = "C";
		}
		//檢查資料
		TsmpApiReg apiReg = getCommForwardProcService().getTsmpApiReg(httpReq);
		maskInfo = new HashMap<>();
		maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
		maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
		maskInfo.put("bodyMaskPolicyNum", String.valueOf( apiReg.getBodyMaskPolicyNum()));
		maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());
		
		maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
		maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
		maskInfo.put("headerMaskPolicyNum", String.valueOf( apiReg.getHeaderMaskPolicyNum()));
		maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());		
		StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());



		
		ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, payload, false);
		
		//第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
		TsmpApiLogReq tsmpcPatchDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload, "tsmpc", aType);
		// 第一組 RDB Req
		TsmpApiLogReq tsmpcPatchDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, payload, "tsmpc", aType);
					
		// JWT 資料驗證有錯誤
		if(errRespEntity != null) {
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
			//第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcPatchDgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcPatchDgrReqVo_rdb, respMbody);
			return errRespEntity;
		}
		
		//轉換 Request Body 格式
		JwtPayloadData jwtPayloadData = getCommForwardProcService().convertRequestBody(httpRes, httpReq, payload, false);
		errRespEntity = jwtPayloadData.errRespEntity;
		if(errRespEntity != null) {//資料有錯誤	
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
			//第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcPatchDgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcPatchDgrReqVo_rdb, respMbody);
			return errRespEntity;
		}
		payload = jwtPayloadData.payloadStr;

		String tsmpcPatch_srcUrl = getCommForwardProcService().getSrcUrl(httpReq);
		if(!StringUtils.hasText(tsmpcPatch_srcUrl)) {
			return null;
		}
		//判斷是否啟用Path Parameter選項
        boolean isURLRID = getCommForwardProcService().isURLRID(tsmpcPatch_moduleName, apiId);
		//進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
		//取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
		//完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
        if (isURLRID) 
        	tsmpcPatch_srcUrl = tsmpcPatch_srcUrl + commForwardProcService.getTsmpcPathParameter(reqUrl);	

		int tokenPayload = apiReg.getFunFlag();
		
		// For API mock test
		boolean isMockTest = checkIfMockTest(httpHeaders);
		if (isMockTest) {
			return mockForwardTo(httpReq, httpRes, httpHeaders, tsmpcPatch_srcUrl, uuid, tokenPayload, tsmpcPatchDgrReqVo, tsmpcPatchDgrReqVo_rdb, cApiKeySwitch);
		} else {
			return forwardTo(httpReq, httpRes, httpHeaders, payload, tsmpcPatch_srcUrl, uuid, tokenPayload, tsmpcPatchDgrReqVo, tsmpcPatchDgrReqVo_rdb, cApiKeySwitch);
		}

	}

	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
		      @RequestHeader HttpHeaders httpHeaders, String payload, String reqUrl, String uuid, int tokenPayload,
		      TsmpApiLogReq tsmpcPatchDgrReqVo, TsmpApiLogReq tsmpcPatchDgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {

		
		// 1. req header / body
		// print
		StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());
		
		// 2. tsmpc req header / body
		// 3. tsmpc resp header / body / code
		
		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, false);

		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--" 
					+ "\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend】--\n");
		
		HttpRespData respObj = getHttpRespData(httpReq, header, reqUrl, payload);
		respObj.fetchByte(maskInfo);
		
		TPILogger.tl.debug(respObj.getLogStr()); 
		
		httpRes.setStatus(respObj.statusCode);		
		respObj.respHeader.remove("Transfer-Encoding"); //它不能回傳
//		respObj.respHeader.forEach((k, vs)->{
//			vs.forEach((v)->{
//				httpRes.addHeader(k, v);
//			});			
//		});
		
		// 4. resp header / body / code
		httpRes = getCommForwardProcService().getConvertResponse(respObj.respHeader, respObj.statusCode, httpRes);

		//轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq, respObj.httpRespArray, respObj.respStr);
		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");
		ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
		//http InputStream copy into Array
		IOUtils.copy(bi, httpRes.getOutputStream());
//		String httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		// print
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, contentLength, new HashMap<>(), httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + resLog.toString());
		
		return null;
	}
	
	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload, String reqUrl) throws IOException {
		StringBuffer tsmpcPatch_log = new StringBuffer();
		
		// print
		writeLogger(tsmpcPatch_log, "--【URL】--");
		writeLogger(tsmpcPatch_log, httpReq.getRequestURI());
		writeLogger(tsmpcPatch_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(tsmpcPatch_log, "【" + httpReq.getMethod() + "】\r\n");
		
		// print header
		writeLogger(tsmpcPatch_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			String value = null;
			if (!CollectionUtils.isEmpty(valueList)) {
				String tmpValue = valueList.toString();
				//[ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
				tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
				 value = getCommForwardProcService().convertAuth(key, tmpValue, maskInfo);
			}
			writeLogger(tsmpcPatch_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(tsmpcPatch_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		// print body
		writeLogger(tsmpcPatch_log, "--【Req payload / Form Data】");
		writeLogger(tsmpcPatch_log, getCommForwardProcService().maskBody(maskInfo, payload));
		writeLogger(tsmpcPatch_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		return tsmpcPatch_log;
	}
	
	private StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int content_Length) throws IOException {
		StringBuffer log = new StringBuffer();
		
		// print header
		writeLogger(log, "--【Http Resp Header】--");
		List<String> headerName = httpRes.getHeaderNames().stream().distinct().collect(Collectors.toList());//移除重複的 HeaderName
		headerName.forEach((k)->{
			Collection<String> valueList = httpRes.getHeaders(k);
			String tmpValue = valueList.toString();
			//[ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
			tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
			writeLogger(log, "\tKey: " + k + ", Value: " + tmpValue);
		});
		writeLogger(log, "\tKey: " + "getStatus" + ", Value: " + httpRes.getStatus());
		writeLogger(log, "\tKey: " + "content-Length" + ", Value: " + content_Length);
		writeLogger(log, "\tKey: " + "getCharacterEncoding" + ", Value: " + httpRes.getCharacterEncoding());
		writeLogger(log, "\tKey: " + "getContentType" + ", Value: " + httpRes.getContentType());
		writeLogger(log, "\tKey: " + "getLocale" + ", Value: " + httpRes.getLocale());
		
	    writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print http code
	    writeLogger(log, "--【Http status code】--");
	    writeLogger(log, "--" + httpRes.getStatus());
	    writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		// print body
		writeLogger(log, "--【Resp payload / Form Data】");
		writeLogger(log, getCommForwardProcService().maskBody(maskInfo, httpRespStr));
		writeLogger(log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		 
		return log;
	}
	
	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}
	
	protected HttpRespData getHttpRespData(HttpServletRequest req, Map<String, List<String>> header, 
			String reqUrl, String payload) throws Exception {
		
		String httpMethod = req.getMethod();
		
		HttpRespData httpRespData = HttpUtil.httpReqByRawDataList(reqUrl, httpMethod, payload, header, true, false, maskInfo);
		
		return httpRespData;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch);
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}
