package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
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
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Deprecated
@Service
public class DGRCServicePut implements IApiCacheService {

	private CommForwardProcService commForwardProcService;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private DgrcRoutingHelper dgrcRoutingHelper;
	private TsmpSettingService tsmpSettingService;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private MockApiTestService mockApiTestService;
	private ObjectMapper objectMapper;

	private Map<String, String> maskInfo;

	@Autowired
	public DGRCServicePut(CommForwardProcService commForwardProcService,
			ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy, DgrcRoutingHelper dgrcRoutingHelper,
			TsmpSettingService tsmpSettingService, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			MockApiTestService mockApiTestService, ObjectMapper objectMapper) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.mockApiTestService = mockApiTestService;
		this.objectMapper = objectMapper;
	}

	public ResponseEntity<?> forwardToPut(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes,
			String payload) throws Exception {

		try {

			if (payload == null) {
				payload = "";
			}

			String reqUrl = httpReq.getRequestURI();

			TsmpApiReg apiReg = null;
			String dgrcPut_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcPut_moduleName);
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
						"TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcPut_moduleName);
			}

			// 1. req header / body
			// print
			String uuid = UUID.randomUUID().toString();

			// 判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcPut_moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}

			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());

			// 檢查授權
			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders,
					apiReg, payload, false);

			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq dgrcPutDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload,
					"dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcPutDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, payload,
					"dgrc", aType);

			// 授權錯誤,則回覆錯誤訊息
			if (errRespEntity != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcPutDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcPutDgrReqVo_rdb, respMbody);
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
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, dgrcPutDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, dgrcPutDgrReqVo_rdb, respMbody);
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
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcPutDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcPutDgrReqVo_rdb, respMbody);
				return srcUrlListErrResp;
			}

			int tokenPayload = apiReg.getFunFlag();

			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcPutDgrReqVo,
						dgrcPutDgrReqVo_rdb, cApiKeySwitch, apiReg);
			}

			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcPutDgrReqVo, dgrcPutDgrReqVo_rdb, srcUrlList, payload);

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
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo,
					httpReq);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcPutDgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcPutDgrReqVo_rdb, httpRespStr,
					content_Length);

			return null;
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
			TsmpApiLogReq dgrcPutDgrReqVo, TsmpApiLogReq dgrcPutDgrReqVo_rdb, List<String> srcUrlList,
			String payload) throws Exception {
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
						dgrcPutDgrReqVo, dgrcPutDgrReqVo_rdb, cApiKeySwitch, tryNumWord, apiReg);

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
					dgrcPutDgrReqVo, dgrcPutDgrReqVo_rdb, cApiKeySwitch, null, apiReg);
		}

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq dgrcPutDgrReqVo, TsmpApiLogReq dgrcPutDgrReqVo_rdb, Boolean cApiKeySwitch, String tryNumWord, TsmpApiReg tsmpApiReg)
			throws Exception {

		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		HttpRespData dgrcPut_respObj = new HttpRespData();

		// 2,3道是否走cache
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcPutDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcPutDgrReqVo, srcUrl, reqMbody);
		if (StringUtils.hasText(autoCacheId)) {
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcPutDgrReqVo);
			paramVo.setUuid(uuid);
			paramVo.setHttpMethod(httpReq.getMethod());
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this,
					paramVo);
			if (apiCacheRespVo != null) {// 走cache
				dgrcPut_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(),
						apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				// 此行因為httpRes不能放在callback,所以移到外層
			} else {// cache發生未知錯誤,call api
				dgrcPut_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPutDgrReqVo, reqMbody, uuid,
						false, tryNumWord, tsmpApiReg);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcPutDgrReqVo);
				if (isUpdate) {// 更新紀錄
					dgrcPut_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPutDgrReqVo, reqMbody,
							uuid, true, tryNumWord, tsmpApiReg);
					// statusCode大於等於200 且 小於400才更新紀錄
					if (dgrcPut_respObj.statusCode >= 200 && dgrcPut_respObj.statusCode < 400) {
						cacheVo.setData(dgrcPut_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcPut_respObj.respHeader);
						cacheVo.setStatusCode(dgrcPut_respObj.statusCode);
						cacheVo.setRespStr(dgrcPut_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					} else {// 否則就取上次紀錄
						dgrcPut_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
								cacheVo.getRespHeader());
					}
				} else {// 取得cache資料
					dgrcPut_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
							cacheVo.getRespHeader());
				}
			} else {// call api
				dgrcPut_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPutDgrReqVo, reqMbody, uuid,
						true, tryNumWord, tsmpApiReg);
				// statusCode大於等於200 且 小於400才更新紀錄
				if (dgrcPut_respObj.statusCode >= 200 && dgrcPut_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcPut_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcPut_respObj.respHeader);
					cacheVo.setStatusCode(dgrcPut_respObj.statusCode);
					cacheVo.setRespStr(dgrcPut_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}

		} else {// call api
			dgrcPut_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcPutDgrReqVo, reqMbody, uuid,
					false, tryNumWord, tsmpApiReg);
		}
		
		httpRes = getCommForwardProcService().getConvertResponse(dgrcPut_respObj.respHeader, dgrcPut_respObj.statusCode, httpRes, tsmpApiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				dgrcPut_respObj.httpRespArray, dgrcPut_respObj.respStr);

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	@Override
	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");

			// 第二組ES REQ
			TsmpApiLogReq dgrcPutBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(),
					vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getSrcUrl(),
					vo.getReqMbody());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPutBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String reqMbody, String uuid,
			boolean isFixedCache, String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {
		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}

		// 印出第二,三道log
		StringBuffer dgrcPut_sb = new StringBuffer();
		if (isFixedCache) {
			dgrcPut_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			dgrcPut_sb
					.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcPut_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			dgrcPut_sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}

		// 第二組ES REQ
		TsmpApiLogReq dgrcPutBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl,
				reqMbody);
		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, srcUrl, reqMbody);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		dgrcPut_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcPut_sb.toString());

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
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcPutBackendReqVo, contentLength);

		return respObj;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload, String reqUrl)
			throws IOException {
		StringBuffer dgrcPut_log = new StringBuffer();

		// print
		writeLogger(dgrcPut_log, "--【URL】--");
		writeLogger(dgrcPut_log, httpReq.getRequestURI());
		writeLogger(dgrcPut_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcPut_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcPut_log, "--【Http Req Header】--");
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
			writeLogger(dgrcPut_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcPut_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcPut_log, "--【Req payload / Form Data】");
		writeLogger(dgrcPut_log, getCommForwardProcService().maskBody(maskInfo, payload));
		writeLogger(dgrcPut_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcPut_log;
	}

	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	protected HttpRespData getHttpRespData(String httpMethod, Map<String, List<String>> header, String reqUrl,
			String payload) throws Exception {
		HttpRespData dgrcPut_httpRespData = HttpUtil.httpReqByRawDataList(reqUrl, httpMethod, payload, header, true,
				false, maskInfo);
		return dgrcPut_httpRespData;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid,
			int tokenPayload, TsmpApiLogReq dgrReqVo, TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg)
			throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, tsmpApiReg);
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
