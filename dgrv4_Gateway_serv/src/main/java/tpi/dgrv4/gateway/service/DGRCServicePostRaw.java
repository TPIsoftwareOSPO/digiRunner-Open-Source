package tpi.dgrv4.gateway.service;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import com.fasterxml.jackson.databind.ObjectMapper;

import oshi.util.tuples.Pair;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DgrProtocol;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.component.cache.proxy.ProxyMethodServiceCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.gateway.vo.AutoCacheRespVo;
import tpi.dgrv4.gateway.vo.FixedCacheVo;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.httpu.utils.CertificateInfo;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.httpu.utils.HttpUtil2;

@Service
public class DGRCServicePostRaw implements IApiCacheService {

	private CommForwardProcService commForwardProcService;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private ObjectMapper objectMapper;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private MockApiTestService mockApiTestService;
	private DgrcRoutingHelper dgrcRoutingHelper;

	public Map<String, String> maskInfo;
	private static final String IS_SSE = "isSse";

	@Value("${server.ssl.enabled:false}")
	private boolean sslEnabled;

	@Value("${server.port:8080}")
	private int serverPort;

	@Autowired
	public DGRCServicePostRaw(CommForwardProcService commForwardProcService, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			ObjectMapper objectMapper, ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy,
			TsmpSettingService tsmpSettingService, MockApiTestService mockApiTestService,
			DgrcRoutingHelper dgrcRoutingHelper) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.objectMapper = objectMapper;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.mockApiTestService = mockApiTestService;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
	}

	@Async("async-workers-highway")
	public CompletableFuture<ResponseEntity<?>> forwardToPostRawDataAsyncFast(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
																			  String payload) throws Exception {
		var response = forwardToPostRawData(httpHeaders, httpReq, httpRes, payload);
		return CompletableFuture.completedFuture(response);
	}

	@Async("async-workers")
	public CompletableFuture<ResponseEntity<?>> forwardToPostRawDataAsync(HttpHeaders httpHeaders,
			HttpServletRequest httpReq, HttpServletResponse httpRes,
			String payload) throws Exception {
		var response = forwardToPostRawData(httpHeaders, httpReq, httpRes, payload);
		return CompletableFuture.completedFuture(response);
	}
	
	public ResponseEntity<?> forwardToPostRawData(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpRes,
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
			String dgrcPostRaw_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcPostRaw_moduleName);
			Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
			if (opt_tsmpApiReg.isPresent()) {
				apiReg = opt_tsmpApiReg.get();
				ResponseEntity<?> allowMethodErrRespEntity = getCommForwardProcService().checkMethod(apiReg, httpReq);
				if (allowMethodErrRespEntity != null) {
					return allowMethodErrRespEntity;
				}
				maskInfo = new HashMap<>();
				maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
				maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
				maskInfo.put("bodyMaskPolicyNum", String.valueOf(apiReg.getBodyMaskPolicyNum()));
				maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());

				maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
				maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
				maskInfo.put("headerMaskPolicyNum", String.valueOf(apiReg.getHeaderMaskPolicyNum()));
				maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());
			} else {
				throw new Exception(
						"TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcPostRaw_moduleName);
			}

			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();

			// 判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcPostRaw_moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}

			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());

			// 檢查授權
			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg, payload, false);

			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcPostRawDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload,
					"dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcPostRawDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq,
					payload, "dgrc", aType);

			// 授權錯誤,則回覆錯誤訊息
			if (errRespEntity != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcPostRawDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcPostRawDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}

			// 轉換 Request Body 格式
			JwtPayloadData jwtPayloadData = getCommForwardProcService().convertRequestBody(httpRes, httpReq, payload,
					false);
			errRespEntity = jwtPayloadData.errRespEntity;
			if (errRespEntity != null) {// 資料有錯誤
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcPostRawDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcPostRawDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}

			payload = jwtPayloadData.payloadStr;

			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);

				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcPostRawDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcPostRawDgrReqVo_rdb,
						respMbody);
				return srcUrlListErrResp;
			}

			int tokenPayload = apiReg.getFunFlag();

			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcPostRawDgrReqVo,
						dgrcPostRawDgrReqVo_rdb, cApiKeySwitch, apiReg);
			}

			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcPostRawDgrReqVo, dgrcPostRawDgrReqVo_rdb, srcUrlList, payload, apiReg);
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
				// http InputStream copy into Array
				try {
					// 檢查非同步請求是否仍然有效
					if (!httpRes.isCommitted()) {
						IOUtils.copy(new ByteArrayInputStream(httpArray), httpRes.getOutputStream());
					} else {
						TPILogger.tl.warn("Response already committed or async completed");
						return null;
					}
				} catch (AsyncRequestNotUsableException e) {
					TPILogger.tl.warn(httpRes.getStatus() + ", " + StackTraceUtil.logStackTrace(e));
					return null;
				}
			}

			// 印出第四道log
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo,
					httpReq);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcPostRawDgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcPostRawDgrReqVo_rdb, httpRespStr,
					content_Length);

			return null;
		} catch (AsyncRequestNotUsableException e) {
			return null; // 不再拋出例外，避免影響其他請求
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardToByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrcPostRawDgrReqVo, TsmpApiLogReq dgrcPostRawDgrReqVo_rdb, List<String> srcUrlList,
			String payload, TsmpApiReg tsmpApiReg) throws Exception {
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
						dgrcPostRawDgrReqVo, dgrcPostRawDgrReqVo_rdb, cApiKeySwitch, tryNumWord, tsmpApiReg);

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
					dgrcPostRawDgrReqVo, dgrcPostRawDgrReqVo_rdb, cApiKeySwitch, null, tsmpApiReg);
		}

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
											@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
											TsmpApiLogReq dgrcPostRawDgrReqVo, TsmpApiLogReq dgrcPostRawDgrReqVo_rdb, Boolean cApiKeySwitch,
											String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {
		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		HttpRespData respObj = new HttpRespData();

		//2,3道是否走cache
		//boolean isFile = false;
		String fileSha256 = null;
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcPostRawDgrReqVo, srcUrl,
				reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcPostRawDgrReqVo, srcUrl,
				reqMbody);
		if (StringUtils.hasText(autoCacheId)) {// 自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcPostRawDgrReqVo);
			paramVo.setUuid(uuid);
			paramVo.setHttpMethod(httpReq.getMethod());
			paramVo.setHttpReq(httpReq);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				//若為檔案,就回應不能使用cache
				respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				//此行因為httpRes不能放在callback,所以移到外層
			}else {//cache發生未知錯誤,call api
				respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostRawDgrReqVo, reqMbody, uuid,
						false, tryNumWord);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcPostRawDgrReqVo);
				if (isUpdate) {// 更新紀錄
					respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostRawDgrReqVo, reqMbody, uuid,
							true, tryNumWord);
					// statusCode大於等於200 且 小於400才更新紀錄
					if (respObj.statusCode >= 200 && respObj.statusCode < 400) {
						cacheVo.setData(respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespStr(respObj.respStr);
						cacheVo.setFileSha256(fileSha256);
						// cacheVo.setFile(isFile);
						cacheVo.setRespHeader(respObj.respHeader);
						cacheVo.setStatusCode(respObj.statusCode);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					} else {// 否則就取上次紀錄
						respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
								cacheVo.getRespHeader());
					}
				} else {// 取得cache資料
					respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
							cacheVo.getRespHeader());
				}
			} else {// call api
				respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostRawDgrReqVo, reqMbody, uuid,
						true, tryNumWord);
				// statusCode大於等於200 且 小於400才紀錄
				if (respObj.statusCode >= 200 && respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespStr(respObj.respStr);
					cacheVo.setFileSha256(fileSha256);
					// cacheVo.setFile(isFile);
					cacheVo.setRespHeader(respObj.respHeader);
					cacheVo.setStatusCode(respObj.statusCode);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}

		} else {// call api
			respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostRawDgrReqVo, reqMbody, uuid, false,
					tryNumWord);
		}

		httpRes = getCommForwardProcService().getConvertResponse(respObj.respHeader, respObj.statusCode, httpRes, tsmpApiReg);

		//轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq, respObj.httpRespArray, respObj.respStr);

		return convertResponseBodyMap; //Response alread commited HTTP code : 503 --> null
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");

			//第二組ES REQ
			TsmpApiLogReq dgrcPostRawBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody() , vo.getHttpReq(),null);
			boolean isSse = vo.getHeader().keySet().stream()
					.anyMatch(key -> key.equalsIgnoreCase(IS_SSE));
			respObj.fetchByte(maskInfo, isSse); // because Enable inputStream
			// 下載檔案的處理
			if (respObj.respHeader != null && respObj.respHeader.get("Content-Disposition") != null) {
				if (respObj.respHeader.get("Content-Disposition").toString().indexOf("filename") > -1) {
					respObj.respStr = "{\"errMsg\":\"file content can not cache\"}";
				}
			}

			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPostRawBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
										HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String payload, String uuid,
										boolean isFixedCache, String tryNumWord) throws Exception {

		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}

		StringBuffer dgrcPostRaw_sb = new StringBuffer();
		if (isFixedCache) {
			dgrcPostRaw_sb
					.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			dgrcPostRaw_sb
					.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcPostRaw_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			dgrcPostRaw_sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}

		// 第二組ES REQ
		TsmpApiLogReq dgrcPostRawBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header,
				srcUrl, payload);
		if (header.get(IS_SSE) != null) {
			httpRes.setHeader("Content-Type", "text/event-stream");
		}
		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, srcUrl, payload,httpReq,httpRes.getOutputStream());
		boolean isSse = header.keySet().stream()
				.anyMatch(key -> key.equalsIgnoreCase(IS_SSE));
		respObj.fetchByte(maskInfo, isSse); // because Enable inputStream
		// 若respObj.respStr為null帶表有檔案則寫入檔案sha256
		// if (!StringUtils.hasLength(respObj.respStr)){
		// respObj.respStr = getRespFileLog(respObj);
		// }
		dgrcPostRaw_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcPostRaw_sb.toString());
		
		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
		Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);
		
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;

//		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes);
//		byte[] httpArray = respObj.httpRespArray;
		// 4. resp header / body / code
		//for logs
//		String httpRespStr = null;
//        Map<String, Object> rsMap = new HashMap<>();
//		//若有回傳檔案會包含file Sha256字串
//		if ( !respObj.respStr.contains("file Sha256")) {
//			httpArray = respObj.httpRespArray;
////			httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
//			//第二組ES RESP
//			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, bankendReqVo, httpArray.length);
//			rsMap.put("isFile", "0");
//		} else {
//			httpArray = getInputStreamToByteArray(respObj.respInputStreamObj);
////			httpRespStr = getLogHasFile(respObj.respInputStreamObj);
//			byte[] hash = SHA256Util.getSHA256(httpArray);
//
//			String fileSha256 = HexStringUtils.toString(hash);
//			//第二組ES RESP
//			//不自動遮罩所以給0
//			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, bankendReqVo, fileSha256, 0);
//			TPILogger.tl.debug("\n\n##----- file SHA256 -----------\n"+fileSha256+"\n");
//			rsMap.put("isFile", "1");
//			rsMap.put("fileSha256", fileSha256);
//		}

//		rsMap.put("httpArray", httpArray);
//		rsMap.put("httpRespStr", httpRespStr);
//		rsMap.put("statusCode", respObj.statusCode);
//		rsMap.put("respHeader", respObj.respHeader);

		//第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPostRawBackendReqVo, contentLength);

		return respObj;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload, String reqUrl) throws IOException {
		StringBuffer dgrcPostRaw_log = new StringBuffer();

		// print
		writeLogger(dgrcPostRaw_log, "--【URL】--");
		writeLogger(dgrcPostRaw_log, httpReq.getRequestURI());
		writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcPostRaw_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcPostRaw_log, "--【Http Req Header】--");
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
			writeLogger(dgrcPostRaw_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcPostRaw_log, "--【Req payload / Form Data】");
		writeLogger(dgrcPostRaw_log, getCommForwardProcService().maskBody(maskInfo, payload));
		writeLogger(dgrcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcPostRaw_log;
	}

	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	protected HttpRespData getHttpRespData(String httpMethod, Map<String, List<String>> header,
										   String reqUrl, String payload, HttpServletRequest httpReq, ServletOutputStream sseOutput) throws Exception {

		HttpRespData dgrcPostRaw_httpRespData = null;
		Pair<Boolean, CertificateInfo> pair = new Pair<>(false, null);
		DgrProtocol dgrProtocol = DgrProtocol.parse(reqUrl);
		if (!dgrProtocol.mtls()) {
			var targetUrl = dgrProtocol.getParsed(sslEnabled, serverPort);
			dgrcPostRaw_httpRespData = HttpUtil.httpReqByRawDataList(targetUrl, httpMethod, payload, header, true, false, maskInfo ,sseOutput);
		} else {
			reqUrl = String.format("https://%s:%d%s", dgrProtocol.host(), dgrProtocol.port(), dgrProtocol.path());
			pair = getDgrcRoutingHelper().getMtlsInfo(dgrProtocol.host(), dgrProtocol.port());
			dgrcPostRaw_httpRespData =  HttpUtil2.callApiByHttp2(httpReq,reqUrl,  false, pair.getA(), pair.getB(), sseOutput,false);
		}
		//判斷是否有檔案 有檔案回傳Stream 無則回傳String
//		if (!httpRespData.respHeader.containsKey("Content-Disposition")){
//			httpRespData.respStr = HttpUtil.toPrettyJson(HttpUtil.read(httpRespData.respInputStreamObj));
//		}

		return dgrcPostRaw_httpRespData;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	private String getLogHasFile(InputStream inputStream) {
		String dgrcPostRaw_v = "";
		byte[] bytes;
		try {
			bytes = inputStream.readAllBytes();
			if (bytes != null && bytes.length > 0) {
				dgrcPostRaw_v = HexStringUtils.toString(bytes);
				if (dgrcPostRaw_v.length() > 100) {
					dgrcPostRaw_v = dgrcPostRaw_v.substring(0, 100) + "...";
				}
			}
		} catch (Exception e) {
			dgrcPostRaw_v = "[Unreadble content]";
		}
		return dgrcPostRaw_v;
	}

	private byte[] getInputStreamToByteArray(InputStream inputStream) {
		ByteArrayOutputStream dgrcPostRaw_buffer = new ByteArrayOutputStream();
		try {
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				dgrcPostRaw_buffer.write(data, 0, nRead);
			}
		} catch (Exception e) {
			TPILogger.tl.debug("InputStream To ByteArray is error");
		}
		return dgrcPostRaw_buffer.toByteArray();
	}

	public String getRespFileLog(HttpRespData respObj) throws UnsupportedEncodingException {
		byte[] httpArray = getInputStreamToByteArray(respObj.respInputStreamObj);
		byte[] hash = SHA256Util.getSHA256(httpArray);
		String fileSha256Log = "--file Sha256\n";
		String fileSha256 = HexStringUtils.toString(hash);
		fileSha256Log = fileSha256Log + fileSha256;
		respObj.respInputStreamObj = new ByteArrayInputStream(httpArray);
		return fileSha256Log;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload, TsmpApiLogReq dgrReqVo,
			TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, tsmpApiReg);

	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}
}
