package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenResp {
 
	@JsonProperty("access_token")
	private String accessToken;
	
	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonProperty("jti")
	private String jti;

	@JsonProperty("node")
	private String node;

	@JsonProperty("org_id")
	@JsonInclude (JsonInclude.Include.NON_NULL)
	private String orgId;

	@JsonProperty("refresh_token")
	@JsonInclude (JsonInclude.Include.NON_NULL)
	private String refreshToken;	
	
	@JsonProperty("scope")
	private String scope;

	@JsonProperty("stime")
	private Long stime;
	
	@JsonProperty("token_type")
	private String tokenType;
	
	@JsonProperty("idp_type")
	@JsonInclude (JsonInclude.Include.NON_NULL)
	private String idpType;
	
	@JsonProperty("id_token")
	@JsonInclude (JsonInclude.Include.NON_NULL)
	private String idToken;

	@Override
	public String toString() {
		return "OAuthTokenResp [accessToken=" + accessToken + ", expiresIn=" + expiresIn + ", jti=" + jti + ", node="
				+ node + ", orgId=" + orgId + ", refreshToken=" + refreshToken + ", scope=" + scope + ", stime=" + stime
				+ ", tokenType=" + tokenType + ", idpType=" + idpType + ", idToken=" + idToken + "]";
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Long getStime() {
		return stime;
	}

	public void setStime(Long stime) {
		this.stime = stime;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
}