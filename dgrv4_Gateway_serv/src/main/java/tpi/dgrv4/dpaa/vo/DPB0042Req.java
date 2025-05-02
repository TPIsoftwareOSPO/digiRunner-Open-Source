package tpi.dgrv4.dpaa.vo;

public class DPB0042Req {
	
	/** 標題 */
	private String newTitle;
	
	/** 內容 */
	private String newContent;
	
	/** 公告日期 */
	private String postDateTime;// yyyy/MM/dd
	
	/** 公告類型 */
	private String typeItemNo;
	
	public String getNewTitle() {
		return newTitle;
	}
	
	public String getNewContent() {
		return newContent;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

	public String getPostDateTime() {
		return postDateTime;
	}

	public String getTypeItemNo() {
		return typeItemNo;
	}

	public void setTypeItemNo(String typeItemNo) {
		this.typeItemNo = typeItemNo;
	}
	
	public void setPostDateTime(String postDateTime) {
		this.postDateTime = postDateTime;
	}
}
