package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class DgrWebsiteVo {

	private Long dgrWebsiteId;

	private String websiteName;

	private String remark;
	
	private String websiteStatus;
	
	private String auth;
	
	private String ignoreApi;

	@Override
	public String toString() {
		return "DgrWebsiteVo [dgrWebsiteId=" + dgrWebsiteId + ", websiteName=" + websiteName + ", remark=" + remark
				+ ", websiteStatus=" + websiteStatus + ", auth=" + auth + ", ignoreApi=" + ignoreApi + "]";
	}

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWebsiteStatus() {
		return websiteStatus;
	}

	public void setWebsiteStatus(String websiteStatus) {
		this.websiteStatus = websiteStatus;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getIgnoreApi() {
		return ignoreApi;
	}

	public void setIgnoreApi(String ignoreApi) {
		this.ignoreApi = ignoreApi;
	}
	

	
}
