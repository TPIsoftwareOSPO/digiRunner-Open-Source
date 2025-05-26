package tpi.dgrv4.dpaa.vo;

public class DPB0043Req {
	
	/** PK */
	private Long newsId;
	
	/** version	where 雙欄位更新 */
	private Long lv;
	
	/** 啟用狀態code	BcryptParam(ENABLE_FLAG) = 1, 這裡有值時表示只要變動 status, 其它不更新 */
	private String statusEncode;
	
	/** 標題 */
	private String newTitle;
	
	/** 內容 */
	private String newContent;
	
	/** 公告日期	"參考: DateTimeFormatEnum.java 日期時間格式 YYYY/MM/DD" */
	private String postDateTime;
	
	/** 公告類型	前端使用 Lov 帶入 No */
	private String typeItemNo;

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

	public String getStatusEncode() {
		return statusEncode;
	}

	public void setStatusEncode(String statusEncode) {
		this.statusEncode = statusEncode;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}
	
	public String getNewContent() {
		return newContent;
	}

	public String getPostDateTime() {
		return postDateTime;
	}

	public void setPostDateTime(String postDateTime) {
		this.postDateTime = postDateTime;
	}

	public void setTypeItemNo(String typeItemNo) {
		this.typeItemNo = typeItemNo;
	}
	
	public String getTypeItemNo() {
		return typeItemNo;
	}
}
