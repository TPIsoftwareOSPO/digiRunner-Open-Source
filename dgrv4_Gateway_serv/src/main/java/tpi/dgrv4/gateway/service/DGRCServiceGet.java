package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
public class DGRCServiceGet implements IApiCacheService{
	
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private ObjectMapper objectMapper;
	private CommForwardProcService commForwardProcService;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private DgrcRoutingHelper dgrcRoutingHelper;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private MockApiTestService mockApiTestService;
	
	private static Map<String, String> maskInfo ;

	@Autowired
	public DGRCServiceGet(TsmpApiCacheProxy tsmpApiCacheProxy, ObjectMapper objectMapper,
			CommForwardProcService commForwardProcService, ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy,
			DgrcRoutingHelper dgrcRoutingHelper, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			TsmpSettingService tsmpSettingService, MockApiTestService mockApiTestService) {
		super();
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.objectMapper = objectMapper;
		this.commForwardProcService = commForwardProcService;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.mockApiTestService = mockApiTestService;
	}

	@Async("async-workers-highway")
	public CompletableFuture<ResponseEntity<?>> forwardToGetAsyncFast(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes) throws Exception {
		var response = forwardToGet(httpHeaders, httpReq, httpRes);
		return CompletableFuture.completedFuture(response);
	}
	
	@Async("async-workers")
	public CompletableFuture<ResponseEntity<?>> forwardToGetAsyncSlow(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes) throws Exception {
		var response = forwardToGet(httpHeaders, httpReq, httpRes);
		return CompletableFuture.completedFuture(response);
	}

	public ResponseEntity<?> forwardToGet(HttpHeaders httpHeaders, HttpServletRequest httpReq, 
			HttpServletResponse httpRes) throws Exception {
		try {
			String reqUrl = httpReq.getRequestURI();
			
			TsmpApiReg apiReg = null;
			if (null == httpReq.getAttribute(GatewayFilter.MODULE_NAME)) {
				throw new Exception("TSMP_API_REG module_name is null");
			}
			String dgrcGet_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcGet_moduleName);
			Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);		
			if (opt_tsmpApiReg.isPresent()) {
				apiReg = opt_tsmpApiReg.get(); 
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
				throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcGet_moduleName);
			}
			
			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			
			//判斷是否需要cApikey			
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcGet_moduleName, apiId);
			String aType = "R";
			if(cApiKeySwitch) {
				aType = "C";
			}
			
			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());

			// 檢查授權
			ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, null, true);
			
			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcGetDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, "", "dgrc", aType);
			// 第一組RDB REQ
			TsmpApiLogReq dgrcGetDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, "", "dgrc", aType);
			
			// 授權錯誤,則回覆錯誤訊息
			if(verifyResp != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
				//第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, dgrcGetDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, dgrcGetDgrReqVo_rdb, respMbody);
				return verifyResp;
			}
			
			// 計算依來源IP取得轉發的目地的
			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);

			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);

				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcGetDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcGetDgrReqVo_rdb, respMbody);
				return srcUrlListErrResp;
			}
			
			// 目標URL加上 Query String
			srcUrlList = getSrcUrlListAddQueryString(httpReq, srcUrlList);

			int tokenPayload = apiReg.getFunFlag();
			
			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcGetDgrReqVo,
						dgrcGetDgrReqVo_rdb, cApiKeySwitch);
			}

			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcGetDgrReqVo, dgrcGetDgrReqVo_rdb, srcUrlList);
			
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
 
			int content_Length = 0;
			if (httpArray != null) {
				content_Length = httpArray.length;
				ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
				// http InputStream copy into Array
				try {
					IOUtils.copy(bi, httpRes.getOutputStream());
				} catch (AsyncRequestNotUsableException e) {
					TPILogger.tl.warn(httpRes.getStatus() + ", " + e.getLocalizedMessage());		
				}
			}
			
			// 印出第四道log
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length,
					maskInfo, httpReq);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcGetDgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcGetDgrReqVo_rdb, httpRespStr,
					content_Length);
			
			return null;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}
	
	/**
	 * 目標URL加上 Query String
	 */
	private List<String> getSrcUrlListAddQueryString(HttpServletRequest httpReq, List<String> srcUrlList) {
		List<String> srcUrlList2 = new ArrayList<>();
		String queryStr = httpReq.getQueryString();
		for (String srcUrl : srcUrlList) {
			if (StringUtils.hasLength(queryStr)) {
				srcUrl += "?" + queryStr;
			}
			srcUrlList2.add(srcUrl);
		}

		return srcUrlList2;
	}
	
	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardToByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrcGetDgrReqVo, TsmpApiLogReq dgrcGetDgrReqVo_rdb, List<String> srcUrlList)
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
				convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
						dgrcGetDgrReqVo, dgrcGetDgrReqVo_rdb, cApiKeySwitch, tryNumWord);

				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
					dgrcGetDgrReqVo, dgrcGetDgrReqVo_rdb, cApiKeySwitch, null);
		}

		return convertResponseBodyMap; //Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrcGetDgrReqVo, TsmpApiLogReq dgrcGetDgrReqVo_rdb, Boolean cApiKeySwitch, String tryNumWord)
			throws Exception {

		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders, tokenPayload,cApiKeySwitch, uuid, srcUrl);
		String reqMbody = "";
		HttpRespData dgrcGet_respObj = new HttpRespData();

		//2,3道是否走cache
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcGetDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcGetDgrReqVo, srcUrl, reqMbody);
		if(StringUtils.hasText(autoCacheId)) {//自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcGetDgrReqVo);
			paramVo.setUuid(uuid);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				dgrcGet_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());

				//此行因為httpRes不能放在callback,所以移到外層
			}else {//cache發生未知錯誤,call api
				dgrcGet_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcGetDgrReqVo, reqMbody, uuid, false,
						tryNumWord);
			}
		}else if(StringUtils.hasText(fixedCacheId)) {//固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if(cacheVo != null) {//走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcGetDgrReqVo);
				if(isUpdate) {//更新紀錄
					dgrcGet_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcGetDgrReqVo, reqMbody, uuid,
							true, tryNumWord);
		 
					//statusCode大於等於200 且 小於400才更新紀錄
					if(dgrcGet_respObj.statusCode >= 200 && dgrcGet_respObj.statusCode < 400) {
						cacheVo.setData(dgrcGet_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcGet_respObj.respHeader);
						cacheVo.setStatusCode(dgrcGet_respObj.statusCode);
						cacheVo.setRespStr(dgrcGet_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					}else {//否則就取上次紀錄
						dgrcGet_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
					}
				}else {//取得cache資料
					dgrcGet_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(), cacheVo.getRespHeader());
				}
			}else {//call api
				dgrcGet_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcGetDgrReqVo, reqMbody, uuid, true, tryNumWord);
				//statusCode大於等於200 且 小於400才紀錄
				if(dgrcGet_respObj.statusCode >= 200 && dgrcGet_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcGet_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcGet_respObj.respHeader);
					cacheVo.setStatusCode(dgrcGet_respObj.statusCode);
					cacheVo.setRespStr(dgrcGet_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}
			
		} else {// call api
			dgrcGet_respObj = this.callForwardApi(header, httpRes, srcUrl, dgrcGetDgrReqVo, reqMbody, uuid, false,
					tryNumWord);
		}
		
		httpRes = getCommForwardProcService().getConvertResponse(dgrcGet_respObj.respHeader, dgrcGet_respObj.statusCode,
				httpRes);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				dgrcGet_respObj.httpRespArray, dgrcGet_respObj.respStr);
 
		return convertResponseBodyMap; //Response alread commited HTTP code : 503 --> null
	}
	
	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");
			
			//第二組ES REQ
			TsmpApiLogReq dgrcGetBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHeader(), vo.getSrcUrl(), null);
			boolean isSse = vo.getHeader().keySet().stream()
					.anyMatch(key -> key.equalsIgnoreCase("isSse"));
			respObj.fetchByte(maskInfo,isSse); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());
	
			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;
					
			//第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcGetBackendReqVo, contentLength);
			
			return respObj;
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}
	
	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, String srcUrl,
			TsmpApiLogReq dgrReqVo, String reqMbody, String uuid, boolean isFixedCache, String tryNumWord)
			throws Exception {

		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}
		
		StringBuffer dgrcGet_sb = new StringBuffer();
		if (isFixedCache) {
			dgrcGet_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			dgrcGet_sb
					.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcGet_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			dgrcGet_sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}

		//第二組ES REQ
		TsmpApiLogReq dgrcGetBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl,
				"");
		HttpRespData respObj = getHttpRespData(header, srcUrl,httpRes.getOutputStream());
		boolean isSse = header.keySet().stream()
				.anyMatch(key -> key.equalsIgnoreCase("isSse"));

		respObj.fetchByte(maskInfo ,isSse); // because Enable inputStream
		dgrcGet_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcGet_sb.toString());

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes);

		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		
		// 第二組ES RESP
		/*
		 * 當調用目標URL重試多次,有多筆2,3道資料時,ES的_id會重複, 
		 * 所以最終只會紀錄最後打的2,3道(因_id相同, 變成update),
		 * 目前先維持這樣
		 */
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcGetBackendReqVo, contentLength);
		
		return respObj;
	}
	
	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String reqUrl) throws IOException {

		StringBuffer dgrcGet_log = new StringBuffer();
		
		String queryStr = httpReq.getQueryString();
		if (queryStr != null) {
			reqUrl += "?" + queryStr;
		}
		
		// print
		writeLogger(dgrcGet_log, "--【URL】--");
		writeLogger(dgrcGet_log, reqUrl);
		writeLogger(dgrcGet_log, "--【End】--\r\n");
		writeLogger(dgrcGet_log, "【" + httpReq.getMethod() + "】\r\n");
		
		// print header
		writeLogger(dgrcGet_log, "--【Http Req Header】--");
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
			writeLogger(dgrcGet_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcGet_log, "--【End】--\r\n");
		
		return dgrcGet_log;
	}
		
	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}
	
	private HttpRespData getHttpRespData(Map<String, List<String>> header, 
			String reqUrl, OutputStream sseOutput) throws Exception {
		HttpRespData dgrcGet_httpRespData = HttpUtil.httpReqByGetList(reqUrl, header, true, false, maskInfo, sseOutput);
		
		return dgrcGet_httpRespData;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}
	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}
	
	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			HttpHeaders httpHeaders, String srcUrl, String uuid, 
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch);
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
