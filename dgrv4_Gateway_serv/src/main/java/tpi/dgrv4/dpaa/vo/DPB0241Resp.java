package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0241Resp {

	private String gtwIdpInfoCusId;
	private String clientId;
	private String status;
	private String cusLoginUrl;
	private String cusUserDataUrl;
	private String iconFile;
	private String pageTitle;
	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;

	public String getGtwIdpInfoCusId() {
		return gtwIdpInfoCusId;
	}

	public void setGtwIdpInfoCusId(String gtwIdpInfoCusId) {
		this.gtwIdpInfoCusId = gtwIdpInfoCusId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCusLoginUrl() {
		return cusLoginUrl;
	}

	public void setCusLoginUrl(String cusLoginUrl) {
		this.cusLoginUrl = cusLoginUrl;
	}

	public String getCusUserDataUrl() {
		return cusUserDataUrl;
	}

	public void setCusUserDataUrl(String cusUserDataUrl) {
		this.cusUserDataUrl = cusUserDataUrl;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
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
