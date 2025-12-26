package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_client_details")
public class OauthClientDetails {

	@Id
	@Column(name = "client_id")
	private String clientId;

	@Column(name = "resource_ids")
	private String resourceIds;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "scope")
	private String scope;

	@Column(name = "authorized_grant_types")
	private String authorizedGrantTypes;

	@Column(name = "web_server_redirect_uri")
	private String webServerRedirectUri;

	@Column(name = "authorities")
	private String authorities;

	@Column(name = "access_token_validity")
	private Long accessTokenValidity = 86400L;

	@Column(name = "refresh_token_validity")
	private Long refreshTokenValidity = 86400L;

	@Column(name = "additional_information")
	private String additionalInformation;

	@Column(name = "autoapprove")
	private String autoapprove;
	
	@Column(name = "web_server_redirect_uri1")
	private String webServerRedirectUri1;

	@Column(name = "web_server_redirect_uri2")
	private String webServerRedirectUri2;

	@Column(name = "web_server_redirect_uri3")
	private String webServerRedirectUri3;

	@Column(name = "web_server_redirect_uri4")
	private String webServerRedirectUri4;

	@Column(name = "web_server_redirect_uri5")
	private String webServerRedirectUri5;

	/* constructors */
	public OauthClientDetails() {
	}

	@Override
	public String toString() {
		return "OauthClientDetails [clientId=" + clientId + ", resourceIds=" + resourceIds + ", clientSecret="
				+ clientSecret + ", scope=" + scope + ", authorizedGrantTypes=" + authorizedGrantTypes
				+ ", webServerRedirectUri=" + webServerRedirectUri + ", authorities=" + authorities
				+ ", accessTokenValidity=" + accessTokenValidity + ", refreshTokenValidity=" + refreshTokenValidity
				+ ", additionalInformation=" + additionalInformation + ", autoapprove=" + autoapprove
				+ ", webServerRedirectUri1=" + webServerRedirectUri1 + ", webServerRedirectUri2="
				+ webServerRedirectUri2 + ", webServerRedirectUri3=" + webServerRedirectUri3
				+ ", webServerRedirectUri4=" + webServerRedirectUri4 + ", webServerRedirectUri5="
				+ webServerRedirectUri5 + "]";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAuthorizedGrantTypes() {
		return authorizedGrantTypes;
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public Long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Long getRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(Long refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public String getAutoapprove() {
		return autoapprove;
	}

	public void setAutoapprove(String autoapprove) {
		this.autoapprove = autoapprove;
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
