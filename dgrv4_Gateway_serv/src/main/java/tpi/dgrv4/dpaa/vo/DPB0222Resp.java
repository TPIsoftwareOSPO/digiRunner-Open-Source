package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0222Resp {

	private String cusId;

	private String cusName;

	private String cusStatus;

	private String cusLoginUrl;

	private String cusBackendLoginUrl;

	private String cusUserDataUrl;

	private Date createDateTime;

	private String createUser;

	private Date updateDateTime;

	private String updateUser;

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getCusStatus() {
		return cusStatus;
	}

	public void setCusStatus(String cusStatus) {
		this.cusStatus = cusStatus;
	}

	public String getCusLoginUrl() {
		return cusLoginUrl;
	}

	public void setCusLoginUrl(String cusLoginUrl) {
		this.cusLoginUrl = cusLoginUrl;
	}

	public String getCusBackendLoginUrl() {
		return cusBackendLoginUrl;
	}

	public void setCusBackendLoginUrl(String cusBackendLoginUrl) {
		this.cusBackendLoginUrl = cusBackendLoginUrl;
	}

	public String getCusUserDataUrl() {
		return cusUserDataUrl;
	}

	public void setCusUserDataUrl(String cusUserDataUrl) {
		this.cusUserDataUrl = cusUserDataUrl;
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
