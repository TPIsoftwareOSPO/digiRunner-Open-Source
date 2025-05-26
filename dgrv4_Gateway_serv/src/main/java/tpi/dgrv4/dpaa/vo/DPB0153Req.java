package tpi.dgrv4.dpaa.vo;

public class DPB0153Req {
	private String id; // PK
	private String keyword; // 關鍵字搜尋
	private String websiteStatus; // 狀態

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getWebsiteStatus() {
		return websiteStatus;
	}

	public void setWebsiteStatus(String websiteStatus) {
		this.websiteStatus = websiteStatus;
	}

}
