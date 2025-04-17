package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

import tpi.dgrv4.codec.utils.HexStringUtils;
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

@Service
public class TSMPCServicePostRaw implements IApiCacheService {

	private MockApiTestService mockApiTestService;
	private CommForwardProcService commForwardProcService;
	private ObjectMapper objectMapper;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private TsmpSettingService tsmpSettingService;

	private Map<String, String> maskInfo;

	@Autowired
	public TSMPCServicePostRaw(MockApiTestService mockApiTestService, CommForwardProcService commForwardProcService,
			ObjectMapper objectMapper, ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy,
			TsmpSettingService tsmpSettingService) {
		super();
		this.mockApiTestService = mockApiTestService;
		this.commForwardProcService = commForwardProcService;
		this.objectMapper = objectMapper;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
	}

	public ResponseEntity<?> forwardToPostRawData(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String payload) throws Exception {
		try {

			if (payload == null) {
				payload = "";
			}

			String reqUrl = httpReq.getRequestURI();
			String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();

			// 1. req header / body
			// print log
			String uuid = UUID.randomUUID().toString();
			String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
			// 判斷是否需要cApikey
			boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
			String aType = "R";
			if (cApiKeySwitch) {
				aType = "C";
			}
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
			StringBuffer reqLog = getLogReq(httpReq, httpHeaders, payload, reqUrl);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());

			ResponseEntity<?> errRespEntity = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders,
					apiReg, payload, false);

			// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
			TsmpApiLogReq tsmpcPostRawDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, payload,
					"tsmpc", aType);
			// 第一組 RDB Req
			TsmpApiLogReq tsmpcPostRawDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq,
					payload, "tsmpc", aType);

			// JWT 資料驗證有錯誤
			if (errRespEntity != null) {
				TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n"
						+ getCommForwardProcService().getLogResp(errRespEntity, maskInfo, httpReq).toString());
				// 第一組ES RESP
				String respMbody = getObjectMapper().writeValueAsString(errRespEntity.getBody());
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcPostRawDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcPostRawDgrReqVo_rdb, respMbody);
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
				getCommForwardProcService().addEsTsmpApiLogResp1(errRespEntity, tsmpcPostRawDgrReqVo, respMbody);
				getCommForwardProcService().addRdbTsmpApiLogResp1(errRespEntity, tsmpcPostRawDgrReqVo_rdb, respMbody);
				return errRespEntity;
			}
			payload = jwtPayloadData.payloadStr;

			String tsmpcPostRaw_srcUrl = getCommForwardProcService().getSrcUrl(httpReq);
			if (!StringUtils.hasText(tsmpcPostRaw_srcUrl)) {
				return null;
			}
			// 判斷是否啟用Path Parameter選項
			boolean isURLRID = getCommForwardProcService().isURLRID(moduleName, apiId);
			// 進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
			// 取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
			// 完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
			if (isURLRID) {

				tsmpcPostRaw_srcUrl = tsmpcPostRaw_srcUrl + commForwardProcService.getTsmpcPathParameter(reqUrl);
			}
			int tokenPayload = apiReg.getFunFlag();

			// For API mock test
			boolean isMockTest = checkIfMockTest(httpHeaders);
			if (isMockTest) {
				return mockForwardTo(httpReq, httpRes, httpHeaders, tsmpcPostRaw_srcUrl, uuid, tokenPayload,
						tsmpcPostRawDgrReqVo, tsmpcPostRawDgrReqVo_rdb, cApiKeySwitch);
			} else {
				return forwardTo(httpReq, httpRes, httpHeaders, payload, tsmpcPostRaw_srcUrl, uuid, tokenPayload,
						tsmpcPostRawDgrReqVo, tsmpcPostRawDgrReqVo_rdb, cApiKeySwitch);
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders, String reqMbody, String srcUrl, String uuid, int tokenPayload,
			TsmpApiLogReq tsmpcPostRawDgrReqVo, TsmpApiLogReq tsmpcPostRawDgrReqVo_rdb, Boolean cApiKeySwitch)
			throws Exception {

		// 2. tsmpc req header / body
		// 3. tsmpc resp header / body / code

		// http header
		Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
				tokenPayload, cApiKeySwitch, uuid, false);

		// 2,3道是否走cache
		HttpRespData respObj = new HttpRespData();
		String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(tsmpcPostRawDgrReqVo, srcUrl,
				reqMbody);
		String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(tsmpcPostRawDgrReqVo, srcUrl,
				reqMbody);
		if (StringUtils.hasText(autoCacheId)) {// 自適應cache
			AutoCacheParamVo paramVo = new AutoCacheParamVo();
			paramVo.setHeader(header);
			paramVo.setReqMbody(reqMbody);
			paramVo.setSrcUrl(srcUrl);
			paramVo.setDgrReqVo(tsmpcPostRawDgrReqVo);
			paramVo.setUuid(uuid);
			paramVo.setHttpMethod(httpReq.getMethod());
			AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this,
					paramVo);
			if (apiCacheRespVo != null) {// 走cache
				// 若為檔案,就回應不能使用cache
				respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(),
						apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
			} else {// cache發生未知錯誤,call api
				respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostRawDgrReqVo, reqMbody, uuid,
						false);
			}
		} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
			FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
			if (cacheVo != null) {// 走cache
				boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, tsmpcPostRawDgrReqVo);
				if (isUpdate) {// 更新紀錄
					respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostRawDgrReqVo, reqMbody,
							uuid, true);
					// statusCode小於400才更新紀錄
					if (respObj.statusCode < 400) {
						cacheVo.setData(respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespStr(respObj.respStr);
						cacheVo.setFile(respObj.isFile());
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
				respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostRawDgrReqVo, reqMbody, uuid,
						true);
				// statusCode小於400才紀錄
				if (respObj.statusCode < 400) {
					Map<String, List<String>> respHeader = respObj.respHeader;
					cacheVo = new FixedCacheVo();
					cacheVo.setData(respObj.httpRespArray);
					cacheVo.setDataTimestamp(System.currentTimeMillis());
					cacheVo.setRespStr(respObj.respStr);
					cacheVo.setFile(respObj.isFile());
					cacheVo.setRespHeader(respHeader);
					cacheVo.setStatusCode(respObj.statusCode);
					CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
				}
			}
		} else {// call api
			respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostRawDgrReqVo, reqMbody, uuid,
					false);
		}

		// 不論是 cache , 直接call, 統一在這裡 set RESPONSE HEADER
		httpRes = getCommForwardProcService().getConvertResponse(respObj.respHeader, respObj.statusCode, httpRes);

		// 轉換 Response Body 格式
		Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes, httpReq,
				respObj.httpRespArray, respObj.respStr);
		byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
		String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");

		int content_Length = 0;
		if (httpArray != null) {
			content_Length = httpArray.length;
			// http InputStream copy into Array
			IOUtils.copy(new ByteArrayInputStream(httpArray), httpRes.getOutputStream());
		}

		// print
		StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, maskInfo, httpReq);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + resLog.toString());

		// 第一組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, tsmpcPostRawDgrReqVo, httpRespStr, content_Length);
		// 第一組RDB RESP
		getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, tsmpcPostRawDgrReqVo_rdb, httpRespStr,
				content_Length);

		return null;
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start TSMPC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End TSMPC-from-Backend For Cache】--\n");

			// 第二組ES REQ
			TsmpApiLogReq tsmpcPostRawBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(),
					vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getSrcUrl(),
					vo.getReqMbody());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 下載檔案的處理
			if (respObj.respHeader != null && respObj.respHeader.get("Content-Disposition") != null) {
				if (respObj.respHeader.get("Content-Disposition").toString().indexOf("filename") > -1) {
					respObj.respStr = "{\"errMsg\":\"file content can not cache\"}";
				}
			}

			// 4. resp header / body / code
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcPostRawBackendReqVo, contentLength);

			return respObj;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String payload, String uuid,
			boolean isFixedCache) throws Exception {

		StringBuffer sb = new StringBuffer();
		if (isFixedCache) {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend For Fixed Cache】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend For Fixed Cache】--\n");
		} else {
			sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--");
			sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend】--\n");
		}

		// 第二組ES REQ
		TsmpApiLogReq tsmpcPostRawBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header,
				srcUrl, payload);
		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, srcUrl, payload);
		respObj.fetchByte(maskInfo); // because Enable inputStream
		sb.append(respObj.getLogStr());
		TPILogger.tl.debug(sb.toString());

		// 4. resp header / body / code
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;
		// 若respObj.respStr為null帶表有檔案則寫入檔案sha256
//		if (!StringUtils.hasLength(respObj.respStr)){
//			respObj.respStr = getRespFileLog(respObj);
//		}
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcPostRawBackendReqVo, contentLength);
		return respObj;

//		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes);

		// 4. resp header / body / code
		// for logs
//		byte[] httpArray = null;
//		String httpRespStr = null;
//		Map<String, Object> rsMap = new HashMap<>();
		// 若有回傳檔案會包含file Sha256字串
//		if ( !respObj.respStr.contains("file Sha256")) {
////			httpArray = respObj.respStr.getBytes();
////			httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
//			//第二組ES RESP
//			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, bankendReqVo, httpArray.length);
//			rsMap.put("isFile", "0");
//		} else {
////			httpArray = getInputStreamToByteArray(respObj.respInputStreamObj);
////			httpRespStr = getLogHasFile(respObj.respInputStreamObj);
////			byte[] hash = SHA256Util.getSHA256(httpArray);
////			String fileSha256 = HexStringUtils.toString(hash);
//			//第二組ES RESP
//			//不自動遮罩所以給0
//			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, bankendReqVo, respObj.respStr, 0);
//			TPILogger.tl.debug("\n\n##----- file SHA256 -----------\n"+respObj.respStr+"\n");
//			rsMap.put("isFile", "1");
//			rsMap.put("fileSha256", respObj.respStr);
//		}

//		rsMap.put("httpArray", httpArray);
//		rsMap.put("httpRespStr", respObj.respStr);
//		rsMap.put("statusCode", respObj.statusCode);
//		rsMap.put("respHeader", respObj.respHeader);
//		
//		return rsMap;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String payload, String reqUrl)
			throws IOException {
		StringBuffer tsmpcPostRaw_log = new StringBuffer();

		// print
		writeLogger(tsmpcPostRaw_log, "--【URL】--");
		writeLogger(tsmpcPostRaw_log, httpReq.getRequestURI());
		writeLogger(tsmpcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(tsmpcPostRaw_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(tsmpcPostRaw_log, "--【Http Req Header】--");
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
			writeLogger(tsmpcPostRaw_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(tsmpcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(tsmpcPostRaw_log, "--【Req payload / Form Data】");
		writeLogger(tsmpcPostRaw_log, getCommForwardProcService().maskBody(maskInfo, payload));
		writeLogger(tsmpcPostRaw_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return tsmpcPostRaw_log;
	}

	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	protected HttpRespData getHttpRespData(String httpMethod, Map<String, List<String>> header, String reqUrl,
			String payload) throws Exception {

		HttpRespData httpRespData = HttpUtil.httpReqByRawDataList(reqUrl, httpMethod, payload, header, true, false,
				maskInfo);
		// 判斷是否有檔案 有檔案回傳Stream 無則回傳String
//		if (!httpRespData.respHeader.containsKey("Content-Disposition")){
//			httpRespData.respStr = HttpUtil.toPrettyJson(HttpUtil.read(httpRespData.respInputStreamObj));
//		}

		return httpRespData;
	}

	private String getLogHasFile(InputStream inputStream) {
		String v = "";
		byte[] bytes;
		try {
			bytes = inputStream.readAllBytes();
			if (bytes != null && bytes.length > 0) {
				v = HexStringUtils.toString(bytes);
				if (v.length() > 100) {
					v = v.substring(0, 100) + "...";
				}
			}
		} catch (Exception e) {
			v = "[Unreadble content]";
		}
		return v;
	}

	private byte[] getInputStreamToByteArray(InputStream inputStream) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
		} catch (Exception e) {
			TPILogger.tl.debug("InputStream To ByteArray is error");
		}
		return buffer.toByteArray();
	}

	public String getRespFileLog(HttpRespData respObj) throws UnsupportedEncodingException {
//		byte[] httpArray = getInputStreamToByteArray(respObj.respInputStreamObj);
//		byte[] hash = SHA256Util.getSHA256(httpArray);
//		String fileSha256Log = "--file Sha256\n";
//		String fileSha256 = HexStringUtils.toString(hash);
//		fileSha256Log = fileSha256Log + fileSha256;
//		respObj.respInputStreamObj = new ByteArrayInputStream(httpArray);
//		return fileSha256Log;
		return null;
	}

	protected boolean checkIfMockTest(HttpHeaders httpHeaders) {
		return this.mockApiTestService.checkIfMockTest(httpHeaders);
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected ProxyMethodServiceCacheProxy getProxyMethodServiceCacheProxy() {
		return proxyMethodServiceCacheProxy;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload, TsmpApiLogReq dgrReqVo,
			TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch);
	}

}
