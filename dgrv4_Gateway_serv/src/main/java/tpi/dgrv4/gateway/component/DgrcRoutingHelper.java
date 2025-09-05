package tpi.dgrv4.gateway.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import oshi.util.tuples.Pair;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.UrlCodecHelper;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.cache.proxy.DgrMtlsClientCertCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiRegCacheProxy;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp;
import tpi.dgrv4.httpu.utils.CertificateInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DgrcRoutingHelper {
	
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private TsmpApiRegCacheProxy tsmpApiRegCacheProxy;
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private CommForwardProcService commForwardProcService;
	private TsmpSettingService tsmpSettingService;

	public DgrcRoutingHelper(TsmpApiCacheProxy tsmpApiCacheProxy, TsmpApiRegCacheProxy tsmpApiRegCacheProxy,
			TsmpSettingCacheProxy tsmpSettingCacheProxy, CommForwardProcService commForwardProcService ,DgrMtlsClientCertCacheProxy dgrMtlsClientCertCacheProxy ,TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.tsmpApiRegCacheProxy = tsmpApiRegCacheProxy;
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.commForwardProcService = commForwardProcService;
		this.dgrMtlsClientCertCacheProxy = dgrMtlsClientCertCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
	}
	private DgrMtlsClientCertCacheProxy dgrMtlsClientCertCacheProxy;

	public TsmpApiReg calculateRoute(String reqUrl) {

		//參考001_dgrc_工作方式_流程_限制_說明.txt
		
		if (UrlCodecHelper.hasUrlEncoded(reqUrl)) {
			try {
				reqUrl = UrlCodecHelper.getDecode(reqUrl);
			} catch (Exception e) {
				logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
		
		String keyword = "dgrc";
		int flag = reqUrl.indexOf(keyword);
		String requestURI = reqUrl.substring(flag + keyword.length());

		List<TsmpApi> tsmpApiAllList = getTsmpApiCacheProxy().queryAll_APIkeyAndModuleNameAndSrcUrl();

		TsmpApi targetTsmpApi = null;
		TreeMap<Integer, TsmpApi> conformApiList_phase1 = new TreeMap<Integer, TsmpApi>();


		for (TsmpApi tsmpApiTmp : tsmpApiAllList) {
			
			String proxyPath = "";
			
			proxyPath = tsmpApiTmp.getApiKey();			
			
			int point = 0;
			String[] requestURIArr = endWithSalsh(requestURI);
			String[]  proxyPathArr = endWithSalsh(proxyPath);
//			String[] requestURIArr = requestURI.split("/");
//			String[] proxyPathArr = proxyPath.split("/");
			
			
			if (requestURIArr.length == proxyPathArr.length) {
				
				for (int i = 0; i < proxyPathArr.length; i++) {
					String requestURIArrPath = requestURIArr[i];
					String decodeProxyPathArrPath = proxyPathArr[i];
					if (!(requestURIArrPath.equals(decodeProxyPathArrPath) || "{p}".equals(decodeProxyPathArrPath))) {
						point = -1;
						break;
					} else {
						if (requestURIArrPath.equals(decodeProxyPathArrPath)) {
							point = point + 1;
						}
					}
				}
				if (point >= 0) {
					if (conformApiList_phase1.containsKey(point) == false) {
						conformApiList_phase1.put(point, tsmpApiTmp);
					}
				}
			}
		}
		
		if (conformApiList_phase1.isEmpty()==false) {
			targetTsmpApi = conformApiList_phase1.get(conformApiList_phase1.lastKey());
		}

		if (targetTsmpApi == null) {
			return null;
		}

		TsmpApiReg apiReg = null;
		TsmpApiRegId apiRegId = new TsmpApiRegId(targetTsmpApi.getApiKey(), targetTsmpApi.getModuleName());
		Optional<TsmpApiReg> opt_apiReg = getTsmpApiRegCacheProxy().findById(apiRegId);
		if (!opt_apiReg.isPresent()) {
			return null;
		} else {
			apiReg = opt_apiReg.get();
		}

		return apiReg;
	}
	
	private String[] endWithSalsh(String uri) {
		Pattern pattern = Pattern.compile("/");
		if (uri==null) { uri=""; }
		Matcher matcher = pattern.matcher(uri); // 不接受 null 
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		String[] addSlash = new String[count + 1];
		String[] arr = uri.split("/");
		if (uri.endsWith("/")) {

			System.arraycopy(arr, 0, addSlash, 0, arr.length);
			addSlash[addSlash.length - 1] = "/";

			return addSlash;
		} else {
			return arr;
		}
	}

	public List<String> getRouteSrcUrl(TsmpApiReg apiReg, String reqUrl, HttpServletRequest httpReq) throws Exception {
		List<String> routeSrcUrlList = null;
		
		String keyword = "dgrc";
		int flag = reqUrl.indexOf(keyword);
		String uri = reqUrl.substring(flag + keyword.length());
		
		TsmpApiId apiId = new TsmpApiId(apiReg.getApiKey(), apiReg.getModuleName());
		Optional<TsmpApi> tsmpApi = getTsmpApiCacheProxy().findById(apiId);
		if (tsmpApi.isEmpty()) {
			return null;
		}

		routeSrcUrlList = new ArrayList<String>();
		String apiSrc = tsmpApi.get().getApiSrc();
		if ("C".equals(apiSrc)) {// 組合
			String srcUrl = getComposerSrcUrl(tsmpApi.get());
			routeSrcUrlList.add(srcUrl);
			
		} else {
			List<String> srcUrlList = getSrcuUrlList(httpReq, apiReg);
			if(CollectionUtils.isEmpty(srcUrlList)) {
				return null;
			}
			for (String srcUrl : srcUrlList) {
				if (srcUrl.indexOf("{") != -1) {
					String[] uriArr = uri.split("/");
					String[] proxyPathArr = tsmpApi.get().getApiKey().split("/");
					List<String> pList = new ArrayList<String>();
					for (int i = 0; i < proxyPathArr.length; i++) {
						if ("{p}".equals(proxyPathArr[i])) {
							pList.add(uriArr[i]);
						}
					}
					
					for (String p : pList) {
						srcUrl = srcUrl.replaceFirst("\\{p\\}", p);
					}
				}
				routeSrcUrlList.add(srcUrl);
			}
		}

		return routeSrcUrlList;
	}
	
	/**
	 * 依設定是否依Source IP分流, <br>
	 * 取得要打的目標URL順序清單 <br>
	 * IP, CIDR, FQDN <br>
	 */
	private List<String> getSrcuUrlList(HttpServletRequest httpReq, TsmpApiReg apiReg) {
		List<String> sortSrcUrlList = null;
		String ip = ServiceUtil.getIpAddress(httpReq);
		String dn = ServiceUtil.getFQDN(httpReq);
		HashMap<Integer, String> srcUrlMap = new HashMap<>();
		srcUrlMap.put(1, apiReg.getIpSrcUrl1());
		srcUrlMap.put(2, apiReg.getIpSrcUrl2());
		srcUrlMap.put(3, apiReg.getIpSrcUrl3());
		srcUrlMap.put(4, apiReg.getIpSrcUrl4());
		srcUrlMap.put(5, apiReg.getIpSrcUrl5());

		String isRedirectByIp = apiReg.getRedirectByIp();// 是否依Source IP分流
		if (StringUtils.hasLength(isRedirectByIp) && isRedirectByIp.equals("Y")) {
			// 依Source IP分流
			int opt = switchPath(ip, dn, apiReg);
			logger.debug("path :　" + opt);			
			if (StringUtils.hasLength(srcUrlMap.get(opt))) {
				sortSrcUrlList = getCommForwardProcService().getSortSrcUrlListForLB(srcUrlMap.get(opt));
			} else {
				TPILogger.tl.error("Missing srcUrl information. ip/fqdn= " + ip + "/" + dn);
				return null;
			}

		} else {
			//取得目標URL
			sortSrcUrlList = getCommForwardProcService().getSortSrcUrlListForLB(apiReg.getSrcUrl());
		}

		return sortSrcUrlList;
	}

	/**
	 * 根據來源IP,比對是屬於哪一個分流的區塊,共1~5 <br>
	 * 若沒有匹配的,則回覆0 <br>
	 */
	private int switchPath(String ip, String dn, TsmpApiReg apiReg) {
		TPILogger.tl.debug("IP : " + ip + "FQDN : " + dn);
		String ipForRedirect1 = apiReg.getIpForRedirect1();
		if (StringUtils.hasLength(ipForRedirect1)) {
			Set<String> set = Arrays.stream(ipForRedirect1.split(","))
					.collect(Collectors.toSet());
			if (ServiceUtil.isIpInWhitelist(ip, set) || (StringUtils.hasLength(dn) && ServiceUtil.isIpInWhitelist(dn, set))) {
				return 1;
			}
		}
		String ipForRedirect2 = apiReg.getIpForRedirect2();
		if (StringUtils.hasLength(ipForRedirect2)) {
			Set<String> set = Arrays.stream(ipForRedirect2.split(","))
					.collect(Collectors.toSet());
			if (ServiceUtil.isIpInWhitelist(ip, set) || (StringUtils.hasLength(dn) && ServiceUtil.isIpInWhitelist(dn, set))) {
				return 2;
			}
		}
		String ipForRedirect3 = apiReg.getIpForRedirect3();
		if (StringUtils.hasLength(ipForRedirect3)) {
			Set<String> set = Arrays.stream(ipForRedirect3.split(","))
					.collect(Collectors.toSet());
			if (ServiceUtil.isIpInWhitelist(ip, set) || (StringUtils.hasLength(dn) && ServiceUtil.isIpInWhitelist(dn, set))) {
				return 3;
			}
		}
		String ipForRedirect4 = apiReg.getIpForRedirect4();
		if (StringUtils.hasLength(ipForRedirect4)) {
			Set<String> set = Arrays.stream(ipForRedirect4.split(","))
					.collect(Collectors.toSet());
			if (ServiceUtil.isIpInWhitelist(ip, set) || (StringUtils.hasLength(dn) && ServiceUtil.isIpInWhitelist(dn, set))) {
				return 4;
			}
		}
		String ipForRedirect5 = apiReg.getIpForRedirect5();
		if (StringUtils.hasLength(ipForRedirect5)) {
			Set<String> set = Arrays.stream(ipForRedirect5.split(","))
					.collect(Collectors.toSet());
			if (ServiceUtil.isIpInWhitelist(ip, set) || (StringUtils.hasLength(dn) && ServiceUtil.isIpInWhitelist(dn, set))) {
				return 5;
			}
		}
		return 0;
	}
	
	/**
	 * 沒有目標URL資料時,要回覆的錯誤訊息
	 */
	public ResponseEntity<?> getSrcUrlListErrResp(HttpServletRequest httpReq, String apiId) {
		String ip = ServiceUtil.getIpAddress(httpReq);
		String dn = ServiceUtil.getFQDN(httpReq);
		OAuthTokenErrorResp resp = new OAuthTokenErrorResp();
		resp.setTimestamp(System.currentTimeMillis() + "");
		resp.setStatus(HttpStatus.UNAUTHORIZED.value());// 401
		resp.setError(TokenHelper.UNAUTHORIZED);
		resp.setMessage("Missing srcUrl information. ip/fqdn= " + ip + "/" + dn);
		resp.setPath(apiId);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
	}

	public String getComposerSrcUrl(TsmpApi tsmpApi) {
		Optional<TsmpSetting> tsmpSetting =  getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.TSMP_COMPOSER_ADDRESS);
		if (tsmpSetting.isEmpty()) {
			return null;
		}
		String[] tsmpSettingValue = tsmpSetting.get().getValue().split(",");
		String srcUrl = tsmpApi.getSrcUrl();
		srcUrl = tsmpSettingValue[0] + srcUrl;
		return srcUrl;
	}

	private ConcurrentHashMap<String, String> decryptedKeyMimaCache = new ConcurrentHashMap<>();
	private static final int MAX_CACHE_SIZE = 1000;
	/***
	 * 取得mtls憑證資訊
	 * Get mTLS certificate information
	 * @param host
	 * @param port
	 * @return
	 */
	public Pair<Boolean, CertificateInfo> getMtlsInfo(String host, Integer port) {
		boolean isMtls = false;
//		DgrMtlsClientCert dgrMtlsClientCert = mtlsProxyKeeper.findByHostAndPortAndEnable(host, port, "Y");
		DgrMtlsClientCert dgrMtlsClientCert = getDgrMtlsClientCertCacheProxy().findByHostAndPortAndEnable(host, port, "Y");

		var certBuilder = CertificateInfo.builder();
		if (dgrMtlsClientCert != null) {
			isMtls = true;
			String keyMima = dgrMtlsClientCert.getKeyMima();
			String decryptedKeyMima = decryptedKeyMimaCache.get(keyMima);
			if (decryptedKeyMima == null) {
				try {
					// ENC decrypt
					decryptedKeyMima = getTsmpSettingService().getENCPlainVal(keyMima);
					decryptedKeyMimaCache.put(keyMima, decryptedKeyMima);
				} catch (Exception e) {
					throw TsmpDpAaRtnCode._1434.throwing("{{keyMima}}");
				}
			}
			certBuilder
					.rootCA(dgrMtlsClientCert.getRootCa())
					.clientCert(dgrMtlsClientCert.getClientCert())
					.clientKey(dgrMtlsClientCert.getClientKey())
					.keyMima(decryptedKeyMima);
			evictLRUEntries();
		}
		return new Pair<>(isMtls, certBuilder.build());
	}
	private void evictLRUEntries() {
		// 刪除最少使用的以保持緩存大小
		// Delete the least recently used to maintain cache size
		if (decryptedKeyMimaCache.size() > MAX_CACHE_SIZE) {
			Iterator<Map.Entry<String, String>> iterator = decryptedKeyMimaCache.entrySet().iterator();
			while (iterator.hasNext() && decryptedKeyMimaCache.size() > MAX_CACHE_SIZE) {
				Map.Entry<String, String> entry = iterator.next();
				iterator.remove();
			}
		}
	}

	
	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected TsmpApiRegCacheProxy getTsmpApiRegCacheProxy() {
		return tsmpApiRegCacheProxy;
	}

	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return tsmpSettingCacheProxy;
	}

	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrMtlsClientCertCacheProxy getDgrMtlsClientCertCacheProxy() {
		return dgrMtlsClientCertCacheProxy;
	}
}
