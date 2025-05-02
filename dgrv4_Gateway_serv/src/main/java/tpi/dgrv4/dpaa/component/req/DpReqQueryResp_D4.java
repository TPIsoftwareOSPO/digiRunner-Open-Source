package tpi.dgrv4.dpaa.component.req;

public class DpReqQueryResp_D4 {

	/** 員工(使用者)ID */
	private String userId;

	/** 使用者姓名 */
	private String userName;

	/** 文章內容 */
	private String article;

	public DpReqQueryResp_D4() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

}
