package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceUpdateReq_D4 extends DpReqServiceUpdateReq {

	/** 員工(使用者)ID */
	private String userId;

	/** 文章內容 */
	private String article;

	public DpReqServiceUpdateReq_D4() {
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
