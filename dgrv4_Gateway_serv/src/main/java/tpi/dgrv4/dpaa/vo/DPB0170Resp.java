package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0170Resp {
	private String id;

	private String longId;

	private String clientId;

	private String idpType;

	private String status;

	private String remark;

	private String idpClientId;

	private String idpClientMima;

	private String idpClientName;

	private String wellKnownUrl;

	private String callbackUrl;

	private String authUrl;

	private String accessTokenUrl;

	private String scope;

	private Date createDateTime;

	private String createUser;

	private Date updateDateTime;

	private String updateUser;

	@Override
	public String toString() {
		return "DPB0170Resp [id=" + id + ", longId=" + longId + ", clientId=" + clientId + ", idpType=" + idpType
				+ ", status=" + status + ", remark=" + remark + ", idpClientId=" + idpClientId + ", idpClientMima="
				+ idpClientMima + ", idpClientName=" + idpClientName + ", wellKnownUrl=" + wellKnownUrl
				+ ", callbackUrl=" + callbackUrl + ", authUrl=" + authUrl + ", accessTokenUrl=" + accessTokenUrl
				+ ", scope=" + scope + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIdpClientId() {
		return idpClientId;
	}

	public void setIdpClientId(String idpClientId) {
		this.idpClientId = idpClientId;
	}

	public String getIdpClientMima() {
		return idpClientMima;
	}

	public void setIdpClientMima(String idpClientMima) {
		this.idpClientMima = idpClientMima;
	}

	public String getIdpClientName() {
		return idpClientName;
	}

	public void setIdpClientName(String idpClientName) {
		this.idpClientName = idpClientName;
	}

	public String getWellKnownUrl() {
		return wellKnownUrl;
	}

	public void setWellKnownUrl(String wellKnownUrl) {
		this.wellKnownUrl = wellKnownUrl;
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

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
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
}
