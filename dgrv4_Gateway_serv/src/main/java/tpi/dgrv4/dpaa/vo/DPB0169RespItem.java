package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0169RespItem {
	private String id;
	private String longId;
	private String clientId;
	private String idpType;
	private String status;
	private String remark;
	private String idpClientId;
	private String idpClientName;
	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;

	@Override
	public String toString() {
		return "DPB0169RespItem [id=" + id + ", longId=" + longId + ", clientId=" + clientId + ", idpType=" + idpType
				+ ", status=" + status + ", remark=" + remark + ", idpClientId=" + idpClientId + ", idpClientName="
				+ idpClientName + ", createDateTime=" + createDateTime + ", createUser=" + createUser
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

	public String getIdpClientName() {
		return idpClientName;
	}

	public void setIdpClientName(String idpClientName) {
		this.idpClientName = idpClientName;
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
