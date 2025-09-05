package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.component.cache.proxy.ProxyMethodServiceCacheProxy;
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
public class TSMPCServiceDelete implements IApiCacheService {

	private ObjectMapper objectMapper;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private TsmpSettingService tsmpSettingService;
	private MockApiTestService mockApiTestService;
	private CommForwardProcService commForwardProcService;

	private HashMap<String, String> maskInfo;

	@Autowired
	public TSMPCServiceDelete(ObjectMapper objectMapper, ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy,
			TsmpSettingService tsmpSettingService, MockApiTestService mockApiTestService,
			CommForwardProcService commForwardProcService) {
		super();
		this.objectMapper = objectMapper;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
		this.mockApiTestService = mockApiTestService;
		this.commForwardProcService = commForwardProcService;
	}

	public ResponseEntity<?> forwardToDelete(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String payload) throws Exception {
		try {

			if (payload == null) {
				payload = "";
			}

			String reqUrl = httpReq.getRequestURI();

			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
			String tsmpcDel_moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();

			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			// 判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(tsmpcDel_moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}
			// 檢查資料
			TsmpApiReg apiReg = getCommForwardProcService().getTsmpApiReg(httpReq);
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
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());

			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders,
					apiReg, payload, false);

			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq tsmpcDelDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload,
					"tsmpc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq tsmpcDelDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq,
					payload, "tsmpc", aType);

			// JWT 資料驗證有錯誤
			if (errRespEntity != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcDelDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}

			// 轉換 Request Body 格式
			JwtPayloadData jwtPayloadData = getCommForwardProcService().convertRequestBody(httpRes, httpReq, payload,
					false);
			errRespEntity = jwtPayloadData.errRespEntity;
			if (errRespEntity != null) {// 資料有錯誤
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcDelDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcDelDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}
			payload = jwtPayloadData.payloadStr;

			String tsmpcDel_srcUrl = getCommForwardProcService().getSrcUrl(httpReq);
			if (!StringUtils.hasText(tsmpcDel_srcUrl)) {
				return null;
			}
			// 判斷是否啟用Path Parameter選項
			boolean isURLRID = getCommForwardProcService().isURLRID(tsmpcDel_moduleName, apiId);
			// 進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
			// 取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
			// 完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
			if (isURLRID)
				tsmpcDel_srcUrl = tsmpcDel_srcUrl + commForwardProcService.getTsmpcPathParameter(reqUrl);

			int tokenPayload = apiReg.getFunFlag();

			// For API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				return mockForwardTo(httpReq, httpRes, httpHeaders, tsmpcDel_srcUrl, uuid, tokenPayload,
						tsmpcDelDgrReqVo, tsmpcDelDgrReqVo_rdb, cApiKeySwitch, apiReg);
			} else {
				return forwardTo(httpReq, httpRes, httpHeaders, tsmpcDel_srcUrl, payload, uuid, tokenPayload,
						tsmpcDelDgrReqVo, tsmpcDelDgrReqVo_rdb, cApiKeySwitch, apiReg);
			}

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String srcUrl, String reqMbody, String uuid, int tokenPayload,
			TsmpApiLogReq tsmpcDelDgrReqVo, TsmpApiLogReq tsmpcDelDgrReqVo_rdb //
			, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg) throws Exception {

		// 2. tsmpc req header / body
		// 3. tsmpc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, false);
		// 2,3道是否走cache
		HttpRespData tsmpcDel_respObj = new HttpRespData();
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(tsmpcDelDgrReqVo, srcUrl, reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(tsmpcDelDgrReqVo, srcUrl,
				reqMbody);
		if (StringUtils.hasText(autoCacheId)) {
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(tsmpcDelDgrReqVo);
			paramVo.setUuid(uuid);
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this,
					paramVo);
			if (apiCacheRespVo != null) {// 走cache
				tsmpcDel_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(),
						apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
				// 此行因為httpRes不能放在callback,所以移到外層
			} else {// cache發生未知錯誤,call api
				tsmpcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcDelDgrReqVo, reqMbody, uuid,
						false, httpReq, tsmpApiReg);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, tsmpcDelDgrReqVo);
				if (isUpdate) {// 更新紀錄
					tsmpcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcDelDgrReqVo, reqMbody, uuid,
							true, httpReq, tsmpApiReg);
					
					// statusCode小於400才更新紀錄
					if (tsmpcDel_respObj.statusCode < 400) {
						cacheVo.setData(tsmpcDel_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(tsmpcDel_respObj.respHeader);
						cacheVo.setStatusCode(tsmpcDel_respObj.statusCode);
						cacheVo.setRespStr(tsmpcDel_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					} else {// 否則就取上次紀錄
						tsmpcDel_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
								cacheVo.getRespHeader());
					}
				} else {// 取得cache資料
					tsmpcDel_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(), cacheVo.getData(),
							cacheVo.getRespHeader());
				}
			} else {// call api
				tsmpcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcDelDgrReqVo, reqMbody, uuid, true, httpReq, tsmpApiReg);
				
				// statusCode小於400才紀錄
				if (tsmpcDel_respObj.statusCode < 400) {
					cacheVo = new FixedCacheVo();
					cacheVo.setData(tsmpcDel_respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespHeader(tsmpcDel_respObj.respHeader);
					cacheVo.setStatusCode(tsmpcDel_respObj.statusCode);
					cacheVo.setRespStr(tsmpcDel_respObj.respStr);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}

		} else {// call api
			tsmpcDel_respObj = this.callForwardApi(header, httpRes, srcUrl, tsmpcDelDgrReqVo, reqMbody, uuid, false, httpReq, tsmpApiReg);
		}
		// 不論是 cache , 直接call, 統一在這裡 set RESPONSE HEADER
		httpRes = getCommForwardProcService().getConvertResponse(tsmpcDel_respObj.respHeader,
				tsmpcDel_respObj.statusCode, httpRes, tsmpApiReg);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				tsmpcDel_respObj.httpRespArray, tsmpcDel_respObj.respStr);
		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");

		int content_Length = 0;
		if (httpArray != null) {
			content_Length = httpArray.length;
			ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
			// http InputStream copy into Array
			IOUtils.copy(bi, httpRes.getOutputStream());
		}

		// print
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo,
				httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + resLog.toString());

		// 第一組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, tsmpcDelDgrReqVo, httpRespStr, content_Length);
		// 第一組RDB RESP
		getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, tsmpcDelDgrReqVo_rdb, httpRespStr, content_Length);

		return null;
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start TSMPC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End TSMPC-from-Backend For Cache】--\n");

			// 第二組ES REQ
			TsmpApiLogReq tsmpcDelBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(),
					vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcDelBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}

	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletResponse httpRes, String srcUrl,
			TsmpApiLogReq dgrReqVo, String payload, String uuid, boolean isFixedCache, HttpServletRequest httpReq, TsmpApiReg tsmpApiReg) throws Exception {

		StringBuffer sb = new StringBuffer();
		if (isFixedCache) {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend For Fixed Cache】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend For Fixed Cache】--\n");
		} else {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend】--\n");
		}

		// 第二組ES REQ
		TsmpApiLogReq backendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl, payload);
		HttpRespData respObj = getHttpRespData(header, srcUrl, payload);
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
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, backendReqVo, contentLength);

		return respObj;

	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload, String reqUrl)
			throws IOException {
		StringBuffer tsmpcDel_log = new StringBuffer();

		// print
		writeLogger(tsmpcDel_log, "--【URL】--");
		writeLogger(tsmpcDel_log, httpReq.getRequestURI());
		writeLogger(tsmpcDel_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(tsmpcDel_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(tsmpcDel_log, "--【Http Req Header】--");
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
			writeLogger(tsmpcDel_log, "\tKey: " + key + ", Value: " + value);

		}
		writeLogger(tsmpcDel_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(tsmpcDel_log, "--【Req payload / Form Data】");
		writeLogger(tsmpcDel_log, getCommForwardProcService().maskBody(maskInfo, payload));
		writeLogger(tsmpcDel_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return tsmpcDel_log;
	}

	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	private HttpRespData getHttpRespData(Map<String, List<String>> header, String reqUrl, String payload)
			throws Exception {
		HttpRespData httpRespData = HttpUtil.httpReqByRawDataList(reqUrl, "DELETE", payload, header, true, false,
				maskInfo);

		return httpRespData;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
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

}
