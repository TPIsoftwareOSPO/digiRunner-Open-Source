package tpi.dgrv4.dpaa.vo;

public class DPB0044Req {
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Long newsId;
	
	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;
	
	/** 查詢開始日	參考: DateTimeFormatEnum.java 日期時間格式 YYYY/MM/DD */
	private String queryStartDate;
	
	/** 查詢結束日	參考: DateTimeFormatEnum.java 日期時間格式 YYYY/MM/DD */
	private String queryEndDate;
	
	/** 公告類型	上/下架 */
	private String typeItemNo;
	
	/** 啟用/停用	ex:1 / 0 , 使用BcryptParam設計, itemNo="ENABLE_FLAG */
	private String enFlagEncode;
	
	/** 前後台分類	ex:FRONT / BACK , 使用BcryptParam設計, itemNo="FB_FLAG" */
	private String fbTypeEncode;
	
	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getQueryStartDate() {
		return queryStartDate;
	}

	public void setQueryStartDate(String queryStartDate) {
		this.queryStartDate = queryStartDate;
	}

	public String getQueryEndDate() {
		return queryEndDate;
	}

	public void setQueryEndDate(String queryEndDate) {
		this.queryEndDate = queryEndDate;
	}

	public String getTypeItemNo() {
		return typeItemNo;
	}

	public void setTypeItemNo(String typeItemNo) {
		this.typeItemNo = typeItemNo;
	}

	public String getEnFlagEncode() {
		return enFlagEncode;
	}

	public void setEnFlagEncode(String enFlagEncode) {
		this.enFlagEncode = enFlagEncode;
	}

	public String getFbTypeEncode() {
		return fbTypeEncode;
	}

	public void setFbTypeEncode(String fbTypeEncode) {
		this.fbTypeEncode = fbTypeEncode;
	}

}
