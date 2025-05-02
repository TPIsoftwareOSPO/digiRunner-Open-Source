package tpi.dgrv4.dpaa.vo;

public class DPB0240RespItem {

	private String gtwIdpInfoCusId;
	private String clientId;
	private String status;
	private String cusLoginUrl;
	private String cusUserDataUrl;
	private String iconFile;
	private String pageTitle;

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

}
