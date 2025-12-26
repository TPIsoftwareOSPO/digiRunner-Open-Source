package tpi.dgrv4.dpaa.vo;

import java.util.Set;

public class AA0219Resp {
	
	private String clientID;
	
	private Set<String> authorizedGrantType;
	
	private String webServerRedirectUri;
	
	private Long accessTokenValidity;
	
	private Long raccessTokenValidity;
	
	private Integer accessTokenQuota;
	
	private Integer refreshTokenQuota;
	
	private String webServerRedirectUri1;
	
	private String webServerRedirectUri2;
	
	private String webServerRedirectUri3;
	
	private String webServerRedirectUri4;
	
	private String webServerRedirectUri5;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public Set<String> getAuthorizedGrantType() {
		return authorizedGrantType;
	}

	public void setAuthorizedGrantType(Set<String> authorizedGrantType) {
		this.authorizedGrantType = authorizedGrantType;
	}

	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public Long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Long getRaccessTokenValidity() {
		return raccessTokenValidity;
	}

	public void setRaccessTokenValidity(Long raccessTokenValidity) {
		this.raccessTokenValidity = raccessTokenValidity;
	}

	public Integer getAccessTokenQuota() {
		return accessTokenQuota;
	}

	public void setAccessTokenQuota(Integer accessTokenQuota) {
		this.accessTokenQuota = accessTokenQuota;
	}

	public Integer getRefreshTokenQuota() {
		return refreshTokenQuota;
	}

	public void setRefreshTokenQuota(Integer refreshTokenQuota) {
		this.refreshTokenQuota = refreshTokenQuota;
	}

	public String getWebServerRedirectUri1() {
		return webServerRedirectUri1;
	}

	public void setWebServerRedirectUri1(String webServerRedirectUri1) {
		this.webServerRedirectUri1 = webServerRedirectUri1;
	}

	public String getWebServerRedirectUri2() {
		return webServerRedirectUri2;
	}

	public void setWebServerRedirectUri2(String webServerRedirectUri2) {
		this.webServerRedirectUri2 = webServerRedirectUri2;
	}

	public String getWebServerRedirectUri3() {
		return webServerRedirectUri3;
	}

	public void setWebServerRedirectUri3(String webServerRedirectUri3) {
		this.webServerRedirectUri3 = webServerRedirectUri3;
	}

	public String getWebServerRedirectUri4() {
		return webServerRedirectUri4;
	}

	public void setWebServerRedirectUri4(String webServerRedirectUri4) {
		this.webServerRedirectUri4 = webServerRedirectUri4;
	}

	public String getWebServerRedirectUri5() {
		return webServerRedirectUri5;
	}

	public void setWebServerRedirectUri5(String webServerRedirectUri5) {
		this.webServerRedirectUri5 = webServerRedirectUri5;
	}
}
