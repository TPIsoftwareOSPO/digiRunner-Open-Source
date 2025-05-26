package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GtwIdPVerifyResp {
	@JsonProperty("iss")
	private String iss;

	@JsonProperty("sub")
	private String sub;

	@JsonProperty("aud")
	private String aud;

	@JsonProperty("exp")
	private Long exp;

	@JsonProperty("iat")
	private Long iat;

	@JsonProperty("name")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String name;

	@JsonProperty("email")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String email;

	@JsonProperty("picture")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String picture;

	@JsonProperty("lightId")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String lightId;

	@JsonProperty("roleName")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String roleName;

	@JsonProperty("base64url_orig_profile")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String base64urlOrigProfile;

	@Override
	public String toString() {
		return "GtwIdPVerifyResp [iss=" + iss + ", sub=" + sub + ", aud=" + aud + ", exp=" + exp + ", iat=" + iat
				+ ", name=" + name + ", email=" + email + ", picture=" + picture + ", lightId=" + lightId
				+ ", roleName=" + roleName + ", base64urlOrigProfile=" + base64urlOrigProfile + "]";
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getLightId() {
		return lightId;
	}

	public void setLightId(String lightId) {
		this.lightId = lightId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getBase64urlOrigProfile() {
		return base64urlOrigProfile;
	}

	public void setBase64urlOrigProfile(String base64urlOrigProfile) {
		this.base64urlOrigProfile = base64urlOrigProfile;
	}
}
