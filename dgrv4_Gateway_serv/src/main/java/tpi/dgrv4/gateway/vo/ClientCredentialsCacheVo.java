package tpi.dgrv4.gateway.vo;

public class ClientCredentialsCacheVo {
	private long cacheExpTimestamp;// 快取到期時間
	private long accessTokenValidity;// Access token 授權時間, 單位: 分
	private OAuthTokenResp oAuthTokenResp = new OAuthTokenResp();
	private String scopeList;// Group ID
	private boolean isJwe;// token 是否為 JWE
	private long accessTokenExp = 0L;// Access token 到期時間, 單位秒,10碼 (記錄的是"第一次"放入 cache 的 Access token 到期時間)

	@Override
	public String toString() {
		return "ClientCredentialsCacheVo [cacheExpTimestamp=" + cacheExpTimestamp + ", accessTokenValidity="
				+ accessTokenValidity + ", oAuthTokenResp=" + oAuthTokenResp + ", scopeList=" + scopeList + ", isJwe="
				+ isJwe + ", accessTokenExp=" + accessTokenExp + "]";
	}

	public long getCacheExpTimestamp() {
		return cacheExpTimestamp;
	}

	public void setCacheExpTimestamp(long cacheExpTimestamp) {
		this.cacheExpTimestamp = cacheExpTimestamp;
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

	public long getAccessTokenExp() {
		return accessTokenExp;
	}

	public void setAccessTokenExp(long accessTokenExp) {
		this.accessTokenExp = accessTokenExp;
	}
}
