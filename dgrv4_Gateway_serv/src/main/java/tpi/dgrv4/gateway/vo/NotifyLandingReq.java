package tpi.dgrv4.gateway.vo;

import tpi.dgrv4.gateway.service.OAuthTokenService;

public class NotifyLandingReq {

	private OAuthTokenService.OAuthTokenData oauthTokenData;
	private boolean isHasRefreshToken;
	private String userName;
	private String clientId;
	private String accessTokenJti;
	private String refreshTokenJti;
	private String scopeStr;
	private Long stime;
	private Long accessTokenExp;
	private Long refreshTokenExp;
	private String grantType;
	private String oldAccessTokenJti;
	private String idPType;
	private String idTokenJwt;
	private String refreshTokenJwtstr;

	public OAuthTokenService.OAuthTokenData getOauthTokenData() {
		return oauthTokenData;
	}

	public void setOauthTokenData(OAuthTokenService.OAuthTokenData oauthTokenData) {
		this.oauthTokenData = oauthTokenData;
	}

	public boolean isHasRefreshToken() {
		return isHasRefreshToken;
	}

	public void setHasRefreshToken(boolean isHasRefreshToken) {
		this.isHasRefreshToken = isHasRefreshToken;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getAccessTokenJti() {
		return accessTokenJti;
	}

	public void setAccessTokenJti(String accessTokenJti) {
		this.accessTokenJti = accessTokenJti;
	}

	public String getRefreshTokenJti() {
		return refreshTokenJti;
	}

	public void setRefreshTokenJti(String refreshTokenJti) {
		this.refreshTokenJti = refreshTokenJti;
	}

	public String getScopeStr() {
		return scopeStr;
	}

	public void setScopeStr(String scopeStr) {
		this.scopeStr = scopeStr;
	}

	public Long getStime() {
		return stime;
	}

	public void setStime(Long stime) {
		this.stime = stime;
	}

	public Long getAccessTokenExp() {
		return accessTokenExp;
	}

	public void setAccessTokenExp(Long accessTokenExp) {
		this.accessTokenExp = accessTokenExp;
	}

	public Long getRefreshTokenExp() {
		return refreshTokenExp;
	}

	public void setRefreshTokenExp(Long refreshTokenExp) {
		this.refreshTokenExp = refreshTokenExp;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getOldAccessTokenJti() {
		return oldAccessTokenJti;
	}

	public void setOldAccessTokenJti(String oldAccessTokenJti) {
		this.oldAccessTokenJti = oldAccessTokenJti;
	}

	public String getIdPType() {
		return idPType;
	}

	public void setIdPType(String idPType) {
		this.idPType = idPType;
	}

	public String getIdTokenJwt() {
		return idTokenJwt;
	}

	public void setIdTokenJwt(String idTokenJwt) {
		this.idTokenJwt = idTokenJwt;
	}

	public String getRefreshTokenJwtstr() {
		return refreshTokenJwtstr;
	}

	public void setRefreshTokenJwtstr(String refreshTokenJwtstr) {
		this.refreshTokenJwtstr = refreshTokenJwtstr;
	}

}
