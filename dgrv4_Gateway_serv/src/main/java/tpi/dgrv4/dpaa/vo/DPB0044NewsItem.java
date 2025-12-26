package tpi.dgrv4.dpaa.vo;

public class DPB0044NewsItem {
	/** PK */
	private Long newsId;
	
	/** 標題 */
	private String newTitle;
	
	/** 內容 */
	private String newContent;
	
	/** 啟用狀態code	ex: 1 / 0 */
	private String status;
	
	/** 啟用狀態中文	啟用/停用 , Ref : itemNo="ENABLE_FLAG" */
	private String statusName;
	
	/** 公告日期	"參考: DateTimeFormatEnum.java 日期時間格式 YYYY/MM/DD"*/
	private String postDateTime;
	
	/** 公告類型code	ex : UPDATE / ON / OFF */
	private String typeItemNo;
	
	/** 公告類型中文	Ref : itemNo="NEWS_TYPE" */
	private String typeItemNoName;
	
	/** 組織代碼 */
	private String orgId;
	
	/** 組織名稱 */
	private String orgName;
	
	/**	version	雙欄位更新 */
	private Long lv;

	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}
	
	public String getNewTitle() {
		return newTitle;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getPostDateTime() {
		return postDateTime;
	}

	public void setPostDateTime(String postDateTime) {
		this.postDateTime = postDateTime;
	}

	public String getTypeItemNo() {
		return typeItemNo;
	}

	public void setTypeItemNo(String typeItemNo) {
		this.typeItemNo = typeItemNo;
	}

	public String getTypeItemNoName() {
		return typeItemNoName;
	}

	public void setTypeItemNoName(String typeItemNoName) {
		this.typeItemNoName = typeItemNoName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	@Override
	public String toString() {
		return "DPB0044NewsItem [newsId=" + newsId + ", newTitle=" + newTitle + ", newContent=" + newContent
				+ ", status=" + status + ", statusName=" + statusName + ", postDateTime=" + postDateTime
				+ ", typeItemNo=" + typeItemNo + ", typeItemNoName=" + typeItemNoName + ", orgId=" + orgId
				+ ", orgName=" + orgName + ", lv=" + lv + "]\n";
	}
	
	
}
