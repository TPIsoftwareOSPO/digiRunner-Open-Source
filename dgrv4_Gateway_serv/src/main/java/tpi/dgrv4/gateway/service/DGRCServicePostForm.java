package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import oshi.util.tuples.Pair;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DgrProtocol;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
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
public class DGRCServicePostForm implements IApiCacheService {

	private CommForwardProcService commForwardProcService;
	private MultipartResolver multipartResolver;
	private ObjectMapper objectMapper;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private DgrcRoutingHelper dgrcRoutingHelper;
	private MockApiTestService mockApiTestService;

	private Map<String, String> maskInfo;

	@Autowired
	public DGRCServicePostForm(CommForwardProcService commForwardProcService, MultipartResolver multipartResolver,
			ObjectMapper objectMapper, ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy,
			TsmpApiRegCacheProxy tsmpApiRegCacheProxy, TsmpSettingService tsmpSettingService,
			DgrcRoutingHelper dgrcRoutingHelper, MockApiTestService mockApiTestService) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.multipartResolver = multipartResolver;
		this.objectMapper = objectMapper;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.mockApiTestService = mockApiTestService;
	}

	@Async("async-workers-highway")
	public CompletableFuture<ResponseEntity<?>> forwardToPostFormDataAsyncFast(HttpHeaders httpHeaders,
			HttpServletRequest httpReq,
			HttpServletResponse httpRes) throws Exception {
		var response = forwardToPostFormData(httpHeaders, httpReq, httpRes);
		return CompletableFuture.completedFuture(response);
	}

	@Async("async-workers")
	public CompletableFuture<ResponseEntity<?>> forwardToPostFormDataAsync(HttpHeaders httpHeaders,
			HttpServletRequest httpReq,
			HttpServletResponse httpRes) throws Exception {
		var response = forwardToPostFormData(httpHeaders, httpReq, httpRes);
		return CompletableFuture.completedFuture(response);
	}

	public ResponseEntity<?> forwardToPostFormData(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes) throws Exception {
		MultipartHttpServletRequest multipartRequest = null;
		try {
			multipartRequest = toMultipartRequest(httpReq);
			if ("PATCH".equals(httpReq.getMethod()) && multipartRequest.getFileMap().isEmpty())
				return forwardTo(httpReq, httpRes, httpHeaders);

			// 包含檔案的轉發
			multipartRequest = toMultipartRequest(httpReq);
			return forwardTo(multipartRequest, httpRes, httpHeaders);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}
	/**
	 * 不包含檔案的轉發
	 */
	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {
		try {
			String reqUrl = httpReq.getRequestURI();
			String uuid = UUID.randomUUID().toString();

			TsmpApiReg apiReg = null;
			if (null == httpReq.getAttribute(GatewayFilter.MODULE_NAME)) {
				throw new Exception("TSMP_API_REG module_name is null");
			}
			String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, moduleName);
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
				throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + moduleName);
			}

			// 判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}

			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());

			// 檢查授權
			ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg);

			// 1. req header / body
			String reqMbody = getCommForwardProcService().getReqMbody(httpReq);
			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcPostFormDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, reqMbody,
					"dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcPostFormDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq,
					reqMbody, "dgrc", aType);

			// 授權錯誤,則回覆錯誤訊息
			if (verifyResp != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, dgrcPostFormDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, dgrcPostFormDgrReqVo_rdb, respMbody);
				return verifyResp;
			}

			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);

				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcPostFormDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcPostFormDgrReqVo_rdb, respMbody);
				return srcUrlListErrResp;
			}

			int tokenPayload = apiReg.getFunFlag();

			Map<String, String> partContentTypes = new HashMap<>();
			for (Part part : httpReq.getParts()) {
				partContentTypes.put(part.getName(), part.getContentType());
			}

			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcPostFormDgrReqVo,
						dgrcPostFormDgrReqVo_rdb, cApiKeySwitch, apiReg);
			}

			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToWithoutFileByPolicy(httpHeaders, httpReq, httpRes, apiReg,
					uuid, tokenPayload, cApiKeySwitch, dgrcPostFormDgrReqVo, dgrcPostFormDgrReqVo_rdb, srcUrlList, reqMbody,
					partContentTypes);

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
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcPostFormDgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcPostFormDgrReqVo_rdb, httpRespStr,
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
	private Map<String, Object> forwardToWithoutFileByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrcPostFormDgrReqVo, TsmpApiLogReq dgrcPostFormDgrReqVo_rdb, List<String> srcUrlList,
			String reqMbody, Map<String, String> partContentTypes) throws Exception {
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
				convertResponseBodyMap = forwardToWithoutFile(httpReq, httpRes, httpHeaders, reqMbody, srcUrl, uuid,
						tokenPayload, dgrcPostFormDgrReqVo, dgrcPostFormDgrReqVo_rdb, cApiKeySwitch, partContentTypes,
						tryNumWord, apiReg);

				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardToWithoutFile(httpReq, httpRes, httpHeaders, reqMbody, srcUrl, uuid,
					tokenPayload, dgrcPostFormDgrReqVo, dgrcPostFormDgrReqVo_rdb, cApiKeySwitch, partContentTypes,
					null, apiReg);
		}

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardToWithoutFile(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrcPostFormDgrReqVo, TsmpApiLogReq dgrcPostFormDgrReqVo_rdb, Boolean cApiKeySwitch,
			Map<String, String> partContentTypes, String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {
		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		HttpRespData dgrcPostForm_respObj = new HttpRespData();
		// 2,3道是否走cache
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcPostFormDgrReqVo, srcUrl,
				reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcPostFormDgrReqVo, srcUrl,
				reqMbody);
		if (StringUtils.hasText(autoCacheId)) {// 自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcPostFormDgrReqVo);
			paramVo.setUuid(uuid);
			paramVo.setHttpMethod(httpReq.getMethod());
			paramVo.setParamMap(httpReq.getParameterMap());
			paramVo.setPartContentTypes(partContentTypes);
			paramVo.setHttpReq(httpReq);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this,
					paramVo);
			if (apiCacheRespVo != null) {// 走cache
				dgrcPostForm_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(),
						apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				// 此行因為httpRes不能放在callback,所以移到外層
			} else {// cache發生未知錯誤,call api
				dgrcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostFormDgrReqVo,
						reqMbody, uuid, false, tryNumWord, tsmpApiReg);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcPostFormDgrReqVo);
				if (isUpdate) {// 更新紀錄
					dgrcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostFormDgrReqVo,
							reqMbody, uuid, true, tryNumWord, tsmpApiReg);
					// statusCode大於等於200 且 小於400才更新紀錄
					if (dgrcPostForm_respObj.statusCode >= 200 && dgrcPostForm_respObj.statusCode < 400) {
						cacheVo.setData(dgrcPostForm_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcPostForm_respObj.respHeader);
						cacheVo.setStatusCode(dgrcPostForm_respObj.statusCode);
						cacheVo.setRespStr(dgrcPostForm_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					} else {// 否則就取上次紀錄
						dgrcPostForm_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(),
								cacheVo.getData(), cacheVo.getRespHeader());
					}
				} else {// 取得cache資料
					dgrcPostForm_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
							cacheVo.getRespHeader());
				}
			} else {// call api
				dgrcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostFormDgrReqVo,
						reqMbody, uuid, true, tryNumWord, tsmpApiReg);
				// statusCode大於等於200 且 小於400才更新紀錄
				if (dgrcPostForm_respObj.statusCode >= 200 && dgrcPostForm_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcPostForm_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcPostForm_respObj.respHeader);
					cacheVo.setStatusCode(dgrcPostForm_respObj.statusCode);
					cacheVo.setRespStr(dgrcPostForm_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}

		} else {// call api
			dgrcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPostFormDgrReqVo, reqMbody,
					uuid, false, tryNumWord, tsmpApiReg);
		}

		httpRes = getCommForwardProcService().getConvertResponse(dgrcPostForm_respObj.respHeader,
				dgrcPostForm_respObj.statusCode, httpRes, tsmpApiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				dgrcPostForm_respObj.httpRespArray, dgrcPostForm_respObj.respStr);

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String reqMbody, String uuid,
			boolean isFixedCache, String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {
		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}

		// 印出第二,三道log
		StringBuffer sb = new StringBuffer();
		if (isFixedCache) {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}

		var partsInfo = getCommForwardProcService().fetchFormDataPartsInfo(httpReq);

		// 第二組ES REQ
		TsmpApiLogReq dgrcPostFormBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header,
				srcUrl, reqMbody);

		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, partsInfo.getB(), srcUrl, partsInfo.getA());
		respObj.fetchByte(maskInfo); // because Enable inputStream
		sb.append(respObj.getLogStr());
		TPILogger.tl.debug(sb.toString());

		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
		Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes, tsmpApiReg);

		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;

		// 第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPostFormBackendReqVo, contentLength);

		return respObj;
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			// 印出第二,三道log
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");

			// 第二組ES REQ
			TsmpApiLogReq dgrcPostFormBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(),
					vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getParamMap(), vo.getSrcUrl(),
					vo.getPartContentTypes());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPostFormBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}

	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders,
			String reqUrl) throws IOException, ServletException {
		StringBuffer dgrcPostFormLog_log = new StringBuffer();

		// print
		writeLogger(dgrcPostFormLog_log, "--【URL】--");
		writeLogger(dgrcPostFormLog_log, httpReq.getRequestURI());
		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcPostFormLog_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcPostFormLog_log, "--【Http Req Header】--");
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
			writeLogger(dgrcPostFormLog_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcPostFormLog_log, "--【Req payload / Form Data】");
		Collection<Part> parts;

		parts = httpReq.getParts();
		for (Part part : parts) {
			String name = part.getName();
			String contentType = part.getContentType();
			String value = httpReq.getParameter(name);
			writeLogger(dgrcPostFormLog_log,
					"\tKey: " + name + ", Value: "
							+ getCommForwardProcService().maskBodyFromFormData(maskInfo, name, value)
							+ ", Content-Type: " + contentType);
		}

		// httpReq.getParameterMap().forEach((k, vs) -> {
		// if (vs.length != 0) {
		// for (String v : vs) {
		//
		// writeLogger(dgrcPostFormLog_log, "\tKey: " + k + ", Value: " + v);
		// }
		// }
		// });
		writeLogger(dgrcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcPostFormLog_log;
	}

	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	protected HttpRespData getHttpRespData(String httpMethod, Map<String, List<String>> header,
			Map<String, String[]> paramMap, String reqUrl, Map<String, String> partContentTypes) throws Exception {

		// form data
		Map<String, List<String>> formData = new HashMap<>();
		paramMap.forEach((k, vs) -> {
			if (vs.length != 0) {
				formData.put(k, Arrays.asList(vs));
			}
		});

		HttpRespData respObj = HttpUtil.httpReqByFormDataList(reqUrl, httpMethod, formData, header, true, false,
				partContentTypes, maskInfo);

		return respObj;
	}

	// 若有包含檔案則轉型
	private MultipartHttpServletRequest toMultipartRequest(HttpServletRequest httpServletRequest) {
		MultipartHttpServletRequest multipartHttpServletRequest = this.multipartResolver
				.resolveMultipart(httpServletRequest);
		return multipartHttpServletRequest;
	}

	/*
	 * 包含檔案的轉發
	 */
	protected ResponseEntity<?> forwardTo(MultipartHttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {
		String reqUrl = httpReq.getRequestURI();
		TsmpApiReg apiReg = null;
		String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, moduleName);
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
			throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + moduleName);
		}

		// 0. req header / body
		// writeLogger(log, "--【LOGUUID】【" + uuid + "】【Start DGRC】--");
		String uuid = UUID.randomUUID().toString();

		// 判斷是否需要cApikey
		boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
		String aType = "R";
		if (cApiKeySwitch) {
			aType = "C";
		}

		// 整理要印出的資料
		StringBuffer dgrcPostFormForward_log = new StringBuffer();

		// 1. 【URL】
		writeLogger(dgrcPostFormForward_log, "--【URL】--");
		writeLogger(dgrcPostFormForward_log, httpReq.getRequestURI());
		writeLogger(dgrcPostFormForward_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// 2. 【HTTP METHOD】
		writeLogger(dgrcPostFormForward_log, "【" + httpReq.getMethod() + "】\r\n");

		// 3. 【Http Req Header】
		writeLogger(dgrcPostFormForward_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			String value = null;
			if (!CollectionUtils.isEmpty(valueList)) {
				value = getCommForwardProcService().convertAuth(key, valueList.toString(), maskInfo);
			}
			writeLogger(dgrcPostFormForward_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcPostFormForward_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// 4. 【Req payload / Form Data】
		writeLogger(dgrcPostFormForward_log, "--【Req payload / Form Data】--");
		final String boundary = parseBoundary(httpReq);

		List<byte[]> formBodyParts = new ArrayList<>();
		List<byte[]> formBodyParts_File2Hex = new ArrayList<>();
		Collection<Part> parts = httpReq.getParts();
		Map<String, String> partContentTypes = new HashMap<>();

		// 將每個Part的ContentType保存到Map中
		for (Part part : parts) {
			partContentTypes.put(part.getName(), part.getContentType());
		}
		// 文字
		Map<String, String[]> parameterMap = httpReq.getParameterMap();
		if (!CollectionUtils.isEmpty(parameterMap)) {
			String dgrcPostForm_name;
			String[] vals;
			byte[] data;

			for (Map.Entry<String, String[]> entries : parameterMap.entrySet()) {
				dgrcPostForm_name = entries.getKey();
				vals = entries.getValue();
				for (String val : vals) {
					Map<String, Object> dataMap = HttpUtil.getFormBodyPart(dgrcPostForm_name, null, val.getBytes(),
							boundary, dgrcPostFormForward_log, partContentTypes.get(dgrcPostForm_name), maskInfo);
					data = (byte[]) dataMap.get("data");
					formBodyParts.add(data);
					Map<String, Object> logData = (Map<String, Object>) dataMap.get("logData");
					formBodyParts_File2Hex.add((byte[]) logData.get("contentD"));
					formBodyParts_File2Hex.add((byte[]) logData.get("content"));
				}
			}
		}

		// 檔案
		Map<String, MultipartFile> fileMap = httpReq.getFileMap();
		if (!CollectionUtils.isEmpty(fileMap)) {
			String dgrcPostForm_name;
			MultipartFile mf;
			byte[] data;
			for (Map.Entry<String, MultipartFile> entries : fileMap.entrySet()) {
				dgrcPostForm_name = entries.getKey();
				mf = entries.getValue();
				// String contentType = mf.getContentType(); // Get the content type from the
				// MultipartFile
				Map<String, Object> dataMap = HttpUtil.getFormBodyPart(dgrcPostForm_name, mf.getOriginalFilename(),
						mf.getBytes(), boundary, dgrcPostFormForward_log, partContentTypes.get(dgrcPostForm_name),
						maskInfo);
				data = (byte[]) dataMap.get("data");
				formBodyParts.add(data);

				Map<String, Object> logData = (Map<String, Object>) dataMap.get("logData");
				// byte[] hexData = ("\r\n"+ HttpUtil.PREFIX_Sha256_Hex +
				// HexStringUtils.toString(SHA256Util.getSHA256((byte[])logData.get("content")))).getBytes()
				// ;
				formBodyParts_File2Hex.add((byte[]) logData.get("contentD"));
				formBodyParts_File2Hex.add((byte[]) logData.get("content"));
			}
		}

		byte[] formBody = HttpUtil.getFormBody(formBodyParts, boundary, partContentTypes);
		byte[] formBody2Hex = HttpUtil.getFormBody(formBodyParts_File2Hex, boundary, partContentTypes);
		writeLogger(dgrcPostFormForward_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		String reqMbody = new String(formBody2Hex);

		// 印出第一道log
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + dgrcPostFormForward_log.toString());

		// 檢查授權
		ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg);

		// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
		TsmpApiLogReq dgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, reqMbody, "dgrc",
				aType);
		// 第一組 RDB Req
		TsmpApiLogReq dgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, reqMbody, "dgrc",
				aType);

		// 授權錯誤,則回覆錯誤訊息
		if (verifyResp != null) {
			// 第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, dgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, dgrReqVo_rdb, respMbody);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
					+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
			return verifyResp;
		}

		List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
		// 沒有目標URL,則回覆錯誤訊息
		if (CollectionUtils.isEmpty(srcUrlList)) {
			ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);

			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
					+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
			// 第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrReqVo_rdb, respMbody);
			return srcUrlListErrResp;
		}

		int tokenPayload = apiReg.getFunFlag();

		// 判斷是否為 API mock test
		boolean isMockTest = checkIfMockTest(httpHeaders);
		if (isMockTest) {
			// Mock test 不做重試,只取第一個URL執行
			String srcUrl = srcUrlList.get(0);
			TPILogger.tl.debug("Src Url:" + srcUrl);
			return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb,
					cApiKeySwitch, apiReg);
		}

		// 調用目標URL
		Map<String, Object> convertResponseBodyMap = forwardWithFileByPolicy(httpHeaders, httpReq, httpRes, apiReg,
				uuid, tokenPayload, cApiKeySwitch, dgrReqVo, dgrReqVo_rdb, srcUrlList, reqMbody, boundary, formBody,
				formBody2Hex);

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
			try {
				IOUtils.copy(bi, httpRes.getOutputStream()); // http InputStream copy into Array
			} catch (AsyncRequestNotUsableException e) {
				TPILogger.tl.warn(httpRes.getStatus() + ", " + e.getLocalizedMessage());
			}
		}

		// 印出第四道log
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo,
				httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

		// 第一組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrReqVo, httpRespStr, content_Length);
		// 第一組RDB RESP
		getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrReqVo_rdb, httpRespStr, content_Length);

		return null;
	}

	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardWithFileByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
			TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, List<String> srcUrlList, String reqMbody,
			String boundary, byte[] formBody, byte[] formBody2Hex) throws Exception {
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
				convertResponseBodyMap = forwardWithFile(httpReq, httpRes, httpHeaders, reqMbody, srcUrl, uuid,
						tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, boundary, formBody, formBody2Hex,
						tryNumWord, apiReg);

				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardWithFile(httpReq, httpRes, httpHeaders, reqMbody, srcUrl, uuid,
					tokenPayload, dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, boundary, formBody, formBody2Hex, null, apiReg);
		}

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardWithFile(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, String boundary, byte[] formBody,
			byte[] formBody2Hex, String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {

		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}

		// 5. 【Start DGRC-to-Backend】
		// 6. 【End DGRC-from-Backend】
		// 印出第二,三道log
		StringBuffer dgrcPostFormForward_log = new StringBuffer();

		dgrcPostFormForward_log.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
		dgrcPostFormForward_log.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");

		// ===== ↓ NOTHING TO DO WITH LOGGER ↓ =====

		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);

		// 第二組ES REQ
		TsmpApiLogReq backendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl,
				reqMbody);
		// 7. 呼叫 backend API 的過程
		HttpRespData dgrcPostForm_respObj = forwardWithFile(srcUrl, httpReq.getMethod(), header, boundary, formBody, formBody2Hex, httpReq);



		dgrcPostForm_respObj.fetchByte(maskInfo); // because Enable inputStream
		// ===== ↑ NOTHING TO DO WITH LOGGER ↑ =====

		dgrcPostFormForward_log.append(dgrcPostForm_respObj.getLogStr());
		TPILogger.tl.debug(dgrcPostFormForward_log.toString());
		
		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
		Optional.ofNullable(dgrcPostForm_respObj.loggerElapsedTimeMsg(uuid)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, dgrcPostForm_respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, dgrcPostForm_respObj.elapsedTime);

		// ===== ↓ NOTHING TO DO WITH LOGGER ↓ =====

		httpRes = getCommForwardProcService().getConvertResponse(dgrcPostForm_respObj, httpRes, tsmpApiReg);

		httpRes = getCommForwardProcService().getConvertResponse(dgrcPostForm_respObj.respHeader,
				dgrcPostForm_respObj.statusCode, httpRes, tsmpApiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				dgrcPostForm_respObj.httpRespArray, dgrcPostForm_respObj.respStr);

		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		int contentLength = (httpArray == null) ? 0 : httpArray.length;

		// 第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(dgrcPostForm_respObj, backendReqVo, contentLength);

		// ===== ↑ NOTHING TO DO WITH LOGGER ↑ =====

		return convertResponseBodyMap;
	}

	private HttpRespData forwardWithFile(String reqUrl, String method, Map<String, List<String>> httpHeader, //
										 String boundary, byte[] formBody, byte[] formBody2Hex ,HttpServletRequest httpReq) throws Exception {
		boolean isEnableInputStream = true;
		boolean dgrcPostForm_isRedirect = false;

		HttpRespData respObj = null;
		Pair<Boolean, CertificateInfo> pair = new Pair<>(false, null);
		DgrProtocol dgrProtocol = DgrProtocol.parse(reqUrl);
		if (!dgrProtocol.mtls()) {
			respObj = HttpUtil.httpReqByFormDataList( //
					reqUrl, method, boundary, formBody, formBody2Hex, httpHeader, isEnableInputStream, dgrcPostForm_isRedirect, maskInfo);
		} else {
			reqUrl = String.format("https://%s:%d%s", dgrProtocol.host(), dgrProtocol.port(), dgrProtocol.path());
			pair = getDgrcRoutingHelper().getMtlsInfo(dgrProtocol.host(), dgrProtocol.port());

			respObj = HttpUtil2.callApiByHttp2(httpReq,reqUrl,  false, pair.getA(), pair.getB(), null ,false);

		}
		return respObj;
	}

	private String parseBoundary(MultipartHttpServletRequest multipartReq) {
		HttpHeaders httpHeaders = multipartReq.getRequestHeaders();
		MediaType contentType = httpHeaders.getContentType();
		String dgrcPostForm_boundary;
		if (contentType == null) {
			// not exceed 70 bytes in length and consists only of 7-bit US-ASCII (printable)
			// characters
			dgrcPostForm_boundary = "DgrcBoundary" + UUID.randomUUID().toString();
		} else {
			dgrcPostForm_boundary = contentType.getParameter("boundary");
		}
		return dgrcPostForm_boundary;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid,
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg)
			throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, tsmpApiReg);
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected DgrcRoutingHelper getDgrcRoutingHelper() {
		return dgrcRoutingHelper;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}