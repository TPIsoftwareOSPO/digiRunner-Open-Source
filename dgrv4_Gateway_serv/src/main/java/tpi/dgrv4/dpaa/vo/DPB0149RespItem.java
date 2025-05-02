package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0149RespItem {
	private String Id;
	private String idpType;
	private String clientId;
	private String clientMima;
	private String clientName;
	private String clientStatus;
	private String idpWellKnownUrl;
	private String callbackUrl;
	private String authUrl;
	private String accessTokenUrl;
	private String scope;
	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getIdpType() {
		return idpType;
	}
	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientMima() {
		return clientMima;
	}
	public void setClientMima(String clientMima) {
		this.clientMima = clientMima;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientStatus() {
		return clientStatus;
	}
	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}
	public String getIdpWellKnownUrl() {
		return idpWellKnownUrl;
	}
	public void setIdpWellKnownUrl(String idpWellKnownUrl) {
		this.idpWellKnownUrl = idpWellKnownUrl;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getAuthUrl() {
		return authUrl;
	}
	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public Date getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public Date getUpdateDateTime() {
		return updateDateTime;
	}
	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}
	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}


}
