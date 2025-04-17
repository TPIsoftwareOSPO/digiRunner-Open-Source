package tpi.dgrv4.dpaa.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.util.UdpSsoUtil;
import tpi.dgrv4.dpaa.vo.UdpEnvItem;
import tpi.dgrv4.dpaa.vo.UdpLdapReq;
import tpi.dgrv4.dpaa.vo.UdpLdapResp;
import tpi.dgrv4.dpaa.vo.UdpLoginResp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class UdpLoginService {
	private TPILogger logger = TPILogger.tl;
	
	private UdpLdapService udpLdapService;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	@Autowired
	public UdpLoginService(UdpLdapService udpLdapService, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.udpLdapService = udpLdapService;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public UdpLoginResp udpLogin(HttpHeaders headers, HttpServletRequest httpReq, HttpServletResponse httpRes, ReqHeader reqHeader) throws Exception {
		UdpLoginResp resp = null;
		
		String scheme = httpReq.getScheme();
		
		Map<String, String> parameters = new HashMap<>();
		httpReq.getParameterMap().forEach((k, vs) -> {
			if (vs.length != 0) {
				parameters.put(k, vs[0]);
			}
		});
		
		String locale = reqHeader.getLocale();
		String userIp = StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr() : headers.getFirst("x-forwarded-for");
		resp = udpLogin(parameters, scheme, httpReq, httpRes, locale, userIp);
		return resp;
	}
	
	protected UdpLoginResp udpLogin(Map<String, String> parameters, String scheme, HttpServletRequest httpReq, 
			HttpServletResponse httpRes, String locale, String userIp) throws Exception {
		checkParmas(parameters);
		
		String userName = parameters.get("username");
		if("manager".equals(userName)) {
			throw TsmpDpAaRtnCode._1518.throwing();
		}
		
		doLdap(userIp, parameters, httpReq);
		
		String codeVerifier = parameters.get("codeVerifier");
		String userMail = parameters.get("userMail");
		String udpLoginUrl = parameters.get("udpLoginUrl");
		userName = userName.replace(" ", "_");//將 userName 的空白取代為底線 "_"
		String userNameEncode = ServiceUtil.getAdd0x80ToBase64Encoede(userName);//取得後面有加特殊字0x80,再經 Base64Encode 的  User Name
		
		Map<String, String> ssoParam= new HashMap<>();
		ssoParam.put("grant_type", "password");
		ssoParam.put("username", userNameEncode);
		ssoParam.put("codeVerifier", codeVerifier);
		ssoParam.put("userMail", userMail);
		ssoParam.put("udpLoginUrl", udpLoginUrl);
		ssoParam.put("sessionKeyEn", null);//若 sessionKeyEn 沒有值, 設定為 null
		
		ObjectMapper mapper = new ObjectMapper();
		String dataStr = mapper.writeValueAsString(ssoParam);// 轉成json字串
		String dataStr_en = Base64Util.base64URLEncode(dataStr.getBytes());

		UdpLoginResp resp = getResp(userIp, dataStr_en, locale);
		return resp;
	}
	
	public void checkParmas(Map<String, String> parameters)  throws Exception{
		String userName = parameters.get("username");
		String pwd = parameters.get("password");
		String codeVerifier = parameters.get("codeVerifier");
		String udpLoginUrl = parameters.get("udpLoginUrl");
		
		if (!StringUtils.hasLength(userName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
 
		if (!StringUtils.hasLength(pwd)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if (!StringUtils.hasLength(codeVerifier)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if (!StringUtils.hasLength(udpLoginUrl)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
	
	private UdpLoginResp getResp(String userIp, String paramStr_en, String locale) {
		this.logger.debug(String.format("userIp: %s", userIp));
		UdpLoginResp resp = new UdpLoginResp();
		
		String itemNo = "UDPSSO";
		List<TsmpDpItems> itemList = getTsmpDpItems(itemNo, locale);
		
		List<UdpEnvItem> dataList = UdpSsoUtil.getEnvList(itemList, itemNo, userIp, paramStr_en);
		resp.setDataList(dataList);
		
		return resp;
	}
	
	private List<TsmpDpItems> getTsmpDpItems(String itemNo, String locale) {
		List<TsmpDpItems> itemList = getTsmpDpItemsCacheProxy().findByItemNoAndLocale(itemNo, locale);
		if (itemList == null || itemList.size() == 0) {
			this.logger.debug(String.format("未設定 itemNo 的值: %s", itemNo));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return itemList;
	}
	
	protected void doLdap(String userIp, Map<String, String> parameters, HttpServletRequest httpReq) {
		String userName = parameters.get("username");
		String pwd = parameters.get("password");
		String codeVerifier	 = parameters.get("codeVerifier");
		String codeChallenge = ServiceUtil.getSHA256ToBase64UrlEncodeWithoutPadding(codeVerifier);
		pwd = new String(ServiceUtil.base64Decode(pwd));//須做 Base64Decode ,才是 LDAP 接受的值
		
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(userName);	
		
		UdpLdapReq ldapReq = new UdpLdapReq();
		ldapReq.setUserName(userName);
		ldapReq.setPwd(pwd);
		ldapReq.setCodeChallenge(codeChallenge);
		
		String scheme = httpReq.getScheme() + "://";
	    String serverName = "127.0.0.1";
	    String serverPort = httpReq.getServerPort() == 80 ? "" : ":" + httpReq.getServerPort();
	    String contextPath = httpReq.getContextPath();
	    String localBaseUrl = scheme + serverName + serverPort + contextPath;
	    
		UdpLdapResp ldapResp = udpLdapService.udpCheckAccountByLdap(userIp, httpReq, auth, ldapReq, localBaseUrl);//呼叫 LDAP
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}