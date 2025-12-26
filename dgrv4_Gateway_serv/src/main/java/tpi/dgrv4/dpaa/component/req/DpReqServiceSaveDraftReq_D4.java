package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceSaveDraftReq_D4 extends DpReqServiceSaveDraftReq {

	/** 員工(使用者)ID */
	private String userId;

	/** 文章內容 */
	private String article;

	public DpReqServiceSaveDraftReq_D4() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

}
