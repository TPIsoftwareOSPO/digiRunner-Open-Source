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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

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
public class TSMPCServicePostForm implements IApiCacheService {

	private CommForwardProcService commForwardProcService;
	private MultipartResolver multipartResolver;
	private ObjectMapper objectMapper;
	private MockApiTestService mockApiTestService;
	private ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy;
	private TsmpSettingService tsmpSettingService;

	private Map<String, String> maskInfo;

	@Autowired
	public TSMPCServicePostForm(CommForwardProcService commForwardProcService, MultipartResolver multipartResolver,
			ObjectMapper objectMapper, MockApiTestService mockApiTestService,
			ProxyMethodServiceCacheProxy proxyMethodServiceCacheProxy, TsmpSettingService tsmpSettingService) {
		super();
		this.commForwardProcService = commForwardProcService;
		this.multipartResolver = multipartResolver;
		this.objectMapper = objectMapper;
		this.mockApiTestService = mockApiTestService;
		this.proxyMethodServiceCacheProxy = proxyMethodServiceCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
	}

	public ResponseEntity<?> forwardToPostFormData(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes) throws Exception {
		MultipartHttpServletRequest multipartRequest = null;
		try {
			// 包含檔案的轉發
			multipartRequest = toMultipartRequest(httpReq);
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			boolean hasFile = !CollectionUtils.isEmpty(fileMap);
			if (hasFile) {
				return forwardTo(multipartRequest, httpRes, httpHeaders);
			}

			// 不包含檔案的轉發
			return forwardTo(httpReq, httpRes, httpHeaders);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}

	protected ResponseEntity<?> forwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {

		String reqUrl = httpReq.getRequestURI();
		String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String uuid = UUID.randomUUID().toString();
		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();
		Map<String, String> partContentTypes = new HashMap<>();
		for (Part part : httpReq.getParts()) {
			partContentTypes.put(part.getName(), part.getContentType());
		}
		// 判斷是否需要cApikey
		boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
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
		StringBuffer reqLog = getLogReq(httpReq, httpHeaders, reqUrl);

		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + reqLog.toString());

		ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg);

		// 1. req header / body
		// print
		// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
		String reqMbody = getCommForwardProcService().getReqMbody(httpReq);
		TsmpApiLogReq tsmpcPostFormDgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, reqMbody,
				"tsmpc", aType);
		// 第一組 RDB Req
		TsmpApiLogReq tsmpcPostFormDgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq,
				reqMbody, "tsmpc", aType);

		// JWT 資料驗證有錯誤
		if (verifyResp != null) {
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n"
					+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
			// 第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, tsmpcPostFormDgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, tsmpcPostFormDgrReqVo_rdb, respMbody);
			return verifyResp;
		}

		String srcUrl = getCommForwardProcService().getSrcUrl(httpReq);

		if (!StringUtils.hasText(srcUrl)) {
			return null;
		}
		// 判斷是否啟用Path Parameter選項
		boolean tsmpcPostForm_isURLRID = getCommForwardProcService().isURLRID(moduleName, apiId);
		// 進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
		// 取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
		// 完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
		if (tsmpcPostForm_isURLRID)
			srcUrl = srcUrl + commForwardProcService.getTsmpcPathParameter(reqUrl);

		int tsmpcPostForm_tokenPayload = apiReg.getFunFlag();

		// For API mock test
		boolean isMockTest = checkIfMockTest(httpHeaders);
		if (isMockTest) {
			return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tsmpcPostForm_tokenPayload,
					tsmpcPostFormDgrReqVo, tsmpcPostFormDgrReqVo_rdb, cApiKeySwitch, apiReg);
		} else {
			// 2. tsmpc req header / body
			// 3. tsmpc resp header / body / code

			// http header
			Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
					tsmpcPostForm_tokenPayload, cApiKeySwitch, uuid, false);
			HttpRespData tsmpcPostForm_respObj = new HttpRespData();

			// 2,3道是否走cache
			String autoCacheId = getCommForwardProcService().getAutoCacheIdByFlagStart(tsmpcPostFormDgrReqVo, srcUrl,
					reqMbody);
			String fixedCacheId = getCommForwardProcService().getFixedCacheIdByFlagStart(tsmpcPostFormDgrReqVo, srcUrl,
					reqMbody);
			if (StringUtils.hasText(autoCacheId)) {
				AutoCacheParamVo paramVo = new AutoCacheParamVo();
				paramVo.setHeader(header);
				paramVo.setReqMbody(reqMbody);
				paramVo.setSrcUrl(srcUrl);
				paramVo.setDgrReqVo(tsmpcPostFormDgrReqVo);
				paramVo.setUuid(uuid);
				paramVo.setHttpMethod(httpReq.getMethod());
				paramVo.setParamMap(httpReq.getParameterMap());
				paramVo.setPartContentTypes(partContentTypes);
				AutoCacheRespVo apiCacheRespVo = getProxyMethodServiceCacheProxy().queryByIdCallApi(autoCacheId, this,
						paramVo);
				if (apiCacheRespVo != null) {// 走cache
					tsmpcPostForm_respObj.setRespData(apiCacheRespVo.getStatusCode(), apiCacheRespVo.getRespStr(),
							apiCacheRespVo.getHttpRespArray(), apiCacheRespVo.getRespHeader());
					// 此行因為httpRes不能放在callback,所以移到外層
					// httpRes =
					// getCommForwardProcService().getConvertResponse(apiCacheRespVo.getRespHeader(),
					// apiCacheRespVo.getStatusCode(), httpRes);
				} else {// cache發生未知錯誤,call api
					tsmpcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostFormDgrReqVo,
							reqMbody, uuid, false, apiReg);
				}
			} else if (StringUtils.hasText(fixedCacheId)) {// 固定cache
				FixedCacheVo cacheVo = CommForwardProcService.fixedCacheMap.get(fixedCacheId);
				if (cacheVo != null) {// 走cache
					boolean isUpdate = getCommForwardProcService().isFixedCacheUpdate(cacheVo, tsmpcPostFormDgrReqVo);
					if (isUpdate) {// 更新紀錄
						tsmpcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl,
								tsmpcPostFormDgrReqVo, reqMbody, uuid, true, apiReg);
						// statusCode小於400才更新紀錄
						if (tsmpcPostForm_respObj.statusCode < 400) {
							cacheVo.setData(tsmpcPostForm_respObj.httpRespArray);
							cacheVo.setDataTimestamp(System.currentTimeMillis());
							cacheVo.setRespHeader(tsmpcPostForm_respObj.respHeader);
							cacheVo.setStatusCode(tsmpcPostForm_respObj.statusCode);
							cacheVo.setRespStr(tsmpcPostForm_respObj.respStr);
							CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
						} else {// 否則就取上次紀錄
							tsmpcPostForm_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(),
									cacheVo.getData(), cacheVo.getRespHeader());
						}
					} else {// 取得cache資料
						tsmpcPostForm_respObj.setRespData(cacheVo.getStatusCode(), cacheVo.getRespStr(),
								cacheVo.getData(), cacheVo.getRespHeader());
					}
				} else {// call api
					tsmpcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostFormDgrReqVo,
							reqMbody, uuid, true, apiReg);
					// statusCode小於400才紀錄
					if (tsmpcPostForm_respObj.statusCode < 400) {
						cacheVo = new FixedCacheVo();
						cacheVo.setData(tsmpcPostForm_respObj.httpRespArray);
						cacheVo.setDataTimestamp(System.currentTimeMillis());
						cacheVo.setRespHeader(tsmpcPostForm_respObj.respHeader);
						cacheVo.setStatusCode(tsmpcPostForm_respObj.statusCode);
						cacheVo.setRespStr(tsmpcPostForm_respObj.respStr);
						CommForwardProcService.fixedCacheMap.put(fixedCacheId, cacheVo);
					}
				}

			} else {// call api
				tsmpcPostForm_respObj = this.callForwardApi(header, httpReq, httpRes, srcUrl, tsmpcPostFormDgrReqVo,
						reqMbody, uuid, false, apiReg);
			}

			httpRes = getCommForwardProcService().getConvertResponse(tsmpcPostForm_respObj.respHeader,
					tsmpcPostForm_respObj.statusCode, httpRes, apiReg);

			// 轉換 Response Body 格式
			Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes,
					httpReq, tsmpcPostForm_respObj.httpRespArray, tsmpcPostForm_respObj.respStr);
			byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
			String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");

			int content_Length = 0;
			if (httpArray != null) {
				content_Length = httpArray.length;
				ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
				// http InputStream copy into Array
				IOUtils.copy(bi, httpRes.getOutputStream());
				// String httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
			}

			// print
			StringBuffer resLog = getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length,
					maskInfo, httpReq);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n" + resLog.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, tsmpcPostFormDgrReqVo, httpRespStr,
					content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, tsmpcPostFormDgrReqVo_rdb, httpRespStr,
					content_Length);

			return null;
		}
	}

	public HttpRespData callback(AutoCacheParamVo vo) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【Start TSMPC-to-Backend For Cache】--");
			sb.append("\n--【LOGUUID】【" + vo.getUuid() + "】【End TSMPC-from-Backend For Cache】--\n");

			// 第二組ES REQ
			TsmpApiLogReq tsmpcPostFormBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(vo.getDgrReqVo(),
					vo.getHeader(), vo.getSrcUrl(), vo.getReqMbody());

			HttpRespData respObj = getHttpRespData(vo.getHttpMethod(), vo.getHeader(), vo.getParamMap(), vo.getSrcUrl(),
					vo.getPartContentTypes());
			respObj.fetchByte(maskInfo); // because Enable inputStream
			sb.append(respObj.getLogStr());
			TPILogger.tl.debug(sb.toString());

			// 4. resp header / body / code
			// byte[] httpArray = respObj.respStr.getBytes();
			byte[] httpArray = respObj.httpRespArray;
			int contentLength = (httpArray == null) ? 0 : httpArray.length;

			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcPostFormBackendReqVo, contentLength);

			return respObj;
		} catch (Exception tsmpcPostForm_e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(tsmpcPostForm_e));
			return null;
		}

	}

	private HttpRespData callForwardApi(Map<String, List<String>> header, HttpServletRequest httpReq,
			HttpServletResponse httpRes, String srcUrl, TsmpApiLogReq dgrReqVo, String reqMbody, String uuid,
			boolean isFixedCache, TsmpApiReg tsmpApiReg) throws Exception {

		StringBuffer tsmpcPostForm_sb = new StringBuffer();
		if (isFixedCache) {
			tsmpcPostForm_sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend For Fixed Cache】--");
			tsmpcPostForm_sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend For Fixed Cache】--\n");
		} else {
			tsmpcPostForm_sb.append("\n--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--");
			tsmpcPostForm_sb.append("\n--【LOGUUID】【" + uuid + "】【End TSMPC-from-Backend】--\n");
		}

		var partsInfo = getCommForwardProcService().fetchFormDataPartsInfo(httpReq);

		// 第二組ES REQ
		TsmpApiLogReq tsmpcPostFormBackendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header,
				srcUrl, reqMbody);
		HttpRespData respObj = getHttpRespData(httpReq.getMethod(), header, partsInfo.getB(), srcUrl,
				partsInfo.getA());
		respObj.fetchByte(maskInfo); // because Enable inputStream
		tsmpcPostForm_sb.append(respObj.getLogStr());
		TPILogger.tl.debug(tsmpcPostForm_sb.toString());
		
		// Must call respObj.getLogStr() first
		// Threshhold > 10,000 => print warn msg.
		Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid)).ifPresent(TPILogger.tl::warn);
		httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
		httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);

		httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes, tsmpApiReg);

		// 4. resp header / body / code
		// byte[] httpArray = respObj.respStr.getBytes();
		byte[] httpArray = respObj.httpRespArray;
		int contentLength = (httpArray == null) ? 0 : httpArray.length;

		// 第二組ES RESP
		getCommForwardProcService().addEsTsmpApiLogResp2(respObj, tsmpcPostFormBackendReqVo, contentLength);

		return respObj;
	}

	private StringBuffer getLogReq(HttpServletRequest httpReq, HttpHeaders httpHeaders, String reqUrl)
			throws IOException, ServletException {
		StringBuffer tsmpcPostFormLog_log = new StringBuffer();

		// print
		writeLogger(tsmpcPostFormLog_log, "--【URL】--");
		writeLogger(tsmpcPostFormLog_log, httpReq.getRequestURI());
		writeLogger(tsmpcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		writeLogger(tsmpcPostFormLog_log, "【" + httpReq.getMethod() + "】\r\n");

		// print header
		writeLogger(tsmpcPostFormLog_log, "--【Http Req Header】--");
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
			writeLogger(tsmpcPostFormLog_log, "\tKey: " + key + ", Value: " + value);
		}
		writeLogger(tsmpcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// print body
		writeLogger(tsmpcPostFormLog_log, "--【Req payload / Form Data】");
		Collection<Part> parts;

		parts = httpReq.getParts();
		for (Part part : parts) {
			String name = part.getName();
			String contentType = part.getContentType();
			String value = httpReq.getParameter(name);
			writeLogger(tsmpcPostFormLog_log,
					"\tKey: " + name + ", Value: "
							+ getCommForwardProcService().maskBodyFromFormData(maskInfo, name, value)
							+ ", Content-Type: " + contentType);
		}
		// httpReq.getParameterMap().forEach((k, vs) -> {
		// if (vs.length != 0) {
		// for (String v : vs) {
		// writeLogger(tsmpcPostFormLog_log, "\tKey: " + k + ", Value: " + v);
		// }
		// }
		// });
		writeLogger(tsmpcPostFormLog_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		return tsmpcPostFormLog_log;
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

	public void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	// 包含檔案的轉發
	protected ResponseEntity<?> forwardTo(MultipartHttpServletRequest httpReq, HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {
		String reqUrl = httpReq.getRequestURI();
		String uuid = UUID.randomUUID().toString();

		String moduleName = httpReq.getAttribute(GatewayFilter.MODULE_NAME).toString();
		String apiId = httpReq.getAttribute(GatewayFilter.API_ID).toString();

		boolean cApiKeySwitch = getCommForwardProcService().getcApiKeySwitch(moduleName, apiId);
		StringBuffer tsmpcPostForm_log = new StringBuffer();
		// 0. 【Start TSMPC】
		// writeLogger(log, "--【LOGUUID】【" + uuid + "】【Start TSMPC】--");
		// 檢查資料
		TsmpApiReg apiReg = getCommForwardProcService().getTsmpApiReg(httpReq);
		ResponseEntity<?> allowMethodErrRespEntity = getCommForwardProcService().checkMethod(apiReg, httpReq);
		if (allowMethodErrRespEntity != null) {
			return allowMethodErrRespEntity;
		}
		if (apiReg != null) {

			maskInfo = new HashMap<>();
			maskInfo.put("bodyMaskPolicy", apiReg.getBodyMaskPolicy());
			maskInfo.put("bodyMaskPolicySymbol", apiReg.getBodyMaskPolicySymbol());
			maskInfo.put("bodyMaskPolicyNum", String.valueOf(apiReg.getBodyMaskPolicyNum()));
			maskInfo.put("bodyMaskKeyword", apiReg.getBodyMaskKeyword());

			maskInfo.put("headerMaskPolicy", apiReg.getHeaderMaskPolicy());
			maskInfo.put("headerMaskPolicySymbol", apiReg.getHeaderMaskPolicySymbol());
			maskInfo.put("headerMaskPolicyNum", String.valueOf(apiReg.getHeaderMaskPolicyNum()));
			maskInfo.put("headerMaskKey", apiReg.getHeaderMaskKey());
		}

		// 1. 【URL】
		writeLogger(tsmpcPostForm_log, "--【URL】--");
		writeLogger(tsmpcPostForm_log, httpReq.getRequestURI());
		writeLogger(tsmpcPostForm_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// 2. 【HTTP METHOD】
		writeLogger(tsmpcPostForm_log, "【" + httpReq.getMethod() + "】\r\n");

		// 3. 【Http Req Header】
		writeLogger(tsmpcPostForm_log, "--【Http Req Header】--");
		Enumeration<String> headerKeys = httpReq.getHeaderNames();
		while (headerKeys.hasMoreElements()) {
			String key = headerKeys.nextElement();
			List<String> valueList = httpHeaders.get(key);
			String value = null;
			if (!CollectionUtils.isEmpty(valueList)) {
				value = getCommForwardProcService().convertAuth(key, valueList.toString(), maskInfo);
			}
			writeLogger(tsmpcPostForm_log,
					"\tKey: " + key + ", Value: " + getCommForwardProcService().maskHeader(maskInfo, key, value));
		}
		writeLogger(tsmpcPostForm_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");

		// 4. 【Req payload / Form Data】
		writeLogger(tsmpcPostForm_log, "--【Req payload / Form Data】--");
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
			String name;
			String[] vals;
			byte[] data;
			for (Map.Entry<String, String[]> entries : parameterMap.entrySet()) {
				name = entries.getKey();
				vals = entries.getValue();
				for (String val : vals) {
					Map<String, Object> dataMap = HttpUtil.getFormBodyPart(name, null, val.getBytes(), boundary,
							tsmpcPostForm_log, partContentTypes.get(name), maskInfo);
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
			String name;
			MultipartFile mf;
			byte[] data;
			for (Map.Entry<String, MultipartFile> entries : fileMap.entrySet()) {
				name = entries.getKey();
				mf = entries.getValue();
				String contentType = mf.getContentType(); // Get the content type from the MultipartFile
				Map<String, Object> dataMap = HttpUtil.getFormBodyPart(name, mf.getOriginalFilename(), mf.getBytes(),
						boundary, tsmpcPostForm_log, contentType, null);
				data = (byte[]) dataMap.get("data");
				formBodyParts.add(data);
				Map<String, Object> logData = (Map<String, Object>) dataMap.get("logData");
				formBodyParts_File2Hex.add((byte[]) logData.get("contentD"));
				formBodyParts_File2Hex.add((byte[]) logData.get("content"));
			}
		}

		byte[] formBody = HttpUtil.getFormBody(formBodyParts, boundary, partContentTypes);
		byte[] formBody2Hex = HttpUtil.getFormBody(formBodyParts_File2Hex, boundary, partContentTypes);
		writeLogger(tsmpcPostForm_log, "--【End】 " + StackTraceUtil.getLineNumber() + " --\r\n");
		String reqMbody = new String(formBody2Hex);
		String aType = "R";

		ResponseEntity<?> verifyResp = getCommForwardProcService().verifyData(httpRes, httpReq, httpHeaders, apiReg);

		// 第一組ES REQ (一定要在 CommForwardProcService.verifyData 之後才能記 Log)
		TsmpApiLogReq dgrReqVo = getCommForwardProcService().addEsTsmpApiLogReq1(uuid, httpReq, reqMbody, "tsmpc",
				aType);
		// 第一組 RDB Req
		TsmpApiLogReq dgrReqVo_rdb = getCommForwardProcService().addRdbTsmpApiLogReq1(uuid, httpReq, reqMbody, "tsmpc",
				aType);
		TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【Start TSMPC】--\n" + tsmpcPostForm_log.toString());

		// JWT 資料驗證有錯誤
		if (verifyResp != null) {
			// 第一組ES RESP
			String respMbody = getObjectMapper().writeValueAsString(verifyResp.getBody());
			getCommForwardProcService().addEsTsmpApiLogResp1(verifyResp, dgrReqVo, respMbody);
			getCommForwardProcService().addRdbTsmpApiLogResp1(verifyResp, dgrReqVo_rdb, respMbody);
			TPILogger.tl.debug("\n--【LOGUUID】【" + uuid + "】【End TSMPC】--\n"
					+ getCommForwardProcService().getLogResp(verifyResp, maskInfo, httpReq).toString());
			return verifyResp;
		}

		String srcUrl = getCommForwardProcService().getSrcUrl(httpReq);

		if (!StringUtils.hasText(srcUrl)) {
			return null;
		}

		// 進入tsmpc的url為/tsmpc/minTest/deletebustaxi/a/bb/ccc/1
		// 取出a/bb/ccc/1後，接在要轉發的url http://127.0.0.1:8080/dgrv4/mocktest/delete/api
		// 完整轉發url為http://127.0.0.1:8080/dgrv4/mocktest/delete/api/a/bb/ccc/1
		srcUrl = srcUrl + commForwardProcService.getTsmpcPathParameter(reqUrl);

		int tokenPayload = apiReg.getFunFlag();

		// For API mock test
		boolean isMockTest = checkIfMockTest(httpHeaders);
		if (isMockTest) {
			return mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload, dgrReqVo, dgrReqVo_rdb,
					null, apiReg);
		} else {
			// 5. 【Start TSMPC-to-Backend】
			// 6. 【End TSMPC-from-Backend】
			tsmpcPostForm_log = new StringBuffer();
			writeLogger(tsmpcPostForm_log, //
					"--【LOGUUID】【" + uuid + "】【Start TSMPC-to-Backend】--\n" + "--【LOGUUID】【" + uuid
							+ "】【End TSMPC-from-Backend】--");

			// ===== ↓ NOTHING TO DO WITH LOGGER ↓ =====

			Map<String, List<String>> header = getCommForwardProcService().getConvertHeader(httpReq, httpHeaders,
					tokenPayload, cApiKeySwitch, uuid, false);

			String httpMethod = httpReq.getMethod();
			// 第二組ES REQ
			TsmpApiLogReq backendReqVo = getCommForwardProcService().addEsTsmpApiLogReq2(dgrReqVo, header, srcUrl,
					reqMbody);
			HttpRespData respObj = forwardWithFile(srcUrl, httpMethod, header, boundary, formBody, formBody2Hex);
			respObj.fetchByte(maskInfo); // because Enable inputStream
			// ===== ↑ NOTHING TO DO WITH LOGGER ↑ =====

			// 7. 呼叫 backend API 的過程
			writeLogger(tsmpcPostForm_log, respObj.getLogStr());
			
			// Must call respObj.getLogStr() first
			// Threshhold > 10,000 => print warn msg.
			Optional.ofNullable(respObj.loggerElapsedTimeMsg(uuid)).ifPresent(TPILogger.tl::warn);
			httpReq.setAttribute(GatewayFilter.HTTP_CODE23, respObj.statusCode);
			httpReq.setAttribute(GatewayFilter.ELAPSED_TIME23, respObj.elapsedTime);

			// 8. 【End TSMPC】
			writeLogger(tsmpcPostForm_log, "--【LOGUUID】【" + uuid + "】【End TSMPC】--");

			// ===== ↓ NOTHING TO DO WITH LOGGER ↓ =====

			httpRes = getCommForwardProcService().getConvertResponse(respObj, httpRes, apiReg);

			// byte[] httpArray = respObj.respStr.getBytes();
			// byte[] httpArray =respObj.httpRespArray;

			// 轉換 Response Body 格式
			Map<String, Object> convertResponseBodyMap = getCommForwardProcService().convertResponseBody(httpRes,
					httpReq, respObj.httpRespArray, respObj.respStr);
			byte[] httpArray = (byte[]) convertResponseBodyMap.get("httpArray");
			String httpRespStr = (String) convertResponseBodyMap.get("httpRespStr");
			int content_Length = (httpArray == null) ? 0 : httpArray.length;
			// 第二組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp2(respObj, backendReqVo, content_Length);

			if (httpArray != null) {
				content_Length = httpArray.length;
				ByteArrayInputStream bi = new ByteArrayInputStream(httpArray);
				IOUtils.copy(bi, httpRes.getOutputStream()); // http InputStream copy into Array
			}

			// ===== ↑ NOTHING TO DO WITH LOGGER ↑ =====

			// 9. 【Http Resp Header】
			// 10.【Http status code】
			// 11.【Resp payload / Form Data】
			// String httpRespStr = new String(httpArray , StandardCharsets.UTF_8);
			getCommForwardProcService().getLogResp(httpRes, httpRespStr, content_Length, tsmpcPostForm_log, maskInfo,
					httpReq);

			// print
			TPILogger.tl.debug("\n" + tsmpcPostForm_log.toString());

			// 第一組ES RESP
			getCommForwardProcService().addEsTsmpApiLogResp1(httpRes, dgrReqVo, httpRespStr, content_Length);
			// 第一組RDB RESP
			getCommForwardProcService().addRdbTsmpApiLogResp1(httpRes, dgrReqVo_rdb, httpRespStr, content_Length);

			return null;
		}
	}

	// 若有包含檔案則轉型
	private MultipartHttpServletRequest toMultipartRequest(HttpServletRequest httpServletRequest) {
		MultipartHttpServletRequest multipartHttpServletRequest = this.multipartResolver
				.resolveMultipart(httpServletRequest);
		return multipartHttpServletRequest;
	}

	private HttpRespData forwardWithFile(String reqUrl, String method, Map<String, List<String>> httpHeader, //
			String boundary, byte[] formBody, byte[] formBody2Hex) throws IOException {
		boolean isEnableInputStream = true;
		boolean isRedirect = false;
		HttpRespData respObj = HttpUtil.httpReqByFormDataList( //
				reqUrl, method, boundary, formBody, formBody2Hex, httpHeader, isEnableInputStream, isRedirect,
				maskInfo);
		return respObj;
	}

	private String parseBoundary(MultipartHttpServletRequest multipartReq) {
		HttpHeaders httpHeaders = multipartReq.getRequestHeaders();
		MediaType contentType = httpHeaders.getContentType();
		String boundary;
		if (contentType == null) {
			// not exceed 70 bytes in length and consists only of 7-bit US-ASCII (printable)
			// characters
			boundary = "TsmpcBoundary" + UUID.randomUUID().toString();
		} else {
			boundary = contentType.getParameter("boundary");
		}
		return boundary;
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

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ResponseEntity<?> mockForwardTo(HttpServletRequest httpReq, HttpServletResponse httpRes,
			HttpHeaders httpHeaders, String srcUrl, String uuid, int tokenPayload, TsmpApiLogReq dgrReqVo,
			TsmpApiLogReq dgrReqVo_rdb, Boolean cApiKeySwitch, TsmpApiReg tsmpApiReg) throws Exception {
		return this.mockApiTestService.mockForwardTo(httpReq, httpRes, httpHeaders, srcUrl, uuid, tokenPayload,
				dgrReqVo, dgrReqVo_rdb, cApiKeySwitch, tsmpApiReg);
	}

}