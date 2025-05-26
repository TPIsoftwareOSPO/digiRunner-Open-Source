package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthIntrospectionResp {
	/** 
	 * Introspection response 用，
	 * 代表是否由該 authorization server 所發且還沒有被 resource owner 廢止，
	 * 並還在有效時間內，回 true or false 
	 */
	@JsonProperty("active")
	private boolean active;
	
	/** 代表 access token 可存取的資源範圍 */
	@JsonProperty("scope")
	private String scope;
	
	/** OAuth 裡使用 client_id */
	@JsonProperty("client_id")
	private String clientId;
			
	/** 可供人識別是哪位 user 授權了這個token，例如說：網銀上的別名 */
	@JsonProperty("username")
	private String username;
	
	/** 代表是哪個類型的 token，例如： access_token, refresh_token */
	@JsonProperty("token_type")
	private String tokenType;
	
	/** token 的過期時間 */
	@JsonProperty("exp")
	private Long exp;
	
	/** token 的發行時間，指 token 何時由 OAuth Server 發出 */
	@JsonProperty("iat")
	private Long iat;
	
	/** token 的未生效時間，指 token 在什麼時間點前都不會生效 */
	@JsonProperty("nbf")
	private Long nbf;
	
	/** 可供機器識別是哪位 user 授權了這個token，例如說：db 記錄的 uuid */
	@JsonProperty("sub")
	private String sub;
	
	/** 代表是哪個對象要去接受這個 token */
	@JsonProperty("aud")
	private String aud;
	
	/** 代表是哪個 Authorization Server 發行這個 token */
	@JsonProperty("iss")
	private String iss;
	
	/** 代表這個 token 的 unique id，例如：token 儲存在 db 時用的 uuid */
	@JsonProperty("jti")
	private String jti;

	@Override
	public String toString() {
		return "OAuthIntrospectionResp [active=" + active + ", scope=" + scope + ", clientId=" + clientId
				+ ", username=" + username + ", tokenType=" + tokenType + ", exp=" + exp + ", iat=" + iat + ", nbf="
				+ nbf + ", sub=" + sub + ", aud=" + aud + ", iss=" + iss + ", jti=" + jti + "]";
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Long getExp() {
		return exp;
	}

	public void setExp(Long exp) {
		this.exp = exp;
	}

	public Long getIat() {
		return iat;
	}

	public void setIat(Long iat) {
		this.iat = iat;
	}

	public Long getNbf() {
		return nbf;
	}

	public void setNbf(Long nbf) {
		this.nbf = nbf;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}
}
