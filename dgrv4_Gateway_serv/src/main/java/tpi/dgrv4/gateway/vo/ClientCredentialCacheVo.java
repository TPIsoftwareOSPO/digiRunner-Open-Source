package tpi.dgrv4.gateway.vo;

public class ClientCredentialCacheVo {
	private long expTimestamp;// 快取到期時間
	private long accessTokenValidity;// Access token 授權時間, 單位: 分
	private OAuthTokenResp oAuthTokenResp = new OAuthTokenResp();
	private String scopeList;// Group ID
	private boolean isJwe;// token 是否為 JWE

	@Override
	public String toString() {
		return "ClientCredentialCacheVo [expTimestamp=" + expTimestamp + ", accessTokenValidity=" + accessTokenValidity
				+ ", oAuthTokenResp=" + oAuthTokenResp + ", scopeList=" + scopeList + ", isJwe=" + isJwe + "]";
	}

	public long getExpTimestamp() {
		return expTimestamp;
	}

	public void setExpTimestamp(long expTimestamp) {
		this.expTimestamp = expTimestamp;
	}

	public long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public OAuthTokenResp getoAuthTokenResp() {
		return oAuthTokenResp;
	}

	public void setoAuthTokenResp(OAuthTokenResp oAuthTokenResp) {
		this.oAuthTokenResp = oAuthTokenResp;
	}

	public String getScopeList() {
		return scopeList;
	}

	public void setScopeList(String scopeList) {
		this.scopeList = scopeList;
	}

	public boolean isJwe() {
		return isJwe;
	}

	public void setJwe(boolean isJwe) {
		this.isJwe = isJwe;
	}
}
