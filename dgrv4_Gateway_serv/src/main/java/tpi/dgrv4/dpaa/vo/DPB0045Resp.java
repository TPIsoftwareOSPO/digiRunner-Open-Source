package tpi.dgrv4.dpaa.vo;

public class DPB0045Resp {
	/** PK */
	private Long newsId;
	
	/** version	雙欄位更新 */
	private Long lv;
	
	/** 標題 */
	private String newTitle;
	
	/** 內容 */
	private String newContent;
	
	/** 公告日期	參考: DateTimeFormatEnum.java 日期時間格式 YYYY/MM/DD */
	private String postDateTime;
	
	/** 公告類型 */
	private String typeItemNo;
	
	/** 公告類型名稱	from TSMP_DP_ITEMS.subItemName */
	private String typeItemName;
	
	/** 組織代碼	可以用來說明發佈機關, 後端從token 取得資料 */
	private String orgId;
	
	/** 組織名稱	from TSMP_ORGANIZATION */
	private String orgName;

	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
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

	public String getTypeItemName() {
		return typeItemName;
	}

	public void setTypeItemName(String typeItemName) {
		this.typeItemName = typeItemName;
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
}
