package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DigiRunnerTokenResp {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonProperty("scope")
	private String scope;

	@JsonProperty("org_id")
	private String orgId;

	@JsonProperty("node")
	private String node;

	@JsonProperty("stime")
	private Long stime;

	@JsonProperty("jti")
	private String jti;

	public DigiRunnerTokenResp() {}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Long getStime() {
		return stime;
	}

	public void setStime(Long stime) {
		this.stime = stime;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	@Override
	public String toString() {
		return "DigiRunnerTokenResp [accessToken=" + accessToken + ", tokenType=" + tokenType + ", refreshToken="
				+ refreshToken + ", expiresIn=" + expiresIn + ", scope=" + scope + ", orgId=" + orgId + ", node=" + node
				+ ", stime=" + stime + ", jti=" + jti + "]";
	}

}