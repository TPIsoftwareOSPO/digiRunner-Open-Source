package tpi.dgrv4.gateway.vo;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.dpaa.component.DpaaStaticResourceInitializer;
import tpi.dgrv4.dpaa.service.TsmpAuthorizationService;
import tpi.dgrv4.gateway.component.StaticResourceInitializer;
import tpi.dgrv4.gateway.service.TsmpSettingService;

/** 反轉 Authorization 字串後的物件 
 * tsmpaa 的 token 打 v3 module ----------> 沒stime,走舊的group api檢查
 * tsmpu 的 token 打 v3 module ----------->有stime進 次數天數檢查器邏輯
 * 以V2發的Token, V3也相容。但目前只有tsmpac會用tsmpaa的Token
 */
public class TsmpAuthorization {
	
	/** Injected by {@link DpaaStaticResourceInitializer} and {@link StaticResourceInitializer} */
	private static TsmpSettingService tsmpSettingService;
	private static TsmpAuthorizationService tsmpAuthorizationService;

	@JsonProperty("alg")
	private String alg;	// "RS256"

	@JsonProperty("typ")
	private String typ;	// "JWT"

	@JsonProperty("node")
	private String node;	// TSMP_NODE

	@JsonProperty("aud")
	private List<String> aud;	// 將 Client Id 經過 Base64 演算後放入 List
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("user_name")
	private String userName;

	@JsonProperty("org_id")
	private String orgId;

	@JsonProperty("scope")
	private List<String> scope;	// ["select"]

	@JsonProperty("stime")
	private Long stime;

	@JsonProperty("exp")
	private Long exp;

	@JsonProperty("authorities")
	private List<String> authorities;	// ["1000"]

	@JsonProperty("jti")
	private String jti;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("token_string")
	private String tokenString;
	
	@JsonProperty("idp_type")
	private String idpType;

	public TsmpAuthorization() {}

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public List<String> getAud() {
		return aud;
	}

	public void setAud(List<String> aud) {
		this.aud = aud;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/*
	 * 2022/5/18 增加, 若 CLIENT_CREDENTIALS_DEFAULT_USERNAME 的值為 true, 當 token 取不到
	 * User Name 時, 回傳 user_name 為 client id, 這是為了避免[渣打]用 client_credentials 取得的
	 * token, 沒有 userName, 再去打 digiRunner 的 API,有許多程式會用到 authorization.getUserName()
	 * 時的錯誤, 也避免 createUser 和 updateUser 沒有值.
	 */
	private void setUserName_forClientCredentials() {
		if (tsmpSettingService != null) {
			boolean isDefaultUser = tsmpSettingService.getVal_CLIENT_CREDENTIALS_DEFAULT_USERNAME();
			if (isDefaultUser && !StringUtils.hasLength(userName)) {
				userName = clientId;
			}
		}
	}
	
	public String getUserName() {
		
		setUserName_forClientCredentials();
		
		/**
		 * 2022/12/29,
		 * TsmpAuthorization.getUserName() 修改取值方式,
		 * 若開頭有 b64 , 則取出 "b64.GOOGLE.(李OO Mini Lee).101872102234493560934",
		 * 第三段的值 反b64 回來,
		 * 存入第二+三段= "GOOGLE.李OO Mini Lee",
		 * 若成功預期所有的API程式碼要寫入 createUser / updateUser 時,都可以正確寫入 id_token 裡的值
		 */
		
		if(StringUtils.hasText(userName)) {
			if(userName.toLowerCase().indexOf("b64.") == 0) {
				String[] arrUserName = userName.split("\\.");
				if(arrUserName.length >= 4) {
					byte[] arrName = Base64Util.base64URLDecode(arrUserName[2]);
					String strName = new String(arrName, StandardCharsets.UTF_8);
					return arrUserName[1] + "." + strName;
				}
			}
		}
		
		return userName;
	}
	
	/**
	 * 取得 token 的 userName,
	 * 依不同的登入方式
	 * 
	 * @return userName
	 */
	public String getUserNameForQuery() {
		
		setUserName_forClientCredentials();
		
		return getUserNameForQuery(userName);
	}
	
	/**
	 * 取得 token 的 userName,
	 * 1.若是 "b64." 開頭, 表示為 SSO AC IdP 登入, 
	 *  (1).當 IdP 為 GOOGLE / MS, 則回傳 userName 為 id_token 的 sub
	 *  (2).當 IdP 為 LDAP, 則回傳 userName 為 LDAP 的使用者帳號
	 * 2.若不是 "b64." 開頭, 表示為直接登入 AC , 回傳 userName
	 * 
	 * @return userName
	 */
	public static String getUserNameForQuery(String userName) {
		if(!StringUtils.hasLength(userName)) {
			return null;
		}
		
		int flag = userName.toLowerCase().indexOf("b64.");//轉小寫,再比較(忽略大小寫)
		if (flag == -1) {// 不是 "b64." 開頭, 表示為直接登入 AC 
			return userName;
			
		}else {
			// 若是 "b64." 開頭, 表示為以 AC IdP 登入
			// 例如: "b64.GOOGLE.base64URLEncode(李OO Mini Lee).101872102234493560934"
			// 例如: "b64.LDAP.bWluaWxkYXA.minildap"
			String[] userNameArr = userName.split("\\.");
			
			// 回傳 sub, 
			// 例如: 101872102234493560934
			// 例如: minildap
			return userNameArr[3];
		}
	}
	
	public String getIdTokenJwtstr() {
		if(tsmpAuthorizationService != null) {
		    return tsmpAuthorizationService.getIdTokenJwtstr(userName, idpType);
		}else {
			return null;
		}
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public List<String> getScope() {
		return scope;
	}

	public void setScope(List<String> scope) {
		this.scope = scope;
	}

	public Long getStime() {
		return stime;
	}

	public void setStime(Long stime) {
		this.stime = stime;
	}

	public Long getExp() {
		return exp;
	}

	public void setExp(Long exp) {
		this.exp = exp;
	}

	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTokenString() {
		return tokenString;
	}

	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
	
	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}
	
	@Override
	public String toString() {
		return "TsmpAuthorization [alg=" + alg + ", typ=" + typ + ", node=" + node + ", aud=" + aud + ", userId=" + userId + ", userName=" + userName
				+ ", orgId=" + orgId + ", scope=" + scope + ", stime=" + stime + ", exp=" + exp + ", authorities=" + authorities + ", jti=" + jti
				+ ", clientId=" + clientId + ", tokenString=" + tokenString + ", idpType=" + idpType + "]";
	}

	public static void setTsmpSettingService(TsmpSettingService tsmpSettingService) {
		TsmpAuthorization.tsmpSettingService = tsmpSettingService;
	}

	public static void setTsmpAuthorizationService(TsmpAuthorizationService tsmpAuthorizationService) {
		TsmpAuthorization.tsmpAuthorizationService = tsmpAuthorizationService;
	}

}
