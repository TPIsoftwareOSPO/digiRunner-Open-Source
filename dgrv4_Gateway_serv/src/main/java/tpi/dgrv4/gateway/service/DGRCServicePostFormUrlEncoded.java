package tpi.dgrv4.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import oshi.util.tuples.Pair;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DgrProtocol;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.ResponseHandler;
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

import java.io.IOException;
import java.util.*;

@Service
public class DGRCServicePostFormUrlEncoded implements IApiCacheService {

	@Setter(onMethod_ = @Autowired)
	private CommForwardProcService commForwardProcService;
	@Setter(onMethod_ = @Autowired)
	private ObjectMapper objectMapper;
	@Setter(onMethod_ = @Autowired)
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	@Setter(onMethod_ = @Autowired)
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	@Setter(onMethod_ = @Autowired)
	private MockApiTestService mockApiTestService;
	@Setter(onMethod_ = @Autowired)
	private DgrcRoutingHelper dgrcRoutingHelper;
	@Setter(onMethod_ = @Autowired)
	private TsmpSettingService tsmpSettingService;
	@Setter(onMethod_ = @Autowired)
	ResponseHandler responseHandler;

	private Map<String, String> maskInfo;


	public void forwardToPostFormUrlEncoded(HttpHeaders httpHeaders, HttpServletRequest httpReq,
														 HttpServletResponse httpRes, MultiValueMap<String, String> values) throws Exception {
		try {
			String reqUrl = httpReq.getRequestURI();

			TsmpApiReg apiReg = null;
			if (null == httpReq.getAttribute(GatewayFilter.MODULE_NAME)) {
				throw new Exception("TSMP_API_REG module_name is null");
			}
			String dgrcUrlEncoded_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiId, dgrcUrlEncoded_moduleName);
			Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
			if (opt_tsmpApiReg.isPresent()) {
				apiReg = opt_tsmpApiReg.get();
				ResponseEntity<?> allowMethodErrRespEntity = getCommForwardProcService().checkMethod(apiReg, httpReq);
				if (allowMethodErrRespEntity != null) {
					responseHandler
							.response(httpRes)
							.handle(allowMethodErrRespEntity)
							.write();
					return;
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
						"TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + dgrcUrlEncoded_moduleName);
			}

			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();

			//判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(dgrcUrlEncoded_moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}

			// 印出第一道log
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start DGRC】--\n" + reqLog.toString());

			// 檢查授權
			ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg);

			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			String reqMbody = getCommForwardProcService().getReqMbody(httpReq);
			TsmpApiLogReq dgrcUrlEncodedDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq,
					reqMbody, "dgrc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq dgrcUrlEncodedDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, reqMbody, "dgrc", aType);

			// 授權錯誤,則回覆錯誤訊息
			if(verifyResp != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, dgrcUrlEncodedDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, dgrcUrlEncodedDgrReqVo_rdb, respMbody);
				responseHandler
						.response(httpRes)
						.handle(verifyResp)
						.write();
				return;
			}

			List<String> srcUrlList = getDgrcRoutingHelper().getRouteSrcUrl(apiReg, reqUrl, httpReq);
			// 沒有目標URL,則回覆錯誤訊息
			if (CollectionUtils.isEmpty(srcUrlList)) {
				ResponseEntity<?> srcUrlListErrResp = getDgrcRoutingHelper().getSrcUrlListErrResp(httpReq, apiId);

				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n"
						+ getCommForwardProcService().getLogResp(srcUrlListErrResp, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(srcUrlListErrResp.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(srcUrlListErrResp, dgrcUrlEncodedDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(srcUrlListErrResp, dgrcUrlEncodedDgrReqVo_rdb,
						respMbody);
				responseHandler
						.response(httpRes)
						.handle(srcUrlListErrResp)
						.write();
				return;
			}

			int tokenPayload = apiReg.getFunFlag();

			// 判斷是否為 API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				// Mock test 不做重試,只取第一個URL執行
				String srcUrl = srcUrlList.get(0);
				TPILogger.tl.debug("Src Url:" + srcUrl);
				var mockResp = mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrcUrlEncodedDgrReqVo,
						dgrcUrlEncodedDgrReqVo_rdb, cApiKeySwitch, apiReg);
				responseHandler
						.response(httpRes)
						.handle(mockResp)
						.write();
				return;
			}

			// 調用目標URL
			Map<String, Object> convertResponseBodyMap = forwardToByPolicy(httpHeaders, httpReq, httpRes, apiReg, uuid,
					tokenPayload, cApiKeySwitch, dgrcUrlEncodedDgrReqVo, dgrcUrlEncodedDgrReqVo_rdb, srcUrlList,
					values);

			if (convertResponseBodyMap == null) {
				throw new RuntimeException("forwardToByPolicy return null");
			}

			byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
			String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");


			int content_Length = 0;
			if (httpArray != null) {
				content_Length = httpArray.length;
				responseHandler
						.response(httpRes)
						.handle(httpArray)
						.write();
			}

			// 印出第四道log
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo,
					httpReq);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End DGRC】--\n" + resLog.toString());

			//第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrcUrlEncodedDgrReqVo, httpRespStr, content_Length);
			//第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrcUrlEncodedDgrReqVo_rdb, httpRespStr, content_Length);

		}catch (Throwable e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	/**
	 * 依失敗處置策略,決定只調用API一次或API失敗時重試
	 */
	private Map<String, Object> forwardToByPolicy(HttpHeaders httpHeaders, HttpServletRequest httpReq,
												  HttpServletResponse httpRes, TsmpApiReg apiReg, String uuid, int tokenPayload, boolean cApiKeySwitch,
												  TsmpApiLogReq dgrcUrlEncodedDgrReqVo, TsmpApiLogReq dgrcUrlEncodedDgrReqVo_rdb, List<String> srcUrlList,
												  MultiValueMap<String, String> values) throws Exception {
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
				convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, srcUrl, values, uuid, tokenPayload,
						dgrcUrlEncodedDgrReqVo, dgrcUrlEncodedDgrReqVo_rdb, cApiKeySwitch, tryNumWord, apiReg);

				int httpStatus = httpRes.getStatus();
				boolean isStopReTry = getCommForwardProcService().isStopReTry(failDiscoveryPolicy, httpStatus);// 是否停止重試
				if (isStopReTry) {// 停止重試
					break;
				}
			}

		} else {// 0: 無重試
			String srcUrl = srcUrlList.get(0);// 只取第一個URL執行
			TPILogger.tl.debug("Src Url:" + srcUrl);
			convertResponseBodyMap = forwardTo(httpReq, httpRes, httpHeaders, srcUrl, values, uuid, tokenPayload,
					dgrcUrlEncodedDgrReqVo, dgrcUrlEncodedDgrReqVo_rdb, cApiKeySwitch, null, apiReg);
		}

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	protected Map<String, Object> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
											@RequestHeader HttpHeaders httpHeaders, String srcUrl, MultiValueMap<String, String> values, String uuid,
											int tokenPayload, TsmpApiLogReq dgrcUrlEncodedDgrReqVo, TsmpApiLogReq dgrcUrlEncodedDgrReqVo_rdb,
											Boolean cApiKeySwitch, String tryNumWord, TsmpApiReg apiReg) throws Exception {

		// 2. dgrc req header / body
		// 3. dgrc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, srcUrl);
		String reqMbody = HttpUtil.getDataString(values);
		HttpRespData dgrcUrlEncoded_respObj = new HttpRespData();
		// 2,3道是否走cache
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(dgrcUrlEncodedDgrReqVo, srcUrl,
				reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(dgrcUrlEncodedDgrReqVo, srcUrl,
				reqMbody);
		if (StringUtils.hasText(autoCacheId)) {// 自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(dgrcUrlEncodedDgrReqVo);
			paramVo.setUuid(uuid);
			paramVo.setHttpMethod(httpReq.getMethod());
			paramVo.setValues(values);
			paramVo.setHttpReq(httpReq);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this, paramVo);
			if(apiCacheRespVo != null) {//走cache
				dgrcUrlEncoded_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(), apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				//此行因為httpRes不能放在callback,所以移到外層
			}else {//cache發生未知錯誤,call api
				dgrcUrlEncoded_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcUrlEncodedDgrReqVo,
						reqMbody, uuid, values, false, tryNumWord, apiReg);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, dgrcUrlEncodedDgrReqVo);
				if (isUpdate) {// 更新紀錄
					dgrcUrlEncoded_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl,
							dgrcUrlEncodedDgrReqVo, reqMbody, uuid, values, true, tryNumWord, apiReg);
					// statusCode大於等於200 且 小於400才更新紀錄
					if (dgrcUrlEncoded_respObj.statusCode >= 200 && dgrcUrlEncoded_respObj.statusCode < 400) {
						cacheVo.setData(dgrcUrlEncoded_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(dgrcUrlEncoded_respObj.respHeader);
						cacheVo.setStatusCode(dgrcUrlEncoded_respObj.statusCode);
						cacheVo.setRespStr(dgrcUrlEncoded_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					} else {// 否則就取上次紀錄
						dgrcUrlEncoded_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(),
								cacheVo.getData(), cacheVo.getRespHeader());
					}
				} else {// 取得cache資料
					dgrcUrlEncoded_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
							cacheVo.getRespHeader());
				}
			} else {// call api
				dgrcUrlEncoded_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcUrlEncodedDgrReqVo,
						reqMbody, uuid, values, true, tryNumWord, apiReg);
				// statusCode大於等於200 且 小於400才更新紀錄
				if (dgrcUrlEncoded_respObj.statusCode >= 200 && dgrcUrlEncoded_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(dgrcUrlEncoded_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(dgrcUrlEncoded_respObj.respHeader);
					cacheVo.setStatusCode(dgrcUrlEncoded_respObj.statusCode);
					cacheVo.setRespStr(dgrcUrlEncoded_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}

		}else {//call api
			dgrcUrlEncoded_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, dgrcUrlEncodedDgrReqVo,
					reqMbody, uuid, values, false, tryNumWord, apiReg);
		}

		httpRes = getCommForwardProcService().getConvertResponse(dgrcUrlEncoded_respObj.respHeader,
				dgrcUrlEncoded_respObj.statusCode, httpRes, apiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				dgrcUrlEncoded_respObj.httpRespArray, dgrcUrlEncoded_respObj.respStr);

		return convertResponseBodyMap; // Response alread commited HTTP code : 503 --> null
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start DGRC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End DGRC-from-Backend For Cache】--\n");

			//第二組ES REQ
			TsmpApiLogReq dgrcUrlEncodedBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(), vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getSrcUrl(), vo.getValues() , vo.getHttpReq());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, dgrcUrlEncodedBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
										HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String reqMbody, String uuid,
										MultiValueMap<String, String> values, boolean isFixedCache, String tryNumWord, TsmpApiReg tsmpApiReg) throws Exception {

		String tryNumLog = "";// 當失敗處置策略有設定失敗時重試API時,印出這是第幾次嘗試打API;否則,空白
		if (StringUtils.hasLength(tryNumWord)) {
			tryNumLog = "【" + tryNumWord + "】";
		}

		// 印出第二,三道log
		StringBuffer dgrcUrlEncoded_sb = new StringBuffer();
		if (isFixedCache) {
			dgrcUrlEncoded_sb
					.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend For Fixed Cache】" + tryNumLog + "--");
			dgrcUrlEncoded_sb
					.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend For Fixed Cache】" + tryNumLog + "--\n");
		} else {
			dgrcUrlEncoded_sb.append("\n--【LOGUUID】【" + uuid + "】【Start DGRC-to-Backend】" + tryNumLog + "--");
			dgrcUrlEncoded_sb.append("\n--【LOGUUID】【" + uuid + "】【End DGRC-from-Backend】" + tryNumLog + "--\n");
		}

		//第二組ES REQ
		TsmpApiLogReq backendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl, reqMbody);
		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, srcUrl, values , httpReq);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		dgrcUrlEncoded_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(dgrcUrlEncoded_sb.toString());
		
		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
        Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid, srcUrl, respObj.respStr)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes, tsmpApiReg);

		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;

		// 第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, backendReqVo, contentLength);

		return respObj;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders,
								   String reqUrl) throws IOException {
		StringBuffer dgrcUrlEncoded_log = new StringBuffer();

		// print
		writeLogger(dgrcUrlEncoded_log, "--【URL】--");
		writeLogger(dgrcUrlEncoded_log, httpReq.getRequestURI());
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(dgrcUrlEncoded_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(dgrcUrlEncoded_log, "--【Http Req Header】--");
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
			writeLogger(dgrcUrlEncoded_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(dgrcUrlEncoded_log, "--【Req payload / Form Data】");
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				for (String v : vs) {
					writeLogger(dgrcUrlEncoded_log, "\tKey: " + k + ", Value: "
							+ getCommForwardProcService().maskBodyFromFormData(maskInfo, k, v));
				}
			}
		});
		writeLogger(dgrcUrlEncoded_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return dgrcUrlEncoded_log;
	}

	protected HttpRespData getHttpRespData(String httpMethod, Map<String, List<String>> header,
										   String srcUrl, MultiValueMap<String, String> values , HttpServletRequest httpReq) throws Exception {

		Map<String, List<String>> dgrcUrlEncoded_formData = values;

		HttpRespData respObj;
		Pair<Boolean, CertificateInfo> pair = new Pair<>(false, null);
		DgrProtocol dgrProtocol = DgrProtocol.parse(srcUrl);
		if (!dgrProtocol.mtls()) {
			respObj = HttpUtil.httpReqByX_www_form_urlencoded_UTF8List(srcUrl, httpMethod,
					dgrcUrlEncoded_formData, header, true, false, maskInfo);
		} else {
			srcUrl = String.format("https://%s:%d%s", dgrProtocol.host(), dgrProtocol.port(), dgrProtocol.path());
			pair = getDgrcRoutingHelper().getMtlsInfo(dgrProtocol.host(), dgrProtocol.port());

			respObj =  HttpUtil2.callApiByHttp2(httpReq,srcUrl,  false, pair.getA(), pair.getB(), null,false);

		}


		return respObj;
	}

	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload, TsmpApiLogReq dgrReqVo,
			TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, tsmpApiReg);
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
}