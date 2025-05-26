package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GtwIdPWellKnownResp {
	@JsonProperty("issuer")
	private String issuer;

	@JsonProperty("authorization_endpoint")
	private String authorizationEndpoint;

	@JsonProperty("token_endpoint")
	private String tokenEndpoint;
	
	@JsonProperty("userinfo_endpoint")
	private String userinfoEndpoint;

	@JsonProperty("jwks_uri")
	private String jwksUri;

	@JsonProperty("id_token_signing_alg_values_supported")
	private List<String> idTokenSigningAlgValuesSupported;

	@JsonProperty("scopes_supported")
	private List<String> scopesSupported;

	@JsonProperty("callback_endpoint")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String callbackEndpoint;

	@JsonProperty("code_challenge_methods_supported")
	private List<String> codeChallengeMethodsSupported;

	@Override
	public String toString() {
		return "GtwIdPWellKnownResp [issuer=" + issuer + ", authorizationEndpoint=" + authorizationEndpoint
				+ ", tokenEndpoint=" + tokenEndpoint + ", userinfoEndpoint=" + userinfoEndpoint + ", jwksUri=" + jwksUri
				+ ", idTokenSigningAlgValuesSupported=" + idTokenSigningAlgValuesSupported + ", scopesSupported="
				+ scopesSupported + ", callbackEndpoint=" + callbackEndpoint + ", codeChallengeMethodsSupported="
				+ codeChallengeMethodsSupported + "]";
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	public void setAuthorizationEndpoint(String authorizationEndpoint) {
		this.authorizationEndpoint = authorizationEndpoint;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}
	
	public String getUserinfoEndpoint() {
		return userinfoEndpoint;
	}

	public void setUserinfoEndpoint(String userinfoEndpoint) {
		this.userinfoEndpoint = userinfoEndpoint;
	}

	public String getJwksUri() {
		return jwksUri;
	}

	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	public List<String> getIdTokenSigningAlgValuesSupported() {
		return idTokenSigningAlgValuesSupported;
	}

	public void setIdTokenSigningAlgValuesSupported(List<String> idTokenSigningAlgValuesSupported) {
		this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
	}

	public List<String> getScopesSupported() {
		return scopesSupported;
	}

	public void setScopesSupported(List<String> scopesSupported) {
		this.scopesSupported = scopesSupported;
	}

	public String getCallbackEndpoint() {
		return callbackEndpoint;
	}

	public void setCallbackEndpoint(String callbackEndpoint) {
		this.callbackEndpoint = callbackEndpoint;
	}

	public List<String> getCodeChallengeMethodsSupported() {
		return codeChallengeMethodsSupported;
	}

	public void setCodeChallengeMethodsSupported(List<String> codeChallengeMethodsSupported) {
		this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
	}
}
