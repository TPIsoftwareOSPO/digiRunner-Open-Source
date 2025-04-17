package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.component.cache.proxy.ProxyMethodServiceCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.gateway.vo.AutoCacheRespVo;
import tpi.dgrv4.gateway.vo.FixedCacheVo;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class DGRCServiceDelete implements IApiCacheService{
	
	private CommForwardProcService commForwardProcService;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private DgrcRoutingHelper dgrcRoutingHelper;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private ObjectMapper objectMapper;
	private MockApiTestService mockApiTestService;
	
	private  Map<String, String> maskInfo ;
	
	private static final String LOGUUID = "【LOGUUID】";
	private static final String END_DGRC = "【END_DGRC】";

	@Autowired
	public DGRCServiceDelete(CommForwardProcService commForwardProcService, TsmpApiCacheProxy tsmpApiCacheProxy,
			ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy, DgrcRoutingHelper dgrcRoutingHelper,
			TsmpApiRegCacheProxy tsmpApiRegCacheProxy, TsmpSettingService tsmpSettingService, ObjectMapper objectMapper,
			MockApiTestService mockApiTestService) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.objectMapper = objectMapper;
		this.mockApiTestService = mockApiTestService;
	}

	@Async("async-workers-highway")
	public CompletableFuture<ResponseEntity<?>> forwardToDeleteAsyncFast(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
																	 String payload) throws Exception {
		return dgrcService(httpHeaders, httpReq, httpRes, payload);
	}

	@Async("async-workers")
	public CompletableFuture<ResponseEntity<?>> forwardToDeleteAsync(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
			String payload) throws Exception {
		return dgrcService(httpHeaders, httpReq, httpRes, payload);
	}

	private CompletableFuture<ResponseEntity<?>> dgrcService(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String payload) throws Exception {
		var response = forwardToDelete(httpHeaders, httpReq, httpRes, payload);
		return CompletableFuture.completedFuture(response);
	}

	public ResponseEntity<?> forwardToDelete(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
			String payload) throws Exception {
		try {
			
			if(payload == null) {
				payload = "";
			}
			
			String reqUrl = httpReq.getRequestURI();
			
			TsmpApiReg apiReg = null;

			if (null == httpReq.getAttribute(GatewayFilter.MODULE_NAME)) {
				throw new Exception("TSMP_API_REG module_name is null");
			}

			String dgrcDelModuleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcDelModuleName);
			Optional<TsmpApiReg> optTsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);		
			if (optTsmpApiReg.isPresent()) {
				apiReg = optTsmpApiReg.get(); 
				maskInfo = new HashMap<>();
				maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
				maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
				maskInfo.put("bodyMaskPolicyNum", String.valueOf( apiReg.getBodyMaskPolicyNum()));
				maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());
				
				maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
				maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
				maskInfo.put("headerMaskPolicyNum", String.valueOf( apiReg.getHeaderMaskPolicyNum()));
				maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());
		
			}else {
				throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcDelModuleName);
			}
			
			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			//判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcDelModuleName, apiId);
			String aType = "R";
			if(cApiKeySwitch) {
				aType = "C";
			}
			
			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders);
			TPILogger.tl.debug("\n--" + LOGUUID + "【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());
			
			// 檢查授權
			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, payload, false);
			
			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcDelDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload, "dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcDelDgrReqVoRdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, payload, "dgrc", aType);
						
			// 授權錯誤,則回覆錯誤訊息
			if(errRespEntity != null) {
				TPILogger.tl.debug("\n--" + LOGUUID + "【" + uuid + "】" + END_DGRC + "--\n" 
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVoRdb, respMbody);
				return errRespEntity;
			}
			
			//轉換 Request Body 格式
			JwtPayloadData jwtPayloadData = getCommForwardProcService().convertRequestBody(httpRes, httpReq, payload, false);
			errRespEntity = jwtPayloadData.errRespEntity;
			if(errRespEntity != null) {//資料有錯誤	
				TPILogger.tl.debug("\n--" + LOGUUID + "【" + uuid + "】" + END_DGRC + "--\n" 
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcDelDgrReqVoRdb, respMbody);
				return errRespEntity;
			}
			payload = jwtPayloadData.payloadStr;
			
			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);
				
				TPILogger.tl.debug("\n--" + LOGUUID + "【" + uuid + "】" + END_DGRC + "--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcDelDgrReqVoRdb, respMbody);
				return srcUrlListErrResp;
			}
					
			int tokenPayload = apiReg.getFunFlag();
			
			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcDelDgrReqVo,
						dgrcDelDgrReqVoRdb, cApiKeySwitch);
			}
			
			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcDelDgrReqVo, srcUrlList, payload);
			
			if (convertResponseBodyMap == null) {
				// Response alread commited HTTP code : 503 --> null
				// 不寫 ES , 不輸出 online console
				return null;
			}

			byte[] httpArray = null;
			String httpRespStr = null;
			if (convertResponseBodyMap != null) {
				httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
				httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");
			}
			
			int contentLength = 0;
			if (httpArray != null) {
				contentLength = httpArray.length;
				ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
				//http InputStream copy into Array
				try {
					IOUtils.copy(bi, httpRes.getOutputStream());
				} catch (AsyncRequestNotUsableException e) {
					TPILogger.tl.warn(httpRes.getStatus() + ", " + e.getLocalizedMessage());		
				}
			}
			
			// 印出第四道log
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, contentLength,
					maskInfo, httpReq);
			TPILogger.tl.debug("\n--" + LOGUUID + "【" + uuid + "】" + END_DGRC + "--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcDelDgrReqVo, httpRespStr, contentLength);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcDelDgrReqVoRdb, httpRespStr,
					contentLength);

			return null;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardToByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrcDelDgrReqVo, List<String> srcUrlList, String payload)
			throws Exception {
		// 失敗判定策略
		String failDiscoveryPolicy = apiReg.getFailDiscoveryPolicy();
		// 失敗處置策略, 0: 無重試; 1: 當調用目標URL失敗時,自動重試下一個目標URL
		String failHandlePolicy = apiReg.getFailHandlePolicy();
		
		Map<String, Object> convertResponseBodyMap = null;
		if ("1".equals(failHandlePolicy)) {// 1: 當調用目標URL失敗時,自動重試下一個目標URL
			TPILogger.tl.debug("srcUrl size:" + srcUrlList.size());
			for (int i = 0; i < srcUrlList.size(); i++) {
				String srcUrl = srcUrlList.get(i);
				String tryNumWord = (i + 1) + "/" + srcUrlList.size();
				TPILogger.tl.debug("Src Url(" + tryNumWord + "):" + srcUrl);
				convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, payload, srcUrl, uuid, tokenPayload,
						dgrcDelDgrReqVo, cApiKeySwitch, tryNumWord);
 
				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, payload, srcUrl, uuid, tokenPayload,
					dgrcDelDgrReqVo, cApiKeySwitch, null);
		}

		return convertResponseBodyMap; //Response alread commited HTTP code : 503 --> null
	}
	
	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrcDelDgrReqVo, Boolean cApiKeySwitch, String tryNumWord)
			throws Exception {

		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code
		
		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		
		// 2,3道是否走cache
		HttpRespData dgrcDelRespObj = new HttpRespData();
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcDelDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcDelDgrReqVo, srcUrl, reqMbody);
		if(StringUtils.hasText(autoCacheId)) {//自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcDelDgrReqVo);
			paramVo.setUuid(uuid);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				dgrcDelRespObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				//此行因為httpRes不能放在callback,所以移到外層
			}else {//cache發生未知錯誤,call api
				dgrcDelRespObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, false,
						tryNumWord);
			}
		}else if(StringUtils.hasText(fixedCacheId)) {//固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if(cacheVo != null) {//走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcDelDgrReqVo);
				if(isUpdate) {//更新紀錄
					dgrcDelRespObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid,
							true, tryNumWord);
					//statusCode大於等於200 且 小於400才更新紀錄
					if(dgrcDelRespObj.statusCode >= 200 && dgrcDelRespObj.statusCode < 400) {
						cacheVo.setData(dgrcDelRespObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcDelRespObj.respHeader);
						cacheVo.setStatusCode(dgrcDelRespObj.statusCode);
						cacheVo.setRespStr(dgrcDelRespObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					}else {//否則就取上次紀錄
						dgrcDelRespObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
					}
				}else {//取得cache資料
					dgrcDelRespObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
				}
			}else {//call api
				dgrcDelRespObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, true,
						tryNumWord);
				//statusCode大於等於200 且 小於400才更新紀錄
				if(dgrcDelRespObj.statusCode >= 200 && dgrcDelRespObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcDelRespObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcDelRespObj.respHeader);
					cacheVo.setStatusCode(dgrcDelRespObj.statusCode);
					cacheVo.setRespStr(dgrcDelRespObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}
			
		}else {//call api
			dgrcDelRespObj = this.callForwardApi(header, httpRes, srcUrl, dgrcDelDgrReqVo, reqMbody, uuid, false,
					tryNumWord);
		}
		
		httpRes = getCommForwardProcService().getConvertResponse(dgrcDelRespObj.respHeader, dgrcDelRespObj.statusCode, httpRes);

		//轉換 Response Body 格式
		return getCommForwardProcService().convertResponseBody(httpRes, httpReq, dgrcDelRespObj.httpRespArray, dgrcDelRespObj.respStr);
 
	}
	
	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--" + LOGUUID + "【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--" + LOGUUID + "【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");
			
			//第二組ES REQ
			TsmpApiLogReq dgrcDelBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			
			HttpRespData respObj = getHttpRespData(vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString()); 
	 
			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcDelBackendReqVo, contentLength);
			
			return respObj;

		}catch(Exception dgrcDelExc) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(dgrcDelExc));
			return null;
		}
	}
	
	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, String srcUrl,
			TsmpApiLogReq dgrReqVo, String payload, String uuid, boolean isFixedCache, String tryNumWord)
			throws Exception {
		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		} 
		
		StringBuffer dgrcDelSb = new StringBuffer();
		if (isFixedCache) {
			dgrcDelSb.append("\n--" + LOGUUID + "【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			dgrcDelSb
					.append("\n--" + LOGUUID + "【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcDelSb.append("\n--" + LOGUUID + "【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			dgrcDelSb.append("\n--" + LOGUUID + "【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}
		
		//第二組ES REQ
		TsmpApiLogReq dgrcDelBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl, payload);
		HttpRespData respObj = getHttpRespData(header, srcUrl, payload);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		dgrcDelSb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcDelSb.toString()); 
		
		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes);
 
		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		
		//第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcDelBackendReqVo, contentLength);
		
		return respObj;
	}
	
	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders) throws IOException {
		StringBuffer dgrcDelLog = new StringBuffer();
		
		// print
		writeLogger(dgrcDelLog, "--【URL】--");
		writeLogger(dgrcDelLog, httpReq.getRequestURI());
		writeLogger(dgrcDelLog, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcDelLog, "【" + httpReq.getMethod() + "】\r\n");
		
		// print header
		writeLogger(dgrcDelLog, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			String value = null;
			if (!CollectionUtils.isEmpty(valueList)) {
				String tmpValue = valueList.toString();
				// [ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
				tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
				value = getCommForwardProcService().convertAuth(key, tmpValue, maskInfo);
			}
			writeLogger(dgrcDelLog, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcDelLog, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		
		return dgrcDelLog;
	}
		
	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}
	
	private HttpRespData getHttpRespData(Map<String, List<String>> header, 
			String reqUrl, String payload) throws Exception {
		return HttpUtil.httpReqByRawDataList(reqUrl, "DELETE", payload,header, true, false, maskInfo);
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}
	
	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVoRdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVoRdb, cApiKeySwitch);
	}
	
	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}
	
	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}
	
	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}
}
