package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.util.tuples.Pair;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.codec.utils.JWScodec;
import tpi.dgrv4.codec.utils.MaskUtil;
import tpi.dgrv4.codec.utils.PEMUtil;
import tpi.dgrv4.codec.utils.ProbabilityAlgUtils;
import tpi.dgrv4.codec.utils.RandomUtil;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.ifs.TraceCodeUtilIfs;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.es.DiskSpaceMonitor;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.component.DgrcRoutingHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientCertCacheProxy;
import tpi.dgrv4.gateway.component.check.TokenCheck;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.FixedCacheVo;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.TWHeader;
import tpi.dgrv4.gateway.vo.TWReqRespJwe;
import tpi.dgrv4.gateway.vo.TWReqRespJweRecipients;
import tpi.dgrv4.gateway.vo.TWReqRespJws;
import tpi.dgrv4.gateway.vo.TWReqRespJwsSignatures;
import tpi.dgrv4.gateway.vo.TWResponse;
import tpi.dgrv4.gateway.vo.TWResponseBody;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;
import tpi.dgrv4.gateway.vo.TsmpApiLogResp;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.httpu.utils.HttpsConnectionChecker;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Slf4j
@Service
public class CommForwardProcService {
	
	@Value("${cors.allow.headers}")
	private String corsAllowHeaders;

	public static Map<String, FixedCacheVo> fixedCacheMap = new HashMap<>();
	public static final String AUTHORIZATION = "authorization";
	public static final String TOKENPAYLOAD = "tokenpayload";
	public static final String BACKAUTH = "backauth";
	private static final String MODULE_NAME = "moduleName";
	private static final List<String> MASK_TOKEN_LIST = new ArrayList<>();

	static {
		MASK_TOKEN_LIST.add(CommForwardProcService.AUTHORIZATION);
		MASK_TOKEN_LIST.add(CommForwardProcService.TOKENPAYLOAD);
		MASK_TOKEN_LIST.add(CommForwardProcService.BACKAUTH);
	}

	private ExecutorService executor;
	
//	@Autowired(required = false)
	private TraceCodeUtilIfs traceCodeUtil;
	
	private DgrcRoutingHelper dgrcRoutingHelper;
	private OAuthTokenService oAuthTokenService;
	
	private TokenHelper tokenHelper;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private JobHelper jobHelper;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private ObjectMapper objectMapper;
	private TsmpSettingService tsmpSettingService;
	private TokenCheck tokenCheck;
	private TsmpReqLogDao tsmpReqLogDao;
	private TsmpResLogDao tsmpResLogDao;
	private TsmpClientCacheProxy tsmpClientCacheProxy;
	private TsmpClientCertCacheProxy tsmpClientCertCacheProxy;

	public static class ClientPublicKeyData {
		private ResponseEntity<?> errRespEntity;
		private PublicKey clientPublicKey;
		public ResponseEntity<?> getErrRespEntity() {
			return errRespEntity;
		}
		public void setErrRespEntity(ResponseEntity<?> errRespEntity) {
			this.errRespEntity = errRespEntity;
		}
		public PublicKey getClientPublicKey() {
			return clientPublicKey;
		}
		public void setClientPublicKey(PublicKey clientPublicKey) {
			this.clientPublicKey = clientPublicKey;
		}
	}
 
	@Autowired
	public void setCommForwardProcService(@Nullable TraceCodeUtilIfs traceCodeUtil, DgrcRoutingHelper dgrcRoutingHelper,
			OAuthTokenService oAuthTokenService, TokenHelper tokenHelper, TsmpApiCacheProxy tsmpApiCacheProxy,
			JobHelper jobHelper, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			TsmpCoreTokenEntityHelper tsmpCoreTokenHelper, ObjectMapper objectMapper,
			TsmpSettingService tsmpSettingService, TokenCheck tokenCheck, TsmpReqLogDao tsmpReqLogDao,
			TsmpResLogDao tsmpResLogDao, TsmpClientCacheProxy tsmpClientCacheProxy,
			TsmpClientCertCacheProxy tsmpClientCertCacheProxy) {
		this.executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("CommForwardProcService"));
		this.traceCodeUtil = traceCodeUtil;

		this.tokenHelper = tokenHelper;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.jobHelper = jobHelper;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.tokenCheck = tokenCheck;
		this.tsmpReqLogDao = tsmpReqLogDao;
		this.tsmpResLogDao = tsmpResLogDao;
		this.tsmpClientCacheProxy = tsmpClientCacheProxy;
		this.tsmpClientCertCacheProxy = tsmpClientCertCacheProxy;

		// Because using constructor injection will cause a circular dependency, use
		// method injection instead
		this.dgrcRoutingHelper = dgrcRoutingHelper;
		this.oAuthTokenService = oAuthTokenService;
	}

	@PreDestroy
	public void destroy() {
		if (this.executor != null && !this.executor.isShutdown()) {
			this.executor.shutdown();
		}
	}

	// No Auth 找不到有效的憑證
	public static final String NO_AUTH_NO_VALID_CREDENTIALS_FOUND = "'No Auth' no valid credentials found.";
	// 沒有 client ID 找不到有效的憑證
	public static final String MISSING_CLIENT_ID_NO_VALID_CERTIFICATE_FOUND = "Missing client ID, no valid certificate found.";
	// 找不到有效的憑證
	public static final String NO_VALID_CERTIFICATE_FOUND = "No valid certificate found, client_id: ";
	// 產生公鑰失敗
	public static final String FAILED_TO_GENERATE_PUBLIC_KEY = "Failed to generate public key, client_id: ";


	/**
	 *
	 * @param httpReq
	 * @return A: partContentTypes B: parameterMap
	 * @throws ServletException IOException
	 */
	public Pair<Map<String, String>, Map<String, String[]>> fetchFormDataPartsInfo(HttpServletRequest httpReq) throws ServletException, IOException {
		Map<String, String> partContentTypes = new HashMap<>();
		Map<String, String[]> parametermap = new HashMap<>();
		for (Part part : httpReq.getParts()) {
			String contentType = part.getContentType();
			partContentTypes.put(part.getName(), contentType);
			var textContent = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			if (StringUtils.hasLength(contentType) && contentType.contains(";")) {
				Charset charset = Charset.forName(contentType.split(";")[1].split("=")[1]);
				textContent = new String(textContent.getBytes(charset), charset);
			}

			parametermap.put(part.getName(), new String[]{textContent});

		}
		return new Pair<>(partContentTypes, parametermap);
	}

	public String getSrcUrl(HttpServletRequest httpReq) throws Exception {
		String srcUrl = null;
		TsmpApi tsmpApi = getTsmpApi(httpReq);
		String apiSrc = tsmpApi.getApiSrc();
		if ("C".equals(apiSrc)) {// 組合
			srcUrl = getDgrcRoutingHelper().getComposerSrcUrl(tsmpApi);
		} else {
			TsmpApiReg apiReg = getTsmpApiReg(httpReq);
			srcUrl = getSrcUrlForLB(apiReg.getSrcUrl());
		}
		return srcUrl;
	}

	/**
	 * 將 srcUrl 切割, 取得機率和目標URL, String[]: 值為 probability 1 + "", targetUrl 1 <br>
	 * 1.若 srcUrl 值不是 "b64." 開頭, 為 URL 明碼, 只有一筆, 即要打的 URL <br>
	 * 2.若 srcUrl 值為 "b64." 開頭, 用 "." <br>
	 * 切割, 得到 Array, 可能有多筆機率和URL <br>
	 * 
	 * @param srcUrl
	 * @param isThrowing 是否要丟出例外
	 * @return
	 */
	public List<String[]> getSrcUrlToTargetUrlAndProbabilityList(String srcUrl, boolean isThrowing) {
		// 1.若 srcUrl 沒有值，則 return null
		if (!StringUtils.hasLength(srcUrl)) {
			TPILogger.tl.debug("srcUrl has no value");
			if (isThrowing) {
				throw TsmpDpAaRtnCode._1350.throwing("{{srcUrl}}");
			}
			return null;
		}

		// 2.若 srcUrl 值不是 "b64." 開頭，為 URL 明碼，只有一筆，即要打的 URL
		LinkedList<String[]> srcUrlDataList = new LinkedList<String[]>(); // String[]: 值為 probability 1 + "", targetUrl
																			// 1
		String key = "b64.";
		int index = srcUrl.indexOf(key);
		if (index != 0) {// 不是 "b64." 開頭
			String[] dataArr = new String[2];
			dataArr[0] = 100 + "";
			dataArr[1] = srcUrl;
			srcUrlDataList.add(dataArr);
			return srcUrlDataList;
		}

		// 3.若 srcUrl 值為 "b64." 開頭，表示內容為 "b64." + 機率 1 + "." + Base64URL Encode(url 1) +
		// "." + 機率 2 + "." + Base64URL Encode(url 2) + ......
		// (1).取得 "b64." 後面的文字，用 "." 切割，得到 Array，可能有多筆機率和URL
		String b64Str = srcUrl.substring(index + key.length());
		String[] b64Arr = b64Str.split("\\.");

		int totalProbability = 0;// 機率的總合
		for (int i = 0; i < b64Arr.length; i = i + 2) {
			String[] dataArr = new String[2];
			// (2).Array 第1、3、...... 個元素: 機率，即 probability
			// 若沒有值，則 return null
			String probabilityStr = b64Arr[i];
			if (!StringUtils.hasLength(probabilityStr)) {
				TPILogger.tl.debug("probability has no value");
				if (isThrowing) {
					throw TsmpDpAaRtnCode._1350.throwing("{{probability}}");
				}
				return null;
			}
			// 若不為數字，則 return null
			int probability = 0;
			try {
				probability = Integer.valueOf(probabilityStr);
			} catch (Exception e) {
				TPILogger.tl.debug("probability can only enter integers, probability: " + probabilityStr);
				if (isThrowing) {
					throw TsmpDpAaRtnCode._1527.throwing("{{probability}}");
				}
				return null;
			}

			// (3).Array 第2、4、...... 個元素: 經過 Base64URL Decode，取得 URL，即 targetUrl
			// 若沒有值，則 return null
			String targetUrl = b64Arr[i + 1];
			if (!StringUtils.hasLength(targetUrl)) {
				TPILogger.tl.debug("targetUrl has no value");
				if (isThrowing) {
					throw TsmpDpAaRtnCode._1350.throwing("{{targetUrl}}");
				}
				return null;
			}

			try {
				targetUrl = new String(Base64Util.base64URLDecode(targetUrl));
			} catch (Exception e) {
				TPILogger.tl.error("Illegal base64, targetUrl: " + targetUrl);
				if (isThrowing) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
					throw TsmpDpAaRtnCode._1297.throwing("targetUrl");
				}
				return null;
			}

			totalProbability += probability;

			// (4).將 機率 和 URL 值放入 LinkedList 物件
			dataArr[0] = probability + "";
			dataArr[1] = targetUrl;
			srcUrlDataList.add(dataArr);
		}

		// (5).所有機率加總必須為 100，否則 return null
		if (totalProbability != 100) {
			TPILogger.tl.debug("The total number of chances must be 100, but it is " + totalProbability);
			if (isThrowing) {
				throw TsmpDpAaRtnCode._1528.throwing(totalProbability + "");
			}
			return null;
		}
 
		return srcUrlDataList;
	}

	/** 
	 * 取得要打的目標URL順序清單,依機率演算法排序 <br> 
	 * 1.若只有一個，即為要打的URL <br> 
	 * 2.若有多個，依URL的機率計算(機率演算法)，取得要打的URL順序清單 <br> 
	 */
	public List<String> getSortSrcUrlListForLB(String srcUrl) {
		boolean isThrowing = false;// 是否要丟出例外
		LinkedList<String[]> dataList = (LinkedList<String[]>) getSrcUrlToTargetUrlAndProbabilityList(srcUrl, isThrowing);
		if (CollectionUtils.isEmpty(dataList)) {
			return null;
		}

		List<String> srcUrlList = new ArrayList<>();// 排過順序的URL
		// 只有一筆資料，即要打的URL
		if (dataList.size() == 1) {
			String[] arr = dataList.get(0);
			srcUrlList.add(arr[1]);
			return srcUrlList;
		}

		// 依各個URL的機率計算，取得依序要打的URL清單
		String firstURL = ProbabilityAlgUtils.getProbabilityAns(dataList);
		List<String[]> dataList01 = ProbabilityAlgUtils.getAllUrlByProb(dataList, firstURL);
		for (String[] arr : dataList01) {
			srcUrlList.add(arr[1]);
		}

		return srcUrlList;
	}
	
	/**
	 * 取得最終要打的目標URL <br> 
	 * 1.若只有一個，即為要打的URL; <br> 
	 * 2.若有多個，必須依URL的機率計算(機率演算法)，取得最終要打的URL <br> 
	 */
	public String getSrcUrlForLB(String srcUrl) {
		boolean isThrowing = false;// 是否要丟出例外
		LinkedList<String[]> dataList = (LinkedList<String[]>) getSrcUrlToTargetUrlAndProbabilityList(srcUrl,
				isThrowing);
		if (CollectionUtils.isEmpty(dataList)) {
			return null;
		}

		// 只有一筆資料，即要打的URL
		if (dataList.size() == 1) {
			String[] arr = dataList.get(0);
			return arr[1];
		}

		// 依各個URL的機率計算，取得要打的URL
		return ProbabilityAlgUtils.getProbabilityAns(dataList);
	}

	public Boolean isURLRID(String moduleName, String apiKey) throws Exception {
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(tsmpApiId);
		if (!tsmpApi.isPresent()) {
			throw new Exception("TSMP_API not found, api_key:" + apiKey + "\t,module_name:" + moduleName);
		} else {

			Optional<TsmpApiReg> tsmpApiReg = getTsmpApiRegCacheProxy().findById(tsmpApiRegId);
			if (tsmpApiReg.isPresent()) {
				String UrlRid = tsmpApiReg.get().getUrlRid();
                return "1".equals(UrlRid);
			}

		}
		return false;
	}

	public TsmpApi getTsmpApi(HttpServletRequest httpReq) throws Exception {
		String moduleName = getAttribute(httpReq, GatewayFilter.MODULE_NAME);
		String apiId = getAttribute(httpReq, GatewayFilter.API_ID);

		TsmpApi tsmpApi = null;
		TsmpApiId tsmpApiId = new TsmpApiId(apiId, moduleName);
		Optional<TsmpApi> optTsmpApi = getTsmpApiCacheProxy().findById(tsmpApiId);
		if (!optTsmpApi.isPresent()) {
			throw new Exception("TSMP_API not found, api_key:" + apiId + "\t,module_name:" + moduleName);
		} else {
			tsmpApi = optTsmpApi.get();
		}
		return tsmpApi;
	}

	public TsmpApiReg getTsmpApiReg(HttpServletRequest httpReq) throws Exception {
		String moduleName = getAttribute(httpReq, GatewayFilter.MODULE_NAME);
		String apiId = getAttribute(httpReq, GatewayFilter.API_ID);

		TsmpApiReg tsmpApiReg = null;
		TsmpApiRegId apiRegId = new TsmpApiRegId(apiId, moduleName);
		Optional<TsmpApiReg> optApiReg = getTsmpApiRegCacheProxy().findById(apiRegId);
		if (!optApiReg.isPresent()) {
			throw new Exception("TSMP_API_REG not found, api_key:" + apiId + "\t,module_name:" + moduleName);
		} else {
			tsmpApiReg = optApiReg.get();
		}
		return tsmpApiReg;
	}

	/**
	 * 所有的 TSMPC / DGRC Service 改用這個 method, verifyData(HttpServletResponse httpRes,
	 * HttpServletRequest httpReq, HttpHeaders httpHeaders, TsmpApiReg apiReg)
	 */
	public ResponseEntity<?> verifyData(HttpServletResponse httpRes, HttpServletRequest httpReq,
			HttpHeaders httpHeaders, TsmpApiReg apiReg) throws Exception {
		return verifyData(httpRes, httpReq, httpHeaders, apiReg, null, false);
	}

	public ResponseEntity<?> verifyData(HttpServletResponse httpRes, HttpServletRequest httpReq,
			HttpHeaders httpHeaders, TsmpApiReg apiReg, String payload, boolean isGet) throws Exception {

		// 說明 dgr-v4 流程使用, 沒有注入就不會引用

		// 檢查是否要驗 Authorization (No Auth 機制)
		String noAuth = apiReg.getNoOauth();
		httpReq.setAttribute(TokenHelper.CLIENT_ID, "");
		httpReq.setAttribute(TokenHelper.NO_AUTH, noAuth);

		if ("1".equals(noAuth)) {
			// 不用驗證打 API 的 authorization
			TPILogger.tl.debug("No Auth Flag: " + "1(true)");// 不用驗證才印出

		} else {
			// 驗證打 API 的 authorization 或 x-api-key
			String auth = httpHeaders.getFirst(AUTHORIZATION);
			String xApiKey = httpHeaders.getFirst("x-api-key");
			
			if (StringUtils.hasText(auth)) {
				// 驗證打 API 的 "Authorization"
				ResponseEntity<?> respEntity = getTokenHelper().verifyApiForAuth(auth, httpReq, payload);
				if (respEntity != null) {// 資料驗證有錯誤
					return respEntity;
				}

			} else if (StringUtils.hasText(xApiKey)) {
				// 驗證打 API 的 "x-api-key"
				ResponseEntity<?> respEntity = getTokenHelper().verifyApiForXApiKey(xApiKey, httpReq);
				if (respEntity != null) {// 資料驗證有錯誤
					return respEntity;
				}
				
			} else {
				// 沒有傳入 "Authorization" 和 "x-api-key"
				if (!StringUtils.hasText(auth) && !StringUtils.hasText(xApiKey)) {
					String reqUri = httpReq.getRequestURI();

					String errMsg = TokenHelper.AUTHORIZATION_AND_X_API_KEY_HAVE_NO_VALUES;
					TPILogger.tl.debug(errMsg);

					errMsg = TokenHelper.UNAUTHORIZED;
					TPILogger.tl.debug(errMsg);
					return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
				}
			}
		}

		// 從 request 中取得 client id
		String clientId = (String) httpReq.getAttribute(TokenHelper.CLIENT_ID);
		Integer priority = 0; // 查不到 client id 時的預設優先值。
		if (StringUtils.hasText(clientId)) {
			// 因為verifyApi有用findFirstByClientId的cache機制,所以沒用findById
			TsmpClient client = getTsmpClientCacheProxy().findFirstByClientId(clientId);
			if (client != null) {
				priority = client.getcPriority();
			}
		}

		takeTurnsSleeping(clientId, priority); // 依照 JobQueueSize 與 priority 的數值，決定是否暫停。
		return null;
	}

	public void takeTurnsSleeping(String clientid, Integer priority) throws InterruptedException {

//		initProability_50_50();
		//String sleepStrFlag = ProbabilityAlgUtils.getProbabilityAns(probMap5050);
				
		int jobQueueSizeThreshold = 300;
		// 0:high / 5:default / 9:low
		// 在 JobQueueSize > 300 的時候，中優先度與低優先度的請求皆停止100毫秒。
		if (jobHelper.getJobQueueSize(1) > jobQueueSizeThreshold && priority > 4 && RandomUtil.getIntRandom(100) < 20) {
			// 取代原有的 ProbabilityAlgUtils.getProbabilityAns(probMap5050);
			// 隨機數判斷 - 產生 0-99 的數字,小於 20 代表 20% 的機率,
			// 要改成 30% 的機率,就改成 nextInt(100) < 30
			Thread.sleep(100);
		}

		// 在 JobQueueSize > 300 的時候，低優先度的請求皆停止100毫秒。
		if (jobHelper.getJobQueueSize(1) > jobQueueSizeThreshold && priority > 5) {
			Thread.sleep(100);
		}
	}
	
	//機率表轉換為 Map, 執行緒安全的初始化 (高併發優化版)
//	private static final Map<Integer, String> probMap5050 = initProability_50_50_v2();
	
//	private static Map<Integer, String> initProability_50_50_v2() {
//	    Map<Integer, String> map = new HashMap<>(100);
//	    for (int i = 0; i < 50; i++) {
//	        map.put(i, "true");
//	    }
//	    for (int i = 50; i < 100; i++) {
//	        map.put(i, "false");
//	    }
//	    return Collections.unmodifiableMap(map);
//	}


	public Map<String, List<String>> getConvertHeader(HttpServletRequest httpReq, HttpHeaders httpHeaders,
			int tokenPayloadFlag, Boolean cApiKeySwitch, String uuid, String srcUrl) throws Exception {
		List<String> compAddrs = getTsmpSettingService().getVal_TSMP_COMPOSER_ADDRESS();
		String addr = srcUrl;

		/* 
		 * 1. Composer API, 則 Header 要加上 "capi-key"
		 * 若目標路徑有 TSMP_COMPOSER_ADDRESS 的值, 例如: https://10.20.30.88:18440,
		 * dgrc --> Backend(Composer),
		 * 則傳送 capi-key
		 */
		for (String a : compAddrs) { 
			if (addr.contains(a)) {
				cApiKeySwitch = true;
				break;
			}
		}
		
		/*
		 * 2. 註冊的客製包 API, 則 Header 要加上 "capi-key" (expireCApiKey)
		 * 若目標路徑有 "/dgrv4/cus/",
		 * CURL --> dgrc --> Backend(/dgrv4/cus/**),
		 * 則傳送 capi-key(expireCApiKey) + Authorization(當勾選no auth,如實傳送字串),
		 * 例如: SCB 集團打 dgrc, 轉導到註冊的客製包 OneCert API 時,
		 */
		boolean expireCApiKeySwitch = false;
		if (srcUrl.contains("/dgrv4/cus/")) {
			expireCApiKeySwitch = true;
		}
		
		return getConvertHeader(httpReq, httpHeaders, tokenPayloadFlag, cApiKeySwitch, uuid, expireCApiKeySwitch);
	}

	/**
	 * 處理 tokenpayload & backAuth 機制
	 * @throws Exception 
	 */
	public Map<String, List<String>> getConvertHeader(HttpServletRequest httpReq, HttpHeaders httpHeaders,
			int tokenPayloadFlag, Boolean cApiKeySwitch, String uuid, boolean expireCApiKeySwitch) throws Exception {

		String noAuth = (String) httpReq.getAttribute(TokenHelper.NO_AUTH);

		boolean isIdToken = false;
		boolean isBackAuth = false;
		boolean isOrigAuth = false;
		List<String> idTokenValueList = new ArrayList<String>();
		List<String> backAuthValueList = new ArrayList<String>();
		List<String> origAuthValueList = new ArrayList<String>();
		Enumeration<String> httpHeaderKeys = httpReq.getHeaderNames();
		Map<String, List<String>> headers = new HashMap<>();

		// 1.取得原本 header 的值
		while (httpHeaderKeys.hasMoreElements()) {
			String key = httpHeaderKeys.nextElement();

			var headerValueList = httpHeaders.get(key);

			if (headerValueList == null) continue;

			if ("id-token".equalsIgnoreCase(key)) {
				isIdToken = true;
				for (String str : headerValueList) {
					str = "Bearer " + str;// ID Token JWT 前面加上前綴字 "Bearer "
					idTokenValueList.add(str);
				}

			} else if ("backAuth".equalsIgnoreCase(key)) {
				isBackAuth = true;
				backAuthValueList = headerValueList;

			} else if (AUTHORIZATION.equalsIgnoreCase(key)) {
				if ("1".equals(noAuth)) {// 有勾選 No Auth
					isOrigAuth = true;
					origAuthValueList = headerValueList;
				}

				if (tokenPayloadFlag == 1 && !headerValueList.isEmpty()) {// 有勾選 Token Payload
					String authorization = headerValueList.get(0);
					String tokenpayload = getTokenpayload(authorization);
					headers.put("tokenpayload", Arrays.asList(tokenpayload));// 放到 http header 的 "tokenpayload"
				}

			} else {
				if (!"If-Modified-Since".equalsIgnoreCase(key) && !"If-None-Match".equalsIgnoreCase(key)) {
					headers.put(key, headerValueList);
				}
			}
		}

		// 2."authorization"的值,依 No Auth 是否勾選來自以下順序:
		// 有勾選時,優先順序(大到小): "backAuth" -> "authorization"
		// 沒有勾選時,優先順序(大到小): "id-token" -> "backAuth"
		if ("1".equals(noAuth)) {// 有勾選 No Auth
			// 若 header 中有 "authorization", 將值放入或取代
			if (isOrigAuth) {
				headers.put(AUTHORIZATION, origAuthValueList);
			}

			// 若 header 中有 "backAuth", 將值放入或取代
			if (isBackAuth) {
				headers.put(AUTHORIZATION, backAuthValueList);
			}

		} else {// 沒有勾選 No Auth
			// 若 header 中有 "backAuth", 將值放入或取代
			if (isBackAuth) {
				headers.put(AUTHORIZATION, backAuthValueList);
			}

			// 若 header 中有 "id-token", 將值放入或取代
			if (isIdToken) {
				headers.put(AUTHORIZATION, idTokenValueList);
			}
		}

		// 3.確認是否需要cApiKey
		if (cApiKeySwitch) {
			String uuidForCapiKey = UUID.randomUUID().toString();
			String cuuid = uuidForCapiKey.toUpperCase();
			String capikey = CApiKeyUtils.signCKey(cuuid);
			headers.put("cuuid", Arrays.asList(cuuid));
			headers.put("capi-key", Arrays.asList(capikey));
			headers.put("uuid", Arrays.asList(uuid));
		}

		// 4.確認是否需要expireCApiKey
		if (expireCApiKeySwitch) {
			// 產生 capi-key string
			String jws = ExpireKeyUtil.getExpireKey_60sec();
			headers.put("capi-key", Arrays.asList(jws));
		}

		return headers;
	}

	/**
	 * 將tsmpc路徑後的參數回傳。
	 *
	 * @param reqUrl
	 * @return
	 */
	public String getTsmpcPathParameter(String reqUrl) {
		// 這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
		String regStr = "\\/{0,1}tsmpc\\/.*?\\/.*?\\/(.*)";
		Pattern pattern = Pattern.compile(regStr); // NOSONAR
		Matcher matcher = pattern.matcher(reqUrl);

		if (matcher.find()) {
			return "/" + matcher.group(1);
		}

		return "";
	}

	public HttpServletResponse getConvertResponse(HttpRespData respObj, HttpServletResponse httpRes) {
		// 此方法有更改,也要修改getConvertResponse(Map<String, List<String>> respHeader, int
		// status, HttpServletResponse httpRes)
		if(respObj.respHeader == null) {
			return httpRes;
		}
		httpRes.setStatus(respObj.statusCode);
		respObj.respHeader.remove("Transfer-Encoding"); // 它不能回傳
		respObj.respHeader.remove("Content-Length"); // 需自動計算
		//大寫
		respObj.respHeader.remove("Access-Control-Allow-Origin"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respObj.respHeader.remove("Access-Control-Allow-Headers"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respObj.respHeader.remove("Access-Control-Allow-Methods"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除

		//小寫
		respObj.respHeader.remove("access-control-allow-origin"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respObj.respHeader.remove("access-control-allow-headers"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respObj.respHeader.remove("access-control-allow-methods"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除

		respObj.respHeader.remove("Content-Security-Policy");
		respObj.respHeader.remove("X-Frame-Options");
		respObj.respHeader.remove("X-Content-Type-Options");
		respObj.respHeader.remove("Strict-Transport-Security");
		respObj.respHeader.remove("Referrer-Policy");
		respObj.respHeader.remove("Cache-Control");
		respObj.respHeader.remove("Pragma");

		/*
		 * Content-Security-Policy(CSP,內容安全策略)的值, 例如:
		 * "default-src https://10.20.30.88:18442 https://10.20.30.88:28442 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 * "default-src * 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 */
		String dgrCspVal = getTsmpSettingService().getVal_DGR_CSP_VAL();// 例如: "*" 或 "https://10.20.30.88:18442
																		// https://10.20.30.88:28442"

		httpRes.setHeader("Access-Control-Allow-Origin", getTsmpSettingService().getVal_DGR_CORS_VAL()); // 從 TsmpSetting 取值，動態回傳。
		//httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, SignCode, Language"); // CORS
		httpRes.setHeader("Access-Control-Allow-Headers", corsAllowHeaders); //"3. corsAllowHeaders = " + corsAllowHeaders
		httpRes.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,PATCH,DELETE"); // CORS
		if (StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim()))
			httpRes.setHeader("Content-Security-Policy", dgrCspVal);
		httpRes.setHeader("X-Frame-Options", "sameorigin");
		httpRes.setHeader("X-Content-Type-Options", "nosniff");
		httpRes.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		httpRes.setHeader("Cache-Control", "no-cache");
		httpRes.setHeader("Pragma", "no-cache");
		httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        

		// in the phase don't do this, then next phase will do copy RESPONSE header
//		respObj.respHeader.forEach((k, vs)->{
//			vs.forEach((v)->{
//				httpRes.addHeader(k, v);
//			});
//		});

		return httpRes;
	}

	public HttpServletResponse getConvertResponse(Map<String, List<String>> respHeader, int status,
			HttpServletResponse httpRes) {
//		{
//			System.out.println(TPILogInfo.getLineNumber2());
//			List<String> headerName = httpRes.getHeaderNames().stream().collect(Collectors.toList());
//			for (String k : headerName) {
//				Collection<String> valueList = httpRes.getHeaders(k);
//				String value = this.convertAuth(k, valueList.toString());
//				System.out.println("\tKey: " + k + ", Value: " + value);
//			}
//		}

		// 清空 retry 之前的 header
		try {
			httpRes.reset(); //code, headers
		} catch (Exception e) {
			TPILogger.tl.warn("Response already commited HTTP code : " + httpRes.getStatus());
			if (httpRes.getStatus() != 200) {
				TPILogger.tl.warn(httpRes.getStatus() +", is commited:" + httpRes.isCommitted() + "|" + StackTraceUtil.logStackTrace(e));
			}
			return null;
		}


		httpRes.setStatus(status);
		respHeader.remove("Transfer-Encoding"); // 它不能回傳
		respHeader.remove("Content-Length"); // 需自動計算
		//大寫
		respHeader.remove("Access-Control-Allow-Origin"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respHeader.remove("Access-Control-Allow-Headers"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respHeader.remove("Access-Control-Allow-Methods"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		//小寫
		respHeader.remove("access-control-allow-origin"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respHeader.remove("access-control-allow-headers"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除
		respHeader.remove("access-control-allow-methods"); // 經過 APIM 一定要能解決 CORS, 所以原來的 API header 要移除

		respHeader.remove("Content-Security-Policy");
		respHeader.remove("X-Frame-Options");
		respHeader.remove("X-Content-Type-Options");
		respHeader.remove("Strict-Transport-Security");
		respHeader.remove("Referrer-Policy");
		respHeader.remove("Cache-Control");
		respHeader.remove("Pragma");

		/*
		 * Content-Security-Policy(CSP,內容安全策略)的值, 例如:
		 * "default-src https://10.20.30.88:18442 https://10.20.30.88:28442 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 * "default-src * 'self' 'unsafe-inline'; img-src https://* 'self' data:;"
		 */
		String dgrCspVal = getTsmpSettingService().getVal_DGR_CSP_VAL();// 例如: "*" 或 "https://10.20.30.88:18442
																		// https://10.20.30.88:28442"

		httpRes.setHeader("Access-Control-Allow-Origin", getTsmpSettingService().getVal_DGR_CORS_VAL()); // 從 TsmpSetting 取值，動態回傳。
		//httpRes.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, SignCode, Language"); // CORS
		httpRes.setHeader("Access-Control-Allow-Headers", corsAllowHeaders); //"4. corsAllowHeaders = " + corsAllowHeaders

		httpRes.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,PATCH,DELETE"); // CORS

		if (StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim()))
			httpRes.setHeader("Content-Security-Policy", dgrCspVal);
		httpRes.setHeader("X-Frame-Options", "sameorigin");
		httpRes.setHeader("X-Content-Type-Options", "nosniff");
		httpRes.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		httpRes.setHeader("Cache-Control", "no-cache");
		httpRes.setHeader("Pragma", "no-cache");
		httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        

		respHeader.forEach((k, vs) -> {
			vs.forEach((v) -> {
				// John 2024.10.16 , 和雲反應 "Header name was null"
				if (k!=null && v!=null) {
					httpRes.setHeader(k, v); // k, v maybe 可能為 null , "Header name was null"
				} else {
					if (k == null) {
						TPILogger.tl.warn("[Header name] is null... \n\n");
					}
					if (v == null) {
						TPILogger.tl.warn("[Header value] is null, Header name:'"+ k +"'\n\n");
					}
				}
			});
		});
//		{
//			System.out.println(TPILogInfo.getLineNumber2());
//			List<String> headerName = httpRes.getHeaderNames().stream().collect(Collectors.toList());
//			for (String k : headerName) {
//				Collection<String> valueList = httpRes.getHeaders(k);
//				String value = this.convertAuth(k, valueList.toString());
//				System.out.println("\tKey: " + k + ", Value: " + value);
//			}
//		}
		return httpRes;
	}

	/**
	 * DGRC 用, 將 http status code 放到 header, <br>
	 * 記錄 response 的時間 & http status code
	 */
	private void doFetchUriHistoryAfter(HttpServletRequest httpReq, int httpCode) {
		if(httpReq != null) {
			httpReq.setAttribute(GatewayFilter.HTTP_CODE, httpCode);
			
			// 記錄 response 的時間 & http status code
			GatewayFilter.fetchUriHistoryAfter(httpReq);
		}
	}
	
	/**
	 * 取得調用API時,驗證token失敗的log
	 */
	public StringBuffer getLogResp(ResponseEntity<?> respEntity) {
		return getLogResp(respEntity, null);
	}

	/**
	 * 授權錯誤 or 沒有目標URL時, 印出訊息 <br> 
	 * 註: 僅 DGRC, TSMPC 呼叫
	 */
	public StringBuffer getLogResp(ResponseEntity<?> respEntity, Map<String, String> maskInfo,
			HttpServletRequest httpReq) {
		int httpCode = respEntity.getStatusCode().value();
		doFetchUriHistoryAfter(httpReq, httpCode);

		return getLogResp(respEntity, maskInfo);
	}

	/**
	 * 授權錯誤 or 沒有目標URL時, 印出訊息 <br> 
	 * 註: 僅 TSMPC 呼叫, 20250407 tsmpc no call
	 */
	public StringBuffer getLogResp(ResponseEntity<?> respEntity, Map<String, String> maskInfo) {
		StringBuffer log = new StringBuffer();
		// print header
		writeLogger(log, "--【Http Resp Header】--");

		respEntity.getHeaders().forEach((k, vlist) -> {
			vlist.forEach((v) -> {
				String value = this.convertAuth(k, v, maskInfo);
				writeLogger(log, "\tKey: " + k + ", Value: " + value);
			});
		});

		// 以下資料尚未生成
//		writeLogger(log, "\tKey: " + "content-Length" + ", Value: " + respEntity.getHeaders().getContentLength());
//		writeLogger(log, "\tKey: " + "getContentType" + ", Value: " + respEntity.getHeaders().getContentType());
//		writeLogger(log, "\tKey: " + "getLocale" + ", Value: " + respEntity.getHeaders().getContentLanguage());

		writeLogger(log, "--【End】--\r\n");

		// print http code
		writeLogger(log, "--【Http status code】--");
		writeLogger(log, "" + respEntity.getStatusCode().toString());
		writeLogger(log, "--【End】--\r\n");

		// print body
		writeLogger(log, "--【Resp payload / Form Data】");
		// Object to JSON
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(respEntity.getBody());
			writeLogger(log, json);
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		writeLogger(log, "--【End】--\r\n");
		return log;
	}

	/**
	 * 註: 僅 TSMPC 由此直接呼叫,20250407 tsmpc no call
	 */
	public StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int content_Length,
			Map<String, String> maskInfo) throws IOException {
		return getLogResp(httpRes, httpRespStr, content_Length, null, maskInfo);
	}
	
	/**
	 * 註: 僅 DGRC, TSMPC, Mock API Test 由此直接呼叫
	 */
	public StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int contentLength,
			Map<String, String> maskInfo, HttpServletRequest httpReq) throws IOException {
		int httpCode = -1;
		if (httpRes != null) {
			//記錄 response 的時間 & http status code
			httpCode = httpRes.getStatus();
			doFetchUriHistoryAfter(httpReq, httpCode);
		}

		return getLogResp(httpRes, httpRespStr, contentLength, null, maskInfo);
	}
	/**
	 * 取得調用目標URL 的 response log
	 * 註: 僅 TSMPCServicePostForm 由此直接呼叫
	 */
	public StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int contentLength,
			StringBuffer cfp_log, Map<String, String> maskInfo, HttpServletRequest httpReq) throws IOException {
		int httpCode = -1;
		if (httpRes != null) {
			//記錄 response 的時間 & http status code
			httpCode = httpRes.getStatus();
			doFetchUriHistoryAfter(httpReq, httpCode);
		}

		return getLogResp(httpRes, httpRespStr, contentLength, cfp_log, maskInfo);
		
	}
	/**
	 * 取得調用目標URL 的 response log
	 * 註: 僅 TSMPCServicePostForm 由此直接呼叫, 20250407 TSMPCServicePostForm no call
	 */
	public StringBuffer getLogResp(HttpServletResponse httpRes, String httpRespStr, int contentLength,
			StringBuffer cfp_log, Map<String, String> maskInfo) throws IOException {
		if (cfp_log == null) {
			cfp_log = new StringBuffer();
		}

		// print header
		writeLogger(cfp_log, "--【Http Resp Header】--");

//		List<String> headerName = httpRes.getHeaderNames().stream().distinct().collect(Collectors.toList());//移除重複的 HeaderName
		List<String> headerName = httpRes.getHeaderNames().stream().collect(Collectors.toList());
		for (String k : headerName) {
			Collection<String> valueList = httpRes.getHeaders(k);
			String tmpValue = valueList.toString();
			//[ ] 符號總是位於 String 的第一個和最後一個字符，則可以使用 substring() 方法更有效地去除它們。
			tmpValue = tmpValue.substring(1, tmpValue.length() - 1);
			String value = this.convertAuth(k, tmpValue, maskInfo);
			writeLogger(cfp_log, "\tKey: " + k + ", Value: " + value);
		}
		writeLogger(cfp_log, "\tKey: " + "getStatus" + ", Value: " + httpRes.getStatus());
		writeLogger(cfp_log, "\tKey: " + "Content-Length" + ", Value: " + contentLength);
		writeLogger(cfp_log, "\tKey: " + "getCharacterEncoding" + ", Value: " + httpRes.getCharacterEncoding());
		writeLogger(cfp_log, "\tKey: " + "getContentType" + ", Value: " + httpRes.getContentType());
		writeLogger(cfp_log, "\tKey: " + "getLocale" + ", Value: " + httpRes.getLocale());

		writeLogger(cfp_log, "--【End】--\r\n");

		// print http code
		writeLogger(cfp_log, "--【Http status code】--");
		writeLogger(cfp_log, "" + httpRes.getStatus());
		writeLogger(cfp_log, "--【End】--\r\n");

		// print body
		writeLogger(cfp_log, "--【Resp payload / Form Data】");
		writeLogger(cfp_log, httpRespStr);
		writeLogger(cfp_log, "--【End】--\r\n");
		return cfp_log;
	}

	private void writeLogger(StringBuffer log, String msg) {
		msg += "\n";
		log.append("\n" + msg);
	}

	public String maskHeader(Map<String, String> maskInfo, String key, String header) {
		if (maskInfo != null) {
			String headerMaskPolicy = maskInfo.get("headerMaskPolicy");
			String headerMaskPolicySymbol = maskInfo.get("headerMaskPolicySymbol");
			String headerMaskKey = maskInfo.get("headerMaskKey");
			if (StringUtils.hasLength(headerMaskKey)) {

				String[] headerMaskKeyArr = headerMaskKey.split(",");

				int headerMaskPolicyNum = Integer.parseInt(maskInfo.get("headerMaskPolicyNum"));
				for (String headerKey : headerMaskKeyArr) {
					if (StringUtils.hasLength(key) && key.equalsIgnoreCase(headerKey)) {
						if (headerMaskPolicy.equals("1")) {
							if (header.length() > (headerMaskPolicyNum * 2)) {
								header = header.substring(0, headerMaskPolicyNum) + headerMaskPolicySymbol
										+ header.substring(header.length() - headerMaskPolicyNum);
							}
						}

						if (headerMaskPolicy.equals("2")) {
							if (header.length() > headerMaskPolicyNum) {
								header = header.substring(0, headerMaskPolicyNum) + headerMaskPolicySymbol;
							}
						}

						if (headerMaskPolicy.equals("3")) {
							if (header.length() > headerMaskPolicyNum) {
								header = headerMaskPolicySymbol + header.substring(headerMaskPolicyNum);
							}
						}
					}
				}
			}
		}
		return header;
	}

	public String maskBodyFromFormData(Map<String, String> maskInfo, String key, String value) {
		if (maskInfo != null) {

			String bodyMaskPolicySymbol = maskInfo.get("bodyMaskPolicySymbol");
			String bodyMaskKeyword = maskInfo.get("bodyMaskKeyword");
			if (StringUtils.hasLength(bodyMaskKeyword)) {
				String[] bodyMaskKeywordArr = bodyMaskKeyword.split(",");
				for (String keyword : bodyMaskKeywordArr) {
					if (key.equals(keyword)) {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < value.length(); i++) {
							sb.append(bodyMaskPolicySymbol);
						}
						return sb.toString();
					}
				}
			}
		}
		return value;
	}

	public String maskBody(Map<String, String> maskInfo, String mbody) {
		//checkmarx, Unchecked Input for Loop Condition
		return CheckmarxCommUtils.sanitizeForCheckmarx(maskInfo, mbody);
	}

	public Map<String, List<String>> getEsHeaderMap(HttpServletRequest request) {
		Map<String, List<String>> esHeaderMap = new HashMap<>();
		Enumeration<String> headerNameEnum = request.getHeaderNames();
		while (headerNameEnum.hasMoreElements()) {
			String headerName = headerNameEnum.nextElement();
			if (headerName == null) {
				continue;
			}
			List<String> headerList = Collections.list(request.getHeaders(headerName));
			if (CommForwardProcService.MASK_TOKEN_LIST.contains(headerName)) {
				headerList = this.convertAuth(headerList);
			}
			esHeaderMap.put(headerName, headerList);
		}
		return esHeaderMap;
	}

	public TsmpApiLogReq addEsTsmpApiLogReq1(String uuid, HttpServletRequest httpReq, String mbody, String entry,
			String aType) throws Exception {
		Map<String, String> logParams = new HashMap<>();
		logParams.put("url", httpReq.getRequestURI());
		logParams.put("queryString", httpReq.getQueryString());
		// 找不到 module 就改用 uri
		logParams.put(MODULE_NAME, getAttribute(httpReq, GatewayFilter.MODULE_NAME));
		logParams.put("txid", getAttribute(httpReq, GatewayFilter.API_ID));
		logParams.put("httpMethod", httpReq.getMethod());
		logParams.put("orgId", httpReq.getAttribute(TokenHelper.ORG_ID) == null ? ""
				: ((String) httpReq.getAttribute(TokenHelper.ORG_ID)));
		logParams.put("jti",
				httpReq.getAttribute(TokenHelper.JTI) == null ? "" : ((String) httpReq.getAttribute(TokenHelper.JTI)));
		logParams.put("clientId", httpReq.getAttribute(TokenHelper.CLIENT_ID) == null ? ""
				: ((String) httpReq.getAttribute(TokenHelper.CLIENT_ID)));
		logParams.put("cip",
				StringUtils.hasText(httpReq.getHeader("x-forwarded-for")) ? httpReq.getHeader("x-forwarded-for")
						: httpReq.getRemoteAddr());
		logParams.put("contentLength", httpReq.getHeader(HttpHeaders.CONTENT_LENGTH));

		Map<String, List<String>> esHeaderMap = getEsHeaderMap(httpReq);
		return addEsTsmpApiLogReq1(uuid, logParams, esHeaderMap, mbody, entry, aType);
	}

	// 可避免 找不到 moduleName 的問題
	private String getAttribute(HttpServletRequest httpReq, String name) {
		Object value = httpReq.getAttribute(name);
		if (value == null) value = httpReq.getRequestURI();
		return value.toString();
	}
	
	private static boolean esDisable = false;			// Setting Flag
	private static boolean checkStatusFromFile = false; // diskSpaceMonitor 檢查器
	private static final AtomicLong ES_API_LOG_LAST_LOG_TIME = new AtomicLong(0);
	private static final long ES_API_LOG_LOG_INTERVAL = 60000; // 例如每分鐘最多記錄一次
	
	public TsmpApiLogReq addEsTsmpApiLogReq1(String uuid, Map<String, String> logParams, //
			Map<String, List<String>> esHeaderMap, String mbody, String entry, String aType) throws Exception {

		String url = logParams.get("url");
		String moduleName = logParams.get(MODULE_NAME);
		String txid = logParams.get("txid");
		String httpMethod = logParams.get("httpMethod");

		TsmpApiLogReq reqVo = new TsmpApiLogReq();
		reqVo.setModuleName(moduleName);
		reqVo.setTxid(txid);
		reqVo.setHttpMethod(httpMethod);
        
		// 是否禁止紀錄ES的LOG
		esDisable = getTsmpSettingService().getVal_ES_LOG_DISABLE();
		checkStatusFromFile = DiskSpaceMonitor.checkStatusFromFile();
		if (esDisable || checkStatusFromFile) {
		    long currentTime = System.currentTimeMillis();
		    long lastLogTime = ES_API_LOG_LAST_LOG_TIME.get();
		    
		    // 如果距離上次日誌已經過了指定時間，則輸出日誌
		    if (currentTime - lastLogTime > ES_API_LOG_LOG_INTERVAL && ES_API_LOG_LAST_LOG_TIME.compareAndSet(lastLogTime, currentTime)) {
		        TPILogger.tl.warn(String.format("ES_LOG_DISABLE is [%b] or DiskSpaceMonitor.checkStatusFromFile() is [%b]",
		            esDisable, checkStatusFromFile));
		    }
			
			reqVo.setIgnore(esDisable || checkStatusFromFile); // true 略過不寫 ES
			return reqVo;
		}

		// api是否不寫入ES
		boolean isIgnoreApi = this.checkIgnoreApi(entry, moduleName + "/" + txid, url);
		if (isIgnoreApi) {
			TPILogger.tl.info(entry + " " + moduleName + "/" + txid + " or " + url + " is es ignore api");
			reqVo.setIgnore(isIgnoreApi);
			return reqVo;
		}

		// 檢查是否有ES_ID_PWD參數
		if (!StringUtils.hasText(getTsmpSettingService().getVal_ES_ID_PWD())) {
			TPILogger.tl.warn("tsmp_setting ES_ID_PWD no value");
			reqVo.setIgnore(true);
			return reqVo;
		}

		// 檢查是否有ES_URL參數
		if (!StringUtils.hasText(getTsmpSettingService().getVal_ES_URL())) {
			TPILogger.tl.warn("tsmp_setting ES_URL no value");
			reqVo.setIgnore(true);
			return reqVo;
		}

		// 檢查ES_ID_PWD參數內容值個數和URL個數有沒有吻合
		String[] arrEsUrl = getTsmpSettingService().getVal_ES_URL().split(",");
		String[] arrIdPwd = getTsmpSettingService().getVal_ES_ID_PWD().split(",");
		if (arrIdPwd.length != arrEsUrl.length) {
			TPILogger.tl.warn("ES connection info and id pwd is not mapping");
			reqVo.setIgnore(true);
			return reqVo;
		}

		// 測試連線
//		int index = 0;
//		boolean isConnection = false;
//		for(;index < arrEsUrl.length; index++) {
//			isConnection = this.checkConnection(arrEsUrl[index]);
//			if(isConnection) {
//				break;
//			}
//		}
//		if(!isConnection) {
//			TPILogger.tl.error("es connection is fail");
//			reqVo.setIgnore(true);
//			return reqVo;
//		}

		// 解析資料
		String node = "N/A";
		try {
			//防止KeeperServer斷線造成錯誤,所以try..catch
			node = TPILogger.lc.param.get(TPILogger.nodeInfo);
		}catch(Exception e) {
			try {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}catch(Exception ex) {}
		}
		String orgId = logParams.get("orgId");
		String jti = logParams.get("jti");
		String cid = logParams.get("clientId");

		String mtype = "1";
		Long elapse = 0L;
		String id = uuid;
		String queryString = logParams.get("queryString");
		if (StringUtils.hasText(queryString)) {
			url += "?" + queryString;
		}
		String type = "1";
		Date nowDate = DateTimeUtil.now();
		String ts = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日T時分秒時區).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String originServerRequestTime = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日T時分秒時區).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String cip = logParams.get("cip");
		Integer httpStatus = 0;

		// mbody是否遮罩用
		reqVo.setDgrcUri(logParams.get("url"));
		reqVo.setEntry(entry);

		// String mbody = new String(httpReq.getInputStream().readAllBytes(),
		// StandardCharsets.UTF_8);
		Long createTimestamp = nowDate.getTime();
		// mbody遮罩
		String contentLength = logParams.get("contentLength");
		mbody = this.convertMbody(mbody, reqVo, contentLength);

		reqVo.setaType(aType);
		reqVo.setCid(cid);
		reqVo.setCip(cip);
		reqVo.setElapse(elapse);
		reqVo.setHeaders(esHeaderMap);
		reqVo.setHttpStatus(httpStatus);
		reqVo.setId(id);
		reqVo.setJti(jti);
		reqVo.setMbody(mbody);
		reqVo.setMtype(mtype);
		reqVo.setNode(node);
		reqVo.setOrgId(orgId);
		reqVo.setOriginServerRequestTime(originServerRequestTime);
		reqVo.setTs(ts);
		reqVo.setType(type);
		reqVo.setUrl(url);
		reqVo.setCreateTimestamp(createTimestamp);

		// index是否存在,不存在就建立
		String indexName = "tsmp_api_log_" + DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日_4).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		// if(!existIndex(arrEsUrl[index], indexName, arrIdPwd[index])) {
		// createIndex(arrEsUrl[index], indexName, arrIdPwd[index]);
		// }

		//checkmarx, Excessive Data Exposure
		String strJson = CheckmarxUtils.sanitizeForCheckmarx(objectMapper, reqVo) ;
		
//		String esReqUrl = arrEsUrl[index] + indexName + "/_doc";

		// call es add data
//		Map<String, String> header = new HashMap<>();
//		header.put("Accept", "application/json");
//		header.put("Content-Type", "application/json");
//		header.put("Authorization", "Basic " + arrIdPwd[index]);

		// asynchronous call
		int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
//		String esReqUrl = DgrApiLog2ESQueue.getEsReqUrl(arrEsUrl, arrIdPwd, indexName, timeout);
		String esReqUrl = arrEsUrl[0] + indexName + "/_bulk"; // 上面那一行會造成 gateway 的 bottleneck
		DgrApiLog2ESQueue.put(new DgrApiLog2ESQueue(arrEsUrl, arrIdPwd, timeout, indexName, strJson, esReqUrl, id + "-" + type)
						, TPILogger.tl.diskFreeThreshHold
						, TPILogger.tl.deletePercent
						, TPILogger.tl.allowWriteElastic);
//		this.execute(() -> {
//			try {
//				HttpRespData resp = HttpUtil.httpReqByRawData(esReqUrl, HttpMethod.POST.toString(), strJson, header, false);
//				//TPILogger.tl.debug(resp.getLogStr());
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});

		// 傳遞用
		long startTime = System.currentTimeMillis();
		reqVo.setStartTime(startTime);
		reqVo.setEsUrl(esReqUrl);
		//以防workIndex 超過陣列數
		try {
			if (DgrApiLog2ESQueue.workIndex > -1)
				reqVo.setEsIdPwd(arrIdPwd[DgrApiLog2ESQueue.workIndex]);
		} catch (Exception e) {
			reqVo.setEsIdPwd(arrIdPwd[0]);
		}


		return reqVo;
	}

//	public boolean checkConnection_old(String strUrl) {
//		try (Socket socket = new Socket()) {
//		int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
//		URL url = new URL(strUrl);
//		int port = url.getPort()==-1?url.getDefaultPort():url.getPort();
//		socket.connect(new InetSocketAddress(url.getHost(), port), timeout);
//			return true;
//		} catch (Exception e) {
//			TPILogger.tl.error("Url = " + strUrl + "\n" + StackTraceUtil.logStackTrace(e));
//			return false;
//		}
//	}
	
	public boolean checkConnection(String strUrl) {
	    try {
	    	// 未來有需要再改
	    	// int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
	        return HttpsConnectionChecker.checkConnection(strUrl);
	    } catch (Exception e) {
	        TPILogger.tl.error("Connection failed! Url = " + strUrl + "\n" + StackTraceUtil.logStackTrace(e));
	        return false;
	    }
	}

	public boolean checkIgnoreApi(String entry, String api, String uri) {
		boolean isIgnore = false;
		if ("tsmpc".equals(entry)) {
			String ignoreString = getTsmpSettingService().getVal_ES_IGNORE_API();
			return isIgnore(ignoreString, api);
		} else if ("dgrc".equals(entry)) {
			String ignoreString = getTsmpSettingService().getVal_ES_DGRC_IGNORE_URI();
			return isIgnore(ignoreString, uri);
		}
		return isIgnore;
	}

	private boolean isIgnore(String ignoreString, String srcUrl) {
		if (StringUtils.hasText(ignoreString)) {
			String[] ignoreList = ignoreString.split(",");
			for (String strApi : ignoreList) {
				if (strApi.equals(srcUrl)) {
					return true;
				}
			}
		}
		return false;
	}

	public TsmpApiLogReq addEsTsmpApiLogReq2(TsmpApiLogReq reqVo, Map<String, List<String>> headerMap, String reqUrl,
			String mbody) throws Exception {
		if (reqVo.isIgnore()) {
			TsmpApiLogReq reqVo2 = new TsmpApiLogReq();
			reqVo2.setIgnore(true);
			reqVo2.setModuleName(reqVo.getModuleName());
			reqVo2.setTxid(reqVo.getTxid());
			reqVo2.setHttpMethod(reqVo.getHttpMethod());
			return reqVo2;
		}

		String node = reqVo.getNode();
		String orgId = "";
		String jti = "";
		String cid = "";

		String mtype = "";
		Long elapse = 0L;
		String id = reqVo.getId();
		String url = reqUrl;
		String moduleName = reqVo.getModuleName();
		String httpMethod = reqVo.getHttpMethod();
		String type = "2";
		String ts = reqVo.getTs();
		String originServerRequestTime = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日T時分秒時區)
				.orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String cip = CollectionUtils.isEmpty(headerMap.get("x-forwarded-for")) ? reqVo.getCip()
				: headerMap.get("x-forwarded-for").get(0);
		Integer httpStatus = reqVo.getHttpStatus();
		String txid = reqVo.getTxid();
		String aType = reqVo.getaType();
		String entry = reqVo.getEntry();
		Long createTimestamp = reqVo.getCreateTimestamp();

		// mbody是否遮罩用
		TsmpApiLogReq reqVo2 = new TsmpApiLogReq();
		reqVo2.setDgrcUri(reqVo.getDgrcUri());
		reqVo2.setEntry(entry);

		// header
		Map<String, List<String>> esHeaderMap = new HashMap<>(headerMap);

		// mbody遮罩
		List<String> contentLengthList = esHeaderMap.get(HttpHeaders.CONTENT_LENGTH.toLowerCase());
		if (CollectionUtils.isEmpty(contentLengthList)) {
			contentLengthList = esHeaderMap.get(HttpHeaders.CONTENT_LENGTH);
		}
		if (CollectionUtils.isEmpty(contentLengthList)) {
			mbody = this.convertMbody(mbody, reqVo2, "0");
		} else {
			mbody = this.convertMbody(mbody, reqVo2, contentLengthList.get(0));
		}

		// respHeader會有null的KEY,這個也照常辦理
		esHeaderMap.remove(null);

		// 若有MASK_AUTH_LIST,要轉換值
		for (String auth : CommForwardProcService.MASK_TOKEN_LIST) {
			if (esHeaderMap.get(auth) != null) {
				List<String> authList = esHeaderMap.get(auth);
				authList = this.convertAuth(authList);
				esHeaderMap.put(auth, authList);
			}
		}

		reqVo2.setaType(aType);
		reqVo2.setCid(cid);
		reqVo2.setCip(cip);
		reqVo2.setElapse(elapse);
		reqVo2.setHeaders(esHeaderMap);
		reqVo2.setHttpStatus(httpStatus);
		reqVo2.setId(id);
		reqVo2.setJti(jti);
		reqVo2.setMbody(mbody);
		reqVo2.setModuleName(moduleName);
		reqVo2.setHttpMethod(httpMethod);
		reqVo2.setMtype(mtype);
		reqVo2.setNode(node);
		reqVo2.setOrgId(orgId);
		reqVo2.setOriginServerRequestTime(originServerRequestTime);
		reqVo2.setTs(ts);
		reqVo2.setTxid(txid);
		reqVo2.setType(type);
		reqVo2.setUrl(url);
		reqVo2.setCreateTimestamp(createTimestamp);

		//checkmarx, Excessive Data Exposure
		String strJson = CheckmarxUtils.sanitizeForCheckmarx(objectMapper, reqVo2) ;
		String esReqUrl = reqVo.getEsUrl();

		// call es add data
//		Map<String, String> header = new HashMap<>();
//		header.put("Accept", "application/json");
//		header.put("Content-Type", "application/json");
//		header.put("Authorization", "Basic " + reqVo.getEsIdPwd());

		// asynchronous call
		int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
		DgrApiLog2ESQueue.put(new DgrApiLog2ESQueue(timeout, strJson, esReqUrl, id + "-" + type)
				, TPILogger.tl.diskFreeThreshHold
				, TPILogger.tl.deletePercent
				, TPILogger.tl.allowWriteElastic);

//		this.execute(() -> {
//			try {
//				HttpRespData resp = HttpUtil.httpReqByRawData(esReqUrl, HttpMethod.POST.toString(), strJson, header, false);
//				//TPILogger.tl.debug(resp.getLogStr());
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});

		// 傳遞用
		long startTime = System.currentTimeMillis();
		reqVo2.setStartTime(startTime);
		reqVo2.setEsUrl(reqVo.getEsUrl());
		reqVo2.setEsIdPwd(reqVo.getEsIdPwd());
		reqVo2.setDgrcUri(reqVo.getDgrcUri());
		return reqVo2;
	}

	public void addEsTsmpApiLogResp2(HttpRespData respObj, TsmpApiLogReq reqVo, int contentLength) throws Exception {
		addEsTsmpApiLogResp2(respObj, reqVo, null, contentLength);
	}

	public void addEsTsmpApiLogResp2(HttpRespData respObj, TsmpApiLogReq reqVo, String mbody, int contentLength)
			throws Exception {
		if (reqVo.isIgnore()) {
			return;
		}

		String mtype = "";
		String node = reqVo.getNode();
		String orgId = reqVo.getOrgId();
		String jti = reqVo.getJti();
		String cid = reqVo.getCid();
		String ts = reqVo.getTs();
		String id = reqVo.getId();
		String url = reqVo.getUrl();
		String moduleName = reqVo.getModuleName();
		String type = "3";
		String cip = reqVo.getCip();
		String originServerResponseTime = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日T時分秒時區)
				.orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		Integer httpStatus = respObj.statusCode;
		String txid = reqVo.getTxid();
		String aType = reqVo.getaType();
		if (!StringUtils.hasText(mbody)) {
			mbody = respObj.respStr;// 可能回傳是gzip,但有呼叫respObj.fetchByte()就有值
		}
		// mbody遮罩
		mbody = this.convertMbody(mbody, reqVo, String.valueOf(contentLength));

		String entry = reqVo.getEntry();
		Long createTimestamp = reqVo.getCreateTimestamp();

		Map<String, List<String>> esHeaderMap = null;
		if (respObj.respHeader != null) {
			esHeaderMap = new HashMap<>(respObj.respHeader);
		} else {
			esHeaderMap = new HashMap<>();
		}

		// respHeader會有null的KEY
		esHeaderMap.remove(null);

		// 若有MASK_AUTH_LIST,要轉換值
		for (String cfpResp2_auth : CommForwardProcService.MASK_TOKEN_LIST) {
			for (String key : esHeaderMap.keySet()) {
				if (cfpResp2_auth.equalsIgnoreCase(key)) {
					List<String> authList = esHeaderMap.get(key);
					authList = this.convertAuth(authList);
					esHeaderMap.put(key, authList);
					break;
				}
			}
		}

		long elapse = System.currentTimeMillis() - reqVo.getStartTime();

		TsmpApiLogResp respVo = new TsmpApiLogResp();
		respVo.setId(id);
		respVo.setaType(aType);
		respVo.setCid(cid);
		respVo.setCip(cip);
		respVo.setElapse(elapse);
		respVo.setEntry(entry);
		respVo.setHeaders(esHeaderMap);
		respVo.setHttpStatus(httpStatus);
		respVo.setOrgId(orgId);
		respVo.setJti(jti);
		respVo.setMbody(mbody);
		respVo.setModuleName(moduleName);
		respVo.setMtype(mtype);
		respVo.setNode(node);
		respVo.setCreateTimestamp(createTimestamp);
		respVo.setOriginServerResponseTime(originServerResponseTime);
		respVo.setTs(ts);
		respVo.setTxid(txid);
		respVo.setType(type);
		respVo.setUrl(url);

		String strJson = getObjectMapper().writeValueAsString(respVo);
		String esReqUrl = reqVo.getEsUrl();

//		Map<String, String> cfpResp2_header = new HashMap<>();
//		cfpResp2_header.put("Accept", "application/json");
//		cfpResp2_header.put("Content-Type", "application/json");
//		cfpResp2_header.put("Authorization", "Basic " + reqVo.getEsIdPwd());

		// asynchronous call
		int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
		DgrApiLog2ESQueue.put(new DgrApiLog2ESQueue(timeout, strJson, esReqUrl, id + "-" + type)
				, TPILogger.tl.diskFreeThreshHold
				, TPILogger.tl.deletePercent
				, TPILogger.tl.allowWriteElastic);

//		this.execute(() -> {
//			try {
//				HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, HttpMethod.POST.toString(), strJson, cfpResp2_header, false);
//				//TPILogger.tl.debug(resp.getLogStr());
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});
	}

	public void addEsTsmpApiLogResp1(ResponseEntity<?> verifyResp, TsmpApiLogReq reqVo, String mbody) throws Exception {
		Map<String, List<String>> headerMap = new HashMap<>();
		verifyResp.getHeaders().forEach((k, vlist) -> {
			headerMap.put(k, vlist);
		});

		// 因為驗証錯誤不遮罩,所以contentLength給0
		this.addEsTsmpApiLogResp1(headerMap, reqVo, mbody, verifyResp.getStatusCodeValue(), 0);
	}

	public void addEsTsmpApiLogResp1(HttpServletResponse httpResp, TsmpApiLogReq reqVo, String mbody, int contentLength)
			throws Exception {
		Map<String, List<String>> headerMap = new HashMap<>();
		List<String> headerNameList = httpResp.getHeaderNames().stream().distinct().collect(Collectors.toList());// 移除重複的
																													// HeaderName
		for (String name : headerNameList) {
			List<String> valList = httpResp.getHeaders(name).stream().collect(Collectors.toList());
			headerMap.put(name, valList);
		}

		this.addEsTsmpApiLogResp1(headerMap, reqVo, mbody, httpResp.getStatus(), contentLength);
	}

	public void addEsTsmpApiLogResp1(Map<String, List<String>> headerMap, TsmpApiLogReq reqVo, String mbody,
			int statusCode, int contentLength) throws Exception {
		if (reqVo.isIgnore()) {
			return;
		}

		String node = reqVo.getNode();
		String orgId = reqVo.getOrgId();
		String cid = reqVo.getCid();
		String mtype = "2";
		String id = reqVo.getId();
		String url = reqVo.getUrl();
		String jti = reqVo.getJti();
		String moduleName = reqVo.getModuleName();
		String type = "4";
		String originServerResponseTime = DateTimeUtil.dateTimeToString(new Date(), DateTimeFormatEnum.西元年月日T時分秒時區)
				.orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String cip = reqVo.getCip();
		Integer httpStatus = statusCode;
		String txid = reqVo.getTxid();
		String aType = reqVo.getaType();
		String ts = reqVo.getTs();
		String entry = reqVo.getEntry();
		Long createTimestamp = reqVo.getCreateTimestamp();
		mbody = this.convertMbody(mbody, reqVo, String.valueOf(contentLength));

		Map<String, List<String>> esHeaderMap = new HashMap<>(headerMap);
		// respHeader會有null的KEY,這個也照常辦理
		esHeaderMap.remove(null);

		// 若有MASK_AUTH_LIST,要轉換值
		for (String cfpResp1_auth : CommForwardProcService.MASK_TOKEN_LIST) {
			for (String key : esHeaderMap.keySet()) {
				if (cfpResp1_auth.equalsIgnoreCase(key)) {
					List<String> authList = esHeaderMap.get(key);
					authList = this.convertAuth(authList);
					esHeaderMap.put(key, authList);
					break;
				}
			}
		}

		long elapse = System.currentTimeMillis() - reqVo.getStartTime();
		TsmpApiLogResp respVo = new TsmpApiLogResp();
		respVo.setaType(aType);
		respVo.setCid(cid);
		respVo.setCip(cip);
		respVo.setEntry(entry);
		respVo.setHeaders(esHeaderMap);
		respVo.setHttpStatus(httpStatus);
		respVo.setId(id);
		respVo.setJti(jti);
		respVo.setElapse(elapse);
		respVo.setMbody(mbody);
		respVo.setModuleName(moduleName);
		respVo.setMtype(mtype);
		respVo.setOrgId(orgId);
		respVo.setOriginServerResponseTime(originServerResponseTime);
		respVo.setTs(ts);
		respVo.setTxid(txid);
		respVo.setNode(node);
		respVo.setType(type);
		respVo.setUrl(url);
		respVo.setCreateTimestamp(createTimestamp);

		String strJson = getObjectMapper().writeValueAsString(respVo);
		String esReqUrl = reqVo.getEsUrl();

//		Map<String, String> cfpResp1_header = new HashMap<>();
//		cfpResp1_header.put("Accept", "application/json");
//		cfpResp1_header.put("Content-Type", "application/json");
//		cfpResp1_header.put("Authorization", "Basic " + reqVo.getEsIdPwd());

		// asynchronous call
		int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
		DgrApiLog2ESQueue.put(new DgrApiLog2ESQueue(timeout, strJson, esReqUrl, id + "-" + type)
				, TPILogger.tl.diskFreeThreshHold
				, TPILogger.tl.deletePercent
				, TPILogger.tl.allowWriteElastic);

//		this.execute(() -> {
//			try {
//				HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, HttpMethod.POST.toString(), strJson, cfpResp1_header, false);
//				//TPILogger.tl.debug(resp.getLogStr());
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});
	}

	public String convertMbody(String mbody, TsmpApiLogReq reqVo, String strContentLength)
			throws UnsupportedEncodingException {
		if (mbody == null) {
			return "";
		}
		// 取得是否全部遮罩資訊
		boolean isMask = getTsmpSettingService().getVal_ES_MBODY_MASK_FLAG();

		// TSMPC是不是有要遮罩的API
		if (!isMask && "tsmpc".equals(reqVo.getEntry())) {
			String mbodyMaskApi = getTsmpSettingService().getVal_ES_MBODY_MASK_API();
			if (StringUtils.hasText(mbodyMaskApi)) {
				String[] arrUrl = mbodyMaskApi.split(",");
				for (String url : arrUrl) {
					String[] arrApi = url.split("/");
					if (arrApi.length == 2 && arrApi[0].equals(reqVo.getModuleName())
							&& arrApi[1].equals(reqVo.getTxid())) {
						isMask = true;
						break;
					}
				}
			}
		}

		// DGRC是否要遮罩的Uri
		if (!isMask && "dgrc".equals(reqVo.getEntry())) {
			String dgrcMbodyMaskUri = getTsmpSettingService().getVal_ES_DGRC_MBODY_MASK_URI();
			if (StringUtils.hasText(dgrcMbodyMaskUri)) {
				String[] arrUri = dgrcMbodyMaskUri.split(",");
				for (String uri : arrUri) {
					if (uri.equals(reqVo.getDgrcUri())) {
						isMask = true;
						break;
					}
				}
			}
		}

		// max size
		if (!isMask) {
			int contentLength = 0;
			try {
				if (strContentLength != null) {
					contentLength = Integer.parseInt(strContentLength);
				}

			} catch (Exception e) {
				TPILogger.tl.error("content-type is parse number error [" + strContentLength + "]");
			}

			int maxSize = getTsmpSettingService().getVal_ES_MAX_SIZE_MBODY_MASK();
			if (maxSize > 10 && contentLength > maxSize) {
				isMask = true;
			}
		}

		// 判斷是否遮罩
		if (isMask && StringUtils.hasText(mbody)) {
			byte[] arrAllMbody = mbody.getBytes(StandardCharsets.UTF_8);
			// 大於10長度,就取前尾各5byte
			if (arrAllMbody.length > 10) {
				byte[] arrStart = Arrays.copyOf(arrAllMbody, 5);
				byte[] arrEnd = Arrays.copyOfRange(arrAllMbody, arrAllMbody.length - 5, arrAllMbody.length);
				mbody = HexStringUtils.toString(arrStart) + "..." + HexStringUtils.toString(arrEnd);
			}
			return mbody;
		} else {
			return mbody;
		}
	}

	public List<String> convertAuth(List<String> authList) {
		if (CollectionUtils.isEmpty(authList)) {
			return authList;
		}

		boolean tokenMaskFlag = getTsmpSettingService().getVal_ES_TOKEN_MASK_FLAG();

		if (tokenMaskFlag) {
			List<String> retList = new ArrayList<>();
			for (String auth : authList) {
				if (auth == null) {
					retList.add(null);
					continue;
				}
				// 遮罩
				auth = this.maskAuth(auth);

				retList.add(auth);
			}

			return retList;
		} else {
			return authList;
		}
	}

	public String convertAuth(String key, String value01, Map<String, String> maskInfo) {
		try {
			if (!StringUtils.hasText(key) || !StringUtils.hasText(value01)) {
				return value01;
			}

			// authorization, tokenpayload, backauth 有固定的 mask, 不受 API List 控制
			if (MASK_TOKEN_LIST.contains(key.toLowerCase())) {
				// 遮罩
				value01 = this.maskAuth(value01);
				return value01;
			} else {
				// 這是由 API List 中的 header mask 控制
				value01 = this.maskHeader(maskInfo, key, value01);
				return value01;
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return value01;
		}
	}

	private String maskAuth(String value01) {
		if (!StringUtils.hasText(value01)) {
			return value01;
		}

		// 分割,前面可能是Bearer或其他
		String[] arrAuth = value01.split(" ");
		StringBuilder sb = new StringBuilder();
		String perfix = null;
		if (arrAuth.length > 1) {
			perfix = arrAuth[0];
			if ("bearer".equalsIgnoreCase(perfix) || "basic".equalsIgnoreCase(perfix)
					|| "dgrk".equalsIgnoreCase(perfix)) {
				for (int i = 1; i < arrAuth.length; i++) {
					if (i == arrAuth.length - 1) {
						sb.append(arrAuth[i]);
					} else {
						sb.append(arrAuth[i]).append(" ");
					}
				}
				value01 = sb.toString();
			}
		}

		// 遮罩
		value01 = MaskUtil.maskByDefault(value01);

		if (StringUtils.hasText(perfix)) {
			value01 = perfix + " " + value01;
		}

		return value01;
	}

	public String getReqMbody(HttpServletRequest httpReq) throws IOException, ServletException {

		Map<String, String> partContentTypes = new HashMap<>();

		// httpReq.getContentType() 可能為 null
		String contentType = httpReq.getContentType();
		if (contentType == null) {
			contentType = "";
		}

		if (ContentType.MULTIPART_FORM_DATA.toString().equals(httpReq.getContentType())) {
			for (Part part : httpReq.getParts()) {
				partContentTypes.put(part.getName(), part.getContentType());
			}
		}

		StringBuilder sb = new StringBuilder();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				sb.append("{" + k + ":" + Arrays.asList(vs) + "Content Type :　" + partContentTypes.get(k) + "　},");
			}
		});
		String str = sb.toString();
		if (StringUtils.hasText(str)) {
			str = str.substring(0, str.length() - 1);
		}

		return str;
	}

	public boolean existIndex(String url, String indexName, String idPwd) throws IOException {
		String reqUrl = url + indexName;

		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put(AUTHORIZATION, "Basic " + idPwd);

		HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, HttpMethod.HEAD.toString(), null, header, false, null);
		if (resp.statusCode == HttpStatus.OK.value()) {
			return true;
		} else {
			TPILogger.tl.debug(indexName + " index not exist");
			return false;
		}
	}

	public void createIndex(String url, String indexName, String idPwd) throws IOException {
		String reqUrl = url + indexName;

		/*
		 * StringBuilder reqBody = new StringBuilder();
		 * reqBody.append("{                                ");
		 * reqBody.append("	\"settings\":{               ");
		 * reqBody.append("	    \"number_of_shards\":1,  ");
		 * reqBody.append("	    \"number_of_replicas\":0 ");
		 * reqBody.append("     }                           ");
		 * reqBody.append("}                                ");
		 */

		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put(AUTHORIZATION, "Basic " + idPwd);

		HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, HttpMethod.PUT.toString(), null, header, false, null);
		TPILogger.tl.debug(resp.getLogStr());
	}

	/**
	 * 若有勾選 Token Payload, 無論是 JWS 或 JWE, <br> 
	 * token payload 是 payload 的 body 用 base64URL encode 的字串, <br> 
	 * 放到 http header 的 "tokenpayload"
	 */
	public String getTokenpayload(String authorization) {
		String base64UrlPayload = null;

		// 判斷是否有 "bearer "
		boolean hasKeyword = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
		if (!hasKeyword) {
			return null;
		}

		try {
			String[] sAry = authorization.split("\\s");
			String token = sAry[1];

			// 由 access token 取得資料
			JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(token);
			JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;

			String jsonPayload = payloadJsonNode.toString();
			base64UrlPayload = Base64Util.base64URLEncode(jsonPayload.getBytes());

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return base64UrlPayload;
	}

	public boolean getcApiKeySwitch(String moduleName, String apiKey) throws Exception {
		TsmpApiId apiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(apiId);
		boolean cApiKeySwitch = false;
		if (!tsmpApi.isPresent()) {
			throw new Exception("TSMP_API not found, api_key:" + apiKey + "\t,module_name:" + moduleName);
		} else {
			String apiSrc = tsmpApi.get().getApiSrc();
			if ("C".equals(apiSrc)) {
				cApiKeySwitch = true;
			}
		}
		return cApiKeySwitch;
	}

	public String getAutoCacheIdByFlagStart(TsmpApiLogReq dgrReqVo, String reqUrl, String mbody)
			throws UnsupportedEncodingException {
		TsmpApiId tsmpApiId = new TsmpApiId();
		tsmpApiId.setModuleName(dgrReqVo.getModuleName());
		tsmpApiId.setApiKey(dgrReqVo.getTxid());
		Optional<TsmpApi> tsmpApiVo = getTsmpApiCacheProxy().findById(tsmpApiId);
		if (tsmpApiVo.isPresent() && "2".equals(tsmpApiVo.get().getApiCacheFlag())) {
			return this.getCacheId(dgrReqVo, reqUrl, mbody);
		} else {
			return null;
		}
	}

	public String getFixedCacheIdByFlagStart(TsmpApiLogReq dgrReqVo, String reqUrl, String mbody)
			throws UnsupportedEncodingException {
		TsmpApiId tsmpApiId = new TsmpApiId();
		tsmpApiId.setModuleName(dgrReqVo.getModuleName());
		tsmpApiId.setApiKey(dgrReqVo.getTxid());
		Optional<TsmpApi> tsmpApiVo = getTsmpApiCacheProxy().findById(tsmpApiId);
		if (tsmpApiVo.isPresent() && "3".equals(tsmpApiVo.get().getApiCacheFlag())) {
			return this.getCacheId(dgrReqVo, reqUrl, mbody);
		} else {
			return null;
		}
	}

	private String getCacheId(TsmpApiLogReq dgrReqVo, String reqUrl, String mbody) throws UnsupportedEncodingException {
		byte[] arrHttpMethod = dgrReqVo.getHttpMethod().getBytes(StandardCharsets.UTF_8);
		byte[] arrUrl = reqUrl.getBytes(StandardCharsets.UTF_8);
		byte[] arrMbody = null;
		if(StringUtils.hasLength(mbody)) {
			arrMbody = mbody.getBytes(StandardCharsets.UTF_8);
		}else {
			arrMbody = new byte[0];
		}
		
		byte[] arrApiCacheId = new byte[arrHttpMethod.length + arrUrl.length + arrMbody.length];
		System.arraycopy(arrHttpMethod, 0, arrApiCacheId, 0, arrHttpMethod.length);
		System.arraycopy(arrUrl, 0, arrApiCacheId, arrHttpMethod.length, arrUrl.length);
		System.arraycopy(arrMbody, 0, arrApiCacheId, arrHttpMethod.length + arrUrl.length, arrMbody.length);

		byte[] hash = SHA256Util.getSHA256(arrApiCacheId);
		String strApiCacheId = HexStringUtils.toString(hash);
		return strApiCacheId;
	}

	/**
	 * 1.取得 API 的 request payload, <br>
	 * 2.做 JWS 驗章 或 JWE 解密 <br>
	 * (1).JWS 驗章, 使用 client(客戶) 的公鑰驗章, 取得 payload 做 Base64 Decode 後的值 <br>
	 * (2).JWE 解密, 使用 digiRunner 的私鑰解密, 取得 payload 解密後的值 <br>
	 */
	public JwtPayloadData getRequestJwtPayloadData(String jwtStr, String clientId, String noAuth) {
		JwtPayloadData jwtPayloadData = new JwtPayloadData();
		try {
			String[] jwtInfoArr = jwtStr.split("\\.");// 切分 JWT

			if (jwtInfoArr.length == 3) {// JWS
				/* JWS 驗章, 使用 client(客戶) 的公鑰驗章 */

				// 1.取得 client 憑證中的公鑰
				ClientPublicKeyData clientPublicKeyData = getClientPublicKeyData(clientId, noAuth);
				ResponseEntity<?> responseEntity = clientPublicKeyData.errRespEntity;
				if (responseEntity != null) {
					jwtPayloadData.errRespEntity = responseEntity;
					return jwtPayloadData;
				}

				// 取得公鑰
				PublicKey publicKey = clientPublicKeyData.clientPublicKey;

				// 2.JWS 驗章
				boolean verify = JWScodec.jwsVerifyByRS(publicKey, jwtStr);
				if (!verify) {// JWS 驗證不正確
					String errMsg = "API request JWS verify error.";
					TPILogger.tl.debug(errMsg);
					responseEntity = new ResponseEntity<OAuthTokenErrorResp2>(
							getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
							HttpStatus.BAD_REQUEST);// 400
					jwtPayloadData.errRespEntity = responseEntity;
				}

				// 3.取得 payload 做 Base64 Decode 後的值
				String payloadJsonStr = new String(Base64Util.base64URLDecode(jwtInfoArr[1]));
				ObjectMapper om = new ObjectMapper();
				jwtPayloadData.payloadStr = payloadJsonStr;
				jwtPayloadData.payloadJsonNode = om.readTree(payloadJsonStr);

			} else if (jwtInfoArr.length == 5) {// JWE
				/* JWE 解密, 使用 digiRunner 的私鑰解密 */

				// 1.取得 digiRunner 的私鑰
				// 取得私鑰
				PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();
				try {
					// 2.JWE 解密, 取得 payload 解密後的值
					String payloadJsonStr = JWEcodec.jweDecryption(privateKey, jwtStr);
					ObjectMapper om = new ObjectMapper();
					jwtPayloadData.payloadStr = payloadJsonStr;
					jwtPayloadData.payloadJsonNode = om.readTree(payloadJsonStr);

				} catch (Exception e) {// JWE 解密錯誤
					TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
					String errMsg = "API request JWE decryption error.";
					TPILogger.tl.debug(errMsg);
					ResponseEntity<?> responseEntity = new ResponseEntity<OAuthTokenErrorResp2>(
							getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
							HttpStatus.BAD_REQUEST);// 400
					jwtPayloadData.errRespEntity = responseEntity;
				}

			} else {// 錯訊格式
				jwtPayloadData.errRespEntity = getTokenHelper().getApiBodyFormatError();
			}

		} catch (Exception e) {
			TPILogger.tl.warn(StackTraceUtil.logStackTrace(e));
			jwtPayloadData.errRespEntity = getTokenHelper().getApiBodyFormatError();
		}

		return jwtPayloadData;
	}

	/**
	 * 取得 Client(客戶) 憑證中的公鑰
	 */
	public ClientPublicKeyData getClientPublicKeyData(String clientId, String noAuth) {
		ClientPublicKeyData clientPublicKeyData = new ClientPublicKeyData();

		// 1.是否為 No Auth
		if ("1".equals(noAuth)) {// No Auth,沒有驗證,就沒有 client
			// No Auth 找不到有效的憑證
			String errMsg = NO_AUTH_NO_VALID_CREDENTIALS_FOUND;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<?> errResEntity = new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 2.沒有 client ID
		if (!StringUtils.hasLength(clientId)) {
			// 沒有 client ID 找不到有效的憑證
			String errMsg = MISSING_CLIENT_ID_NO_VALID_CERTIFICATE_FOUND;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<?> errResEntity = new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 3.取得 client 的有效憑證
		TsmpClientCert tsmpClientCert = getClientCert(clientId);
		if (tsmpClientCert == null) {
			// Table [TSMP_CLIENT_CERT] 查不到 client
			TPILogger.tl.debug("Table [TSMP_CLIENT_CERT] can't find valid certificate, client_id:" + clientId);
			// 找不到有效的憑證
			String errMsg = NO_VALID_CERTIFICATE_FOUND + clientId;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<?> errResEntity = new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		// 4.產生 client 的公鑰
		try {
			String pubKeyStr = tsmpClientCert.getPubKey();
			PublicKey publicKey = PEMUtil.readPublicKey(pubKeyStr);
			clientPublicKeyData.clientPublicKey = publicKey;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 產生公鑰失敗
			String errMsg = FAILED_TO_GENERATE_PUBLIC_KEY + clientId;
			TPILogger.tl.debug(errMsg);
			ResponseEntity<?> errResEntity = new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
			clientPublicKeyData.errRespEntity = errResEntity;// 400
			return clientPublicKeyData;
		}

		return clientPublicKeyData;
	}

	/**
	 * 取得 client 的有效憑證, <br>
	 * 即過期時間 > 今天的日期,並依建立時間由小到大排序,取第一個 <br>
	 */
	public TsmpClientCert getClientCert(String clientId) {
		// 取得今天的日期,去掉時分秒,例如:2023/7/1
		Date dt = new Date();
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		Date nowDate = DateTimeUtil.stringToDateTime(dtStr, DateTimeFormatEnum.西元年月日_2).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		long nowLong = nowDate.getTime();

		// 找有效的憑證(過期時間 > 今天的日期),並依建立時間由小到大排序
		List<TsmpClientCert> certList = getTsmpClientCertCacheProxy()
				.findByClientIdAndExpiredAtAfterOrderByCreateDateTime(clientId, nowLong);

		if (CollectionUtils.isEmpty(certList)) {
			return null;
		} else {
			return certList.get(0);// 取第一個
		}
	}

	/**
	 * 依 API 在 DB 的設定, 將 原文/JWE/JWS 的 request body 轉成 原文, 若有錯誤訊息ResponseEntity<?>,
	 * 須加上 handleErrorHttpResp(httpRes, jwtPayloadData.errRespEntity), 才能在畫面顯示錯誤訊息
	 */
	public JwtPayloadData convertRequestBody(HttpServletResponse httpRes, HttpServletRequest httpReq, String payload,
			boolean isGet) throws Exception {

		JwtPayloadData jwtPayloadData = convertRequestBody(httpReq, payload);
		ResponseEntity<?> errRespEntity = jwtPayloadData.errRespEntity;
		if (errRespEntity != null) {// 資料驗證有錯誤
			if (!isGet) {
				handleErrorHttpResp(httpRes, jwtPayloadData.errRespEntity); // 直接 HTTP Resp 物件輸出了
			}
		}

//		TPILogger.tl.debug("reqStr:" + jwtPayloadData.payloadStr);
		return jwtPayloadData;
	}

	/**
	 * 依 API 在 DB 的設定, 將 原文/JWE/JWS 的 request body 轉成 原文
	 */
	public JwtPayloadData convertRequestBody(HttpServletRequest httpReq, String payload) throws Exception {
		// 從 request 中取得 client id、no Auth
		String clientId = (String) httpReq.getAttribute(TokenHelper.CLIENT_ID);
		String noAuth = (String) httpReq.getAttribute(TokenHelper.NO_AUTH);

		JwtPayloadData jwtPayloadData = new JwtPayloadData();
		try {
			if (!StringUtils.hasLength(payload)) {// 沒有 request body 不處理
				jwtPayloadData.payloadStr = payload;
				return jwtPayloadData;
			}

			TsmpApi tsmpApi = getTsmpApi(httpReq);
			String jweFlag = tsmpApi.getJewFlag();// 0:none, 1:JWE, 2:JWS

			// 依 API 的設定,取得 request body
			if ("1".equals(jweFlag)) {// JWE
				TPILogger.tl.debug("API request body format: JWE");
				String[] jwtInfoArr = payload.split("\\.");// 切分 JWT
				if (jwtInfoArr.length != 5) {
					ResponseEntity<?> errResEntity = getTokenHelper().getApiJweBodyFormatError();// 400
					jwtPayloadData.errRespEntity = errResEntity;// 400
				} else {
					jwtPayloadData = getRequestJwtPayloadData(payload, clientId, noAuth);
				}
			} else if ("2".equals(jweFlag)) {// JWS
				TPILogger.tl.debug("API request body format: JWS");
				String[] jwtInfoArr = payload.split("\\.");// 切分 JWT
				if (jwtInfoArr.length != 3) {
					ResponseEntity<?> errResEntity = getTokenHelper().getApiJwsBodyFormatError();// 400
					jwtPayloadData.errRespEntity = errResEntity;// 400
				} else {
					jwtPayloadData = getRequestJwtPayloadData(payload, clientId, noAuth);
				}
			} else {// none
//				TPILogger.tl.debug("API request body format: NONE");// 為一般的格式,就不印出此訊息了
				jwtPayloadData.payloadStr = payload;
			}

		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			jwtPayloadData.errRespEntity = getTokenHelper().getApiBodyFormatError();// 400
		}

		return jwtPayloadData;
	}

	/**
	 * 依設定,判斷是否轉換成 TW Open Banking 格式的 response body, 且依 API 在 DB 的設定,轉成 原文/JWE/JWS
	 * 的 response body
	 */
	public Map<String, Object> convertResponseBody(HttpServletResponse httpRes, HttpServletRequest httpReq,
			byte[] httpArray, String httpRespStr) throws Exception {
		// 從 request 中取得 client id、no Auth
		String clientId = (String) httpReq.getAttribute(TokenHelper.CLIENT_ID);
		String noAuth = (String) httpReq.getAttribute(TokenHelper.NO_AUTH);

		Map<String, Object> convertResponseBodyMap = new HashMap<String, Object>();

		if(httpRes == null) {
			return null;
		}
		
		int status = httpRes.getStatus();
		if (status >= 300) {// statusCode 大於 300 的錯誤訊息直接顯示,不處理壓縮,不轉JWE/JWS
			convertResponseBodyMap.put("httpArray", httpArray);
			convertResponseBodyMap.put("httpRespStr", httpRespStr);
			return convertResponseBodyMap;
		}

		// 判斷 0:none, 1:JWE, 2:JWS
		TsmpApi tsmpApi = getTsmpApi(httpReq);
		String jweFlagResp = tsmpApi.getJewFlagResp();// 0:none, 1:JWE, 2:JWS
		String respBody = null;

		char isCompressFormat = 'N'; // N=No, D=Deflate, G=Gzip, O=Other
		String content_encoding = httpRes.getHeader("Content-Encoding");
		if (content_encoding == null) {
			isCompressFormat = 'N';
		} else if (content_encoding.contains("deflate")) {
			isCompressFormat = 'D';
		} else if (content_encoding.contains("gzip")) {
			isCompressFormat = 'G';
		} else {
			isCompressFormat = 'O';
		}

		// 有 1:JWE, 2:JWS , 需要才 UnCompress
		if ("1".equals(jweFlagResp) || "2".equals(jweFlagResp)) {

			// 處理 httpArray, 若是 Compress , 需要把它 UnCompress
			if (isCompressFormat == 'N') {
				respBody = new String(httpArray, "UTF-8");
			} else if (isCompressFormat == 'D') {
				respBody = HttpUtil.deflate_UnCompress(httpArray);
			} else if (isCompressFormat == 'G') {
				respBody = HttpUtil.gzip_UnCompress(httpArray);
			} else {
				respBody = new String(httpArray, "UTF-8");
			}
		}

		// ------------------------

		PublicKey publicKey = null;
		if ("1".equals(jweFlagResp)) {// JWE
			// 取得 client 憑證中的公鑰
			ClientPublicKeyData clientPublicKeyData = getClientPublicKeyData(clientId, noAuth);
			ResponseEntity<?> responseEntity = clientPublicKeyData.errRespEntity;
			if (responseEntity != null) {
				// 錯誤訊息直接顯示,不處理壓縮,不轉JWE/JWS
				httpRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);// 400

				var body = responseEntity.getBody();

				if (body == null) {
					throw TsmpDpAaRtnCode._1291.throwing();
				}

				var oauthTokenErrorResp2 = (OAuthTokenErrorResp2) body;
				String httpRespStr2 = oauthTokenErrorResp2.getErrorDescription();
				convertResponseBodyMap.put("httpArray", httpRespStr2.getBytes());
				convertResponseBodyMap.put("httpRespStr", httpRespStr2);

				return convertResponseBodyMap;
			}

			publicKey = clientPublicKeyData.clientPublicKey;
		}

		// 將 response 轉成 1:JWE, 2:JWS 或 0:NONE
		String respStr = "";
		try {
			respStr = getResponseJwtStr(publicKey, respBody, jweFlagResp);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		// 有 1:JWE, 2:JWS , 才需要再次 Compress
		if ("1".equals(jweFlagResp) || "2".equals(jweFlagResp)) {
			// 若有 Compress 格式, 需要再壓回去
			if (isCompressFormat == 'N') {
				httpArray = respStr.getBytes(); // don't Compress it
				httpRespStr = respStr;
			} else if (isCompressFormat == 'D') {
				httpArray = HttpUtil.deflate_compress(respStr.getBytes());
				httpRespStr = HttpUtil.PREFIX_Sha256_Hex + HexStringUtils.toString(SHA256Util.getSHA256(httpArray));
			} else if (isCompressFormat == 'G') {
				httpArray = HttpUtil.gzip_compress(respStr.getBytes());
				httpRespStr = HttpUtil.PREFIX_Sha256_Hex + HexStringUtils.toString(SHA256Util.getSHA256(httpArray));
			} else {
				httpArray = respStr.getBytes(); // don't Compress it
				httpRespStr = respStr;
			}
		}

		convertResponseBodyMap.put("httpArray", httpArray);
		convertResponseBodyMap.put("httpRespStr", httpRespStr);//
		return convertResponseBodyMap;
	}

	/**
	 * 1.將 response 轉成 1:JWE, 2:JWS 或 0:NONE <br>
	 * 2. <br>
	 * 若 0:NONE, 不轉換 <br>
	 * 若 1:JWE, 使用 client(客戶) 的公鑰加密, 取得 JWE <br>
	 * 若 2:JWS, 使用 digiRunner 的私鑰簽章, 取得 JWS <br>
	 */
	public String getResponseJwtStr(PublicKey publicKey, String respBody, String jweFlagResp) throws Exception {
		String respStr = "";
		String payloadStr = null;

		// 訊息使用 TW Open Banking 格式是否啟用
		boolean isTwOpenBanking = getTsmpSettingService().getVal_DGR_TW_FAPI_ENABLE();
//		TPILogger.tl.debug("isTwOpenBanking:" + isTwOpenBanking);

		if (isTwOpenBanking) {
			// TODO,做 TW Open Banking 的格式,是不是一律要有 token,不然無法取到 client id ?
			// 轉成 TW Open Banking 的 response 格式
			payloadStr = getTWOpenBankingResponse(respBody);
		} else {
			payloadStr = respBody;
		}

		// 依 API 各別的設定,取得 response body(轉為 JWE 或 JWS 格式)
		if ("1".equals(jweFlagResp)) {// JWE
			/* JWE 加密, 使用 Client(客戶) 的公鑰加密 */
			TPILogger.tl.debug("API response body format: JWE");

			// 1.取得 client 憑證中的公鑰
			// 2.取得 response 加密後的 JWE
			String jweStr = JWEcodec.jweEncryption(publicKey, payloadStr);

			if (isTwOpenBanking) {
				respStr = getTWReqRespJweStr(jweStr);
			} else {
				respStr = jweStr;
			}

		} else if ("2".equals(jweFlagResp)) {// JWS
			/* JWS 簽章, 使用 digiRunner 的私鑰簽章 */
			TPILogger.tl.debug("API response body format: JWS");
			if (isTwOpenBanking) {
				// 取得 AES Key
				String aesKey = getAesKey();
				TPILogger.tl.debug("--TAEASK:" + ServiceUtil.dataMask(aesKey, 2, 2));
				String hexSymmetricKey = HexUtils.toHexString(aesKey.getBytes());// 轉成 16 進位
				// 轉成 JWS,依 TW Open Banking 規定的 HMAC SHA-256
				String jwsStr = JWScodec.jwsSignByHS(payloadStr, JWSAlgorithm.HS256, hexSymmetricKey);
				respStr = getTWReqRespJwsStr(jwsStr);

			} else {
				// 1.取得私鑰
				PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();

				// 2.取得 response 簽章後的 JWS
				String jwsStr = JWScodec.jwsSign(privateKey, payloadStr);
				respStr = jwsStr;
			}

		} else {// NONE
//			TPILogger.tl.debug("API response body format: NONE");// 為一般的格式,就不印出此訊息了
			respStr = payloadStr;
		}

		return respStr;
	}

	/**
	 * 將 API 的 response 訊息 轉成 TW Open Banking 的 response 格式
	 */
	private String getTWOpenBankingResponse(String responseBody) throws Exception {
		TWHeader twHeader = new TWHeader();
		TWResponseBody twResponseBody = new TWResponseBody();
		twResponseBody.setAppRepBody(responseBody);
		twResponseBody.setAppRepExtension("");

		TWResponse twResponse = new TWResponse();
		twResponse.setTwHeader(twHeader);
		twResponse.setTwResponseBody(twResponseBody);

		String payloadStr = getObjectMapper().writeValueAsString(twResponse);
		return payloadStr;
	}

	/**
	 * 取得依 TW Open Banking 格式,簽章後 API 訊息區塊文字 (JWS)
	 */
	private String getTWReqRespJwsStr(String jwsStr) throws Exception {
		// 取得 JWE 3 個部份
		String[] jwsInfoArr = jwsStr.split("\\.");// 切分 token
		String jwsHeaderBase64UrlEnc = jwsInfoArr[0];
		String jwsPayloadBase64UrlEnc = jwsInfoArr[1];
		String jwsSignatureBase64UrlEnc = jwsInfoArr[2];

		TWReqRespJwsSignatures twReqRespJwsSignatures = new TWReqRespJwsSignatures();
		twReqRespJwsSignatures.setProtectedData(jwsHeaderBase64UrlEnc);
		twReqRespJwsSignatures.setSignature(jwsSignatureBase64UrlEnc);
		List<TWReqRespJwsSignatures> signaturesList = new ArrayList<TWReqRespJwsSignatures>();
		signaturesList.add(twReqRespJwsSignatures);

		TWReqRespJws twReqRespJws = new TWReqRespJws();
		twReqRespJws.setPayload(jwsPayloadBase64UrlEnc);
		twReqRespJws.setTwReqRespJwsSignaturesList(signaturesList);

		String respStr = getObjectMapper().writeValueAsString(twReqRespJws);
		return respStr;
	}

	/**
	 * 取得依 TW Open Banking 格式,加密後 API 訊息區塊（JWE）
	 */
	private String getTWReqRespJweStr(String jweStr) throws Exception {
		// 取得 JWE 5個部份
		String[] jweInfoArr = jweStr.split("\\.");// 切分 token
		String jweProtectedHeaderBase64UrlEnc = jweInfoArr[0];
		String jweEncryptedKeyBase64UrlEnc = jweInfoArr[1];
		String jweIvBase64UrlEnc = jweInfoArr[2];
		String jweCipherTextBase64UrlEnc = jweInfoArr[3];
		String jweTagBase64UrlEnc = jweInfoArr[4];

		// ex. {"kid":"encryptKid","enc":"A128CBC-HS256","alg":"RSA-OAEP"}
		String protectedHeader = new String(Base64Util.base64URLDecode(jweProtectedHeaderBase64UrlEnc));
		JsonNode jsonNode = getObjectMapper().readTree(protectedHeader);
		String alg = JsonNodeUtil.getNodeAsText(jsonNode, "alg");

		Map<String, String> algMap = new HashMap<String, String>();
		algMap.put("alg", alg);

		TWReqRespJweRecipients twReqRespJweRecipients = new TWReqRespJweRecipients();
		twReqRespJweRecipients.setHeader(algMap);// ex. {"alg":"RSA-OAEP"}
		twReqRespJweRecipients.setEncryptedKey(jweEncryptedKeyBase64UrlEnc);

		List<TWReqRespJweRecipients> twReqRespJweRecipientsList = new ArrayList<TWReqRespJweRecipients>();
		twReqRespJweRecipientsList.add(twReqRespJweRecipients);

		TWReqRespJwe twReqRespJwe = new TWReqRespJwe();
		twReqRespJwe.setEncProtectedData(jweProtectedHeaderBase64UrlEnc);
		twReqRespJwe.setEncTwReqRespJweRecipientsList(twReqRespJweRecipientsList);
		twReqRespJwe.setEncIv(jweIvBase64UrlEnc);
		twReqRespJwe.setEncCipherText(jweCipherTextBase64UrlEnc);
		twReqRespJwe.setEncTag(jweTagBase64UrlEnc); 

		String respStr = getObjectMapper().writeValueAsString(twReqRespJwe);
		return respStr;
	}

	public void handleErrorHttpResp(HttpServletResponse httpRes, ResponseEntity<?> errRespEntity) throws IOException {
		// set HTTP code to RESP
		httpRes.setStatus(errRespEntity.getStatusCodeValue());

		// set HTTP Header
		httpRes.addHeader("javaObject", getClass().getName());
		httpRes.addHeader("Content-Type", "application/json;charset=UTF-8");
		//checkmarx, Missing HSTS Header
		httpRes.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
		        
		// set HTTP Resp
		// 2023/05/03 若這裡註解打開, Response 錯誤訊息時,會有 json 重複問題
//		String json = objectMapper.writeValueAsString(errRespEntity.getBody());
//		json = HttpUtil.toPrettyJson(json);
//		ByteArrayInputStream bi = new ByteArrayInputStream(json.getBytes());				
//		IOUtils.copy(bi, httpRes.getOutputStream());
	}

	public TsmpApiLogReq addRdbTsmpApiLogReq1(String uuid, HttpServletRequest httpReq, //
			String mbody, String entry, String aType) throws Exception {
		Map<String, String> logParams = new HashMap<>();
		logParams.put("uri", httpReq.getRequestURI());
		logParams.put(MODULE_NAME, getAttribute(httpReq, GatewayFilter.MODULE_NAME));
		logParams.put("txid", getAttribute(httpReq, GatewayFilter.API_ID));
		logParams.put("httpMethod", httpReq.getMethod());
		logParams.put("orgId", httpReq.getAttribute(TokenHelper.ORG_ID) == null ? ""
				: ((String) httpReq.getAttribute(TokenHelper.ORG_ID)));
		logParams.put("jti",
				httpReq.getAttribute(TokenHelper.JTI) == null ? "" : ((String) httpReq.getAttribute(TokenHelper.JTI)));
		logParams.put("clientId", httpReq.getAttribute(TokenHelper.CLIENT_ID) == null ? ""
				: ((String) httpReq.getAttribute(TokenHelper.CLIENT_ID)));
		logParams.put("userName", httpReq.getAttribute(TokenHelper.USER_NAME) == null ? ""
				: ((String) httpReq.getAttribute(TokenHelper.USER_NAME)));
		logParams.put("cip",
				StringUtils.hasText(httpReq.getHeader("x-forwarded-for")) ? httpReq.getHeader("x-forwarded-for")
						: httpReq.getRemoteAddr());
		return addRdbTsmpApiLogReq1(uuid, logParams, mbody, entry, aType);
	}

	// v4 的第 1 道 Log
	public TsmpApiLogReq addRdbTsmpApiLogReq1(String uuid, Map<String, String> logParams, //
			String mbody, String entry, String aType) throws Exception {
		TsmpApiLogReq reqVo = new TsmpApiLogReq();

		// entry
		reqVo.setEntry(entry);

		// dgrcUri
		String uri = logParams.get("uri");
		reqVo.setDgrcUri(uri);

		// moduleName
		String moduleName = logParams.get(MODULE_NAME);
		reqVo.setModuleName(moduleName);

		// txid
		String txid = logParams.get("txid");
		reqVo.setTxid(txid);

		// httpMethod
		String httpMethod = logParams.get("httpMethod");
		reqVo.setHttpMethod(httpMethod);

		// 是否禁止紀錄 RDB 的 LOG
		boolean isAvailable = Boolean.parseBoolean(getTsmpSettingService().getVal_TSMP_APILOG_FORCE_WRITE_RDB());
		if(!isAvailable) {
			reqVo.setIgnore(true);
			return reqVo;
		}

		// API 是否設定不寫入 Log
		String api = moduleName + "/" + txid;
		isAvailable = !this.checkIgnoreApi(entry, api, uri);
		if (!isAvailable) {
			TPILogger.tl.info(entry + " " + api + " or " + uri + " is rdb ignore api");
			reqVo.setIgnore(true);
			return reqVo;
		}

		// id
		String id = uuid;
		reqVo.setId(id);

		// node
		String node = "N/A";
		try {
			//防止KeeperServer斷線造成錯誤,所以try..catch
			node = TPILogger.lc.param.get(TPILogger.nodeInfo);
		}catch(Exception e) {
			try {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}catch(Exception ex) {}
		}	
		reqVo.setNode(node);

		// orgId
		String orgId = logParams.get("orgId");
		reqVo.setOrgId(orgId);

		// jti
		String jti = logParams.get("jti");
		reqVo.setJti(jti);

		// cid
		String cid = logParams.get("clientId");
		reqVo.setCid(cid);

		// userName
		String userName = logParams.get("userName");
		reqVo.setUserName(userName);

		// aType
		reqVo.setaType(aType);

		// cip
		String cip = logParams.get("cip");
		reqVo.setCip(cip);

		final TsmpReqLog reqLogEntity = new TsmpReqLog();
		// 'id' is the PK of TSMP_REQ_LOG, can't apply the same UUID on this field.
		reqLogEntity.setId(reqVo.getId());
		reqLogEntity.setRtime(DateTimeUtil.now());
		reqLogEntity.setAtype(reqVo.getaType());
		reqLogEntity.setModuleName(reqVo.getModuleName());
		reqLogEntity.setModuleVersion(" ");
		reqLogEntity.setNodeAlias(reqVo.getNode());
		reqLogEntity.setNodeId(reqVo.getNode());
		try {
			reqLogEntity.setNodeId(reqVo.getNode().split(" / ")[0]);
		} catch (Exception e) {
		}
		reqLogEntity.setUrl(reqVo.getDgrcUri());
		reqLogEntity.setCip(reqVo.getCip());
		reqLogEntity.setOrgid(reqVo.getOrgId());
		reqLogEntity.setTxid(reqVo.getTxid());
		reqLogEntity.setEntry(reqVo.getEntry());
		reqLogEntity.setCid(reqVo.getCid());
		reqLogEntity.setTuser(reqVo.getUserName());
		reqLogEntity.setJti(reqVo.getJti());

		// asynchronous call
		DgrApiLog2RdbQueue.put(new DgrApiLog2RdbQueue() {
			public void run() {
				try {
					arrayListQ.add(reqLogEntity);
					tsmpReqLogDaoObj = getTsmpReqLogDao();
				} catch (Exception e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}
		});

//		this.execute(() -> {
//			try {
//				getTsmpReqLogDao().save(reqLogEntity);
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});

		// 傳遞用
		reqVo.setCreateTimestamp(System.currentTimeMillis());

		return reqVo;
	}

	public void addRdbTsmpApiLogResp1(ResponseEntity<?> verifyResp, TsmpApiLogReq reqVo, String mbody)
			throws Exception {
		Map<String, List<String>> headerMap = new HashMap<>();
		verifyResp.getHeaders().forEach((k, vlist) -> {
			headerMap.put(k, vlist);
		});

		// 因為驗証錯誤不遮罩,所以contentLength給0
		this.addRdbTsmpApiLogResp1(headerMap, reqVo, mbody, verifyResp.getStatusCodeValue(), 0);
	}

	public void addRdbTsmpApiLogResp1(HttpServletResponse httpResp, TsmpApiLogReq reqVo, String mbody,
			int contentLength) throws Exception {
		Map<String, List<String>> headerMap = new HashMap<>();
		List<String> headerNameList = httpResp.getHeaderNames().stream().distinct().collect(Collectors.toList());// 移除重複的
																													// HeaderName
		for (String name : headerNameList) {
			List<String> valList = httpResp.getHeaders(name).stream().collect(Collectors.toList());
			headerMap.put(name, valList);
		}

		this.addRdbTsmpApiLogResp1(headerMap, reqVo, mbody, httpResp.getStatus(), contentLength);
	}

	public void addRdbTsmpApiLogResp1(Map<String, List<String>> headerMap, TsmpApiLogReq reqVo, String mbody,
			int statusCode, int contentLength) throws Exception {

		if (reqVo.isIgnore()) {
			return;
		}

		TsmpApiLogResp respVo = new TsmpApiLogResp();

		// id
		String id = reqVo.getId();
		respVo.setId(id);

		// httpStatus
		Integer httpStatus = statusCode;
		respVo.setHttpStatus(httpStatus);

		// mBody
		mbody = this.convertMbody(mbody, reqVo, String.valueOf(contentLength));
		respVo.setMbody(mbody);

		// elapse
		long elapse = System.currentTimeMillis() - reqVo.getCreateTimestamp();
		respVo.setElapse(elapse);

		TsmpResLog resLogEntity = new TsmpResLog();
		resLogEntity.setId(respVo.getId());
		// exeStatus
		Boolean succ = respVo.getHttpStatus() > 0 && respVo.getHttpStatus() < 400; // 200~399
		if (succ) {
			resLogEntity.setExeStatus("Y");
		} else {
			resLogEntity.setExeStatus("N");
			String errMsg = respVo.getMbody();
			if (errMsg != null && errMsg.length() > 400) { // v3 邏輯
				errMsg = errMsg.substring(0, 400);
			}
			resLogEntity.setErrMsg(errMsg);
		}
		resLogEntity.setElapse(respVo.getElapse().intValue());
		resLogEntity.setRcode(" "); // No return code for v4
		resLogEntity.setHttpStatus(respVo.getHttpStatus());

		// asynchronous call
		DgrApiLog2RdbQueue.put(new DgrApiLog2RdbQueue() {
			public void run() {
				try {
					arrayListP.add(resLogEntity);
					tsmpResLogDaoObj = getTsmpResLogDao();
				} catch (Exception e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}
		});

//		this.execute(() -> {
//			try {
//				getTsmpResLogDao().save(resLogEntity);
//			} catch (Exception e) {
//				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			}
//		});
	}
	
	public boolean isFixedCacheUpdate(FixedCacheVo cacheVo, TsmpApiLogReq reqVo) {
		TsmpApiId tsmpApiId = new TsmpApiId();
		tsmpApiId.setModuleName(reqVo.getModuleName());
		tsmpApiId.setApiKey(reqVo.getTxid());
		TsmpApi tsmpApiVo = getTsmpApiCacheProxy().findById(tsmpApiId).orElse(null);
		int cacheTime = 0 ;
		if(tsmpApiVo != null) {
			cacheTime = tsmpApiVo.getFixedCacheTime();
		}
		if(cacheTime == 0) {
			cacheTime = getTsmpSettingService().getVal_FIXED_CACHE_TIME();
		}
		long cacheTimestamp = cacheVo.getDataTimestamp() + ( cacheTime * 60 * 1000);
		long nowTimestamp = System.currentTimeMillis();
		
		return nowTimestamp > cacheTimestamp;
	}
	
	/**
	 * 是否停止重試調用API, <br>
	 * 依失敗處置策略判斷, <br>
	 * Policy 0: 當連線失敗或 HTTP 狀態碼為 4xx(客戶端錯誤)、5xx(伺服器錯誤),則重試<br>
	 */
	protected boolean isStopReTry(String failDiscoveryPolicy, int httpStatus) {
		if (!StringUtils.hasLength(failDiscoveryPolicy)) {
			failDiscoveryPolicy = "0";
		}

		if ("0".equals(failDiscoveryPolicy)) {// Policy 0
			if (httpStatus == -1 || httpStatus == 0) {// 連線失敗時,response status 為 -1, 因為 undertow, 不接受 -1, 所以多增加 0
				return false;

			} else if (httpStatus >= 400) {// 調用API失敗,response status 為 4xx、5xx
				return false;

			} else {// 調用API成功,則不再重試
				return true;
			}

		} else {
			return true;
		}
	}

	/**
	 * 取得平台啟動時, 由 shell 輸入的 AES Key <br>
	 * 註: 需要交付組在啟動v4的shell或bat中設定"TAEASK"的值，這邊才會抓得到
	 */
	protected String getAesKey() {
		String key = System.getenv("TAEASK");
		return key;
	}
}