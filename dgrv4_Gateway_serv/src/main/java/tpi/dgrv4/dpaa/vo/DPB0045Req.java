package tpi.dgrv4.dpaa.vo;

public class DPB0045Req {
	/** PK */
	private Long newsId;
	
	/** 前後台分類	ex:FRONT / BACK , 使用BcryptParam設計, 前台只查 enable, 後台不區分 */
	private String fbTypeEncode;

	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public String getFbTypeEncode() {
		return fbTypeEncode;
	}

	public void setFbTypeEncode(String fbTypeEncode) {
		this.fbTypeEncode = fbTypeEncode;
	}
	
}
	 
