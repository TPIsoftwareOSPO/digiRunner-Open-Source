package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import tpi.dgrv4.gateway.component.cache.proxy.ProxyMethodServiceCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.gateway.vo.AutoCacheRespVo;
import tpi.dgrv4.gateway.vo.FixedCacheVo;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class TSMPCServiceGet implements IApiCacheService{
	
	private CommForwardProcService commForwardProcService;
	private ObjectMapper objectMapper;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private MockApiTestService mockApiTestService;
	private TsmpSettingService tsmpSettingService;

	private HashMap<String, String> maskInfo ;

	@Autowired
	public TSMPCServiceGet(CommForwardProcService commForwardProcService, ObjectMapper objectMapper,
			ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy, MockApiTestService mockApiTestService,
			TsmpSettingService tsmpSettingService) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.objectMapper = objectMapper;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.mockApiTestService = mockApiTestService;
		this.tsmpSettingService = tsmpSettingService;
	}

	public ResponseEntity<?> forwardToGet(HttpHeaders httpHeaders, HttpServletRequest httpReq, 
			HttpServletResponse httpRes) throws Exception {
		try {
			
			String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			
			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			String reqUrl = httpReq.getRequestURI();
			//判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId); //CommForwardProcService(是否檢查 token , LogES)
			String aType = "R";
			if(cApiKeySwitch) {
				aType = "C";
			}
			
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start】--\n");
			// 檢查資料
			TsmpApiReg apiReg = getCommForwardProcService().getTsmpApiReg(httpReq);
			maskInfo = new HashMap<>();
			maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
			maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
			maskInfo.put("bodyMaskPolicyNum", String.valueOf(apiReg.getBodyMaskPolicyNum()));
			maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());

			maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
			maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
			maskInfo.put("headerMaskPolicyNum", String.valueOf(apiReg.getHeaderMaskPolicyNum()));
			maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());
			
			

			
			ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, null, true);
			
			//第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq tsmpcGetDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, "", "tsmpc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq tsmpcGetDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, "", "tsmpc", aType);
			
			// JWT 資料驗證有錯誤
			if(verifyResp != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, tsmpcGetDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, tsmpcGetDgrReqVo_rdb, respMbody);
				return verifyResp;
			}
	 
			String srcUrl = getCommForwardProcService().getSrcUrl(httpReq);
			
			if(!StringUtils.hasText(srcUrl)) {
				return null;
			}
			//判斷是否啟用Path Parameter選項
			boolean isURLRID = getCommForwardProcService().isURLRID(moduleName, apiId);
			//進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
			//取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
			//完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
			if (isURLRID) {

				srcUrl = srcUrl + getCommForwardProcService().getTsmpcPathParameter(reqUrl);
			}
			
			String queryStr = httpReq.getQueryString();
			if (null != queryStr) {
				srcUrl += "?" + queryStr;
			}
            
			int tokenPayload = apiReg.getFunFlag();
			
			// For API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, tsmpcGetDgrReqVo, tsmpcGetDgrReqVo_rdb, cApiKeySwitch);
			} else {
				return forwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, tsmpcGetDgrReqVo, tsmpcGetDgrReqVo_rdb, cApiKeySwitch);
			}
			
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			@RequestHeader HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq tsmpcGetDgrReqVo, TsmpApiLogReq tsmpcGetDgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {

		// 2. tsmpc req header / body
		// 3. tsmpc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, false);
		String reqMbody = ""; // 這個值是填充物
//		byte[] httpArray = null;
//		String httpRespStr = null;

		//2,3道是否走cache
		HttpRespData respObj = new HttpRespData();
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(tsmpcGetDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(tsmpcGetDgrReqVo, srcUrl, reqMbody);
		if(StringUtils.hasText(autoCacheId)) {//自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(tsmpcGetDgrReqVo);
			paramVo.setUuid(uuid);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				//此行因為httpRes不能放在callback,所以移到外層
				//httpRes = getCommForwardProcService().getConvertResponse(apiCacheRespVo.getRespHeader(), apiCacheRespVo.getStatusCode(), httpRes);
			}else {//cache發生未知錯誤,call api
				respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcGetDgrReqVo, reqMbody, uuid, false); 
			}
			// 說明 dgr-v4 流程使用, 沒有注入就不會引用
			//if (traceCodeUtil != null) { traceCodeUtil.logger(this); } 
		}else if(StringUtils.hasText(fixedCacheId)) {//固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if(cacheVo != null) {//走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, tsmpcGetDgrReqVo);
				if(isUpdate) {//更新紀錄
					respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcGetDgrReqVo, reqMbody, uuid, true); 
					//statusCode小於400才更新紀錄
					if(respObj.statusCode < 400) {
						cacheVo.setData(respObj.httpRespArray);
						cacheVo.setRespStr(respObj.respStr);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(respObj.respHeader);
						cacheVo.setStatusCode(respObj.statusCode);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					}else {//否則就取上次紀錄
						respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
						//httpRes = getCommForwardProcService().getConvertResponse(cacheVo.getRespHeader(), cacheVo.getStatusCode(), httpRes);
					}
				}else {//取得cache資料
					respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
					//httpRes = getCommForwardProcService().getConvertResponse(cacheVo.getRespHeader(), cacheVo.getStatusCode(), httpRes);
				}
			}else {//call api
				respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcGetDgrReqVo, reqMbody, uuid, true); 
				//statusCode小於400才紀錄
				if(respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(respObj.httpRespArray);
					cacheVo.setRespStr(respObj.respStr);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(respObj.respHeader);
					cacheVo.setStatusCode(respObj.statusCode);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}
			// 說明 dgr-v4 流程使用, 沒有注入就不會引用
			//if (traceCodeUtil != null) { traceCodeUtil.logger(this); } 
		}else {//call api
			respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcGetDgrReqVo, reqMbody, uuid, false); 
		}

		// 不論是 cache , 直接call, 統一在這裡 set RESPONSE HEADER
		httpRes = getCommForwardProcService().getConvertResponse(respObj.respHeader, respObj.statusCode, httpRes);

		//轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq, respObj.httpRespArray, respObj.respStr);
		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");
		
		int content_Length = 0;
		if (httpArray != null) {
			content_Length = httpArray.length;
			ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
			//http InputStream copy into Array
			IOUtils.copy(bi, httpRes.getOutputStream());
		}

		// print
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo, httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + resLog.toString());

		//第一組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, tsmpcGetDgrReqVo, httpRespStr, content_Length);
		// 第一組 RDB Resp
		getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, tsmpcGetDgrReqVo_rdb, httpRespStr, content_Length);
		
		return null;
	}
	
	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start TSMPC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End TSMPC-from-Backend For Cache】--\n");
			
			//第二組ES REQ
			TsmpApiLogReq tsmpcGetBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
	
			HttpRespData respObj = getHttpRespData(vo.getHeader(), vo.getSrcUrl());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;
			
			//第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcGetBackendReqVo, contentLength);
			
			return respObj;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}
	
	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, 
			String srcUrl, TsmpApiLogReq dgrReqVo, String reqMbody, String uuid, boolean isFixedCache) throws Exception{
		
		StringBuffer sb = new StringBuffer();
		if(isFixedCache) {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend For Fixed Cache】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend For Fixed Cache】--\n");
		}else {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend】--\n");
		}

		//第二組ES REQ
		TsmpApiLogReq tsmpcGetBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl, reqMbody);

		HttpRespData respObj = getHttpRespData(header, srcUrl);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		sb.append(respObj.getLogStr()); 
		TPILogger.tl.debug(sb.toString()); // can't read resp String

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes); // handle HTTP resp HEADER

		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		
		//第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcGetBackendReqVo, contentLength);
		
		return respObj;
	}
	
	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String reqUrl) throws IOException {

		StringBuffer tsmpcGet_log = new StringBuffer();
		
		String queryStr = httpReq.getQueryString();
		if (queryStr != null) {
			reqUrl += "?" + queryStr;
		}
		
		// print
		writeLogger(tsmpcGet_log, "--【URL】--");
		writeLogger(tsmpcGet_log, reqUrl);
		writeLogger(tsmpcGet_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(tsmpcGet_log, "【" + httpReq.getMethod() + "】\r\n");
		
		// print header
		writeLogger(tsmpcGet_log, "--【Http Req Header】--");
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
			writeLogger(tsmpcGet_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(tsmpcGet_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		return tsmpcGet_log;
	}
		
	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}
	
	private HttpRespData getHttpRespData(Map<String, List<String>> header, 
			String reqUrl) throws Exception {
		HttpRespData httpRespData = HttpUtil.httpReqByGetList(reqUrl, header, true, false, maskInfo);
		
		return httpRespData;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch);
	}
	
	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}

}
