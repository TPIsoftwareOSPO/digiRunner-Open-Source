package tpi.dgrv4.dpaa.vo;

public class DPB0067Req {

	/** PK,做為分頁使用,必須是 List 回傳的最後一筆 */
	private Long reqOrdermId;

	/** 模糊搜尋 */
	private String keyword;

	@Override
	public String toString() {
		return "DPB0067Req [reqOrdermId=" + reqOrdermId + ", keyword=" + keyword + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", encodeReqType=" + encodeReqType + ", encodeReqSubtype=" + encodeReqSubtype
				+ ", encodeQuyType=" + encodeQuyType + "]";
	}

	/** 申請日期 start, 前端預設為7天前, 日期時間格式YYYY/MM/DD */
	private String startDate;

	/** 申請日期 end, 預設為今天 表示範圍到今天的 23:59, 日期時間格式YYYY/MM/DD */
	private String endDate;

	/** 主類別:用戶申請API / 後台申請API上下架 */
	private String encodeReqType;

	/** 申請類別:上架 / 下架 / 異動 */
	private String encodeReqSubtype;

	/**
		REQ: 申請單, EXA: 待審單, REV: 已審單。
		使用BcryptParam (ITEM_NO = ORDERM_QUY_TYPE)。
		前台使用 clientId, 後台使用 userName。
		申請單：個人自己的申請單。
		待審單：擁有審核權的申請單。
		已審單：擁有審核權的申請單且已被審核，可能由它人審核。
	 */
	private String encodeQuyType;

	public String getEncodeQuyType() {
		return encodeQuyType;
	}

	public void setEncodeQuyType(String encodeQuyType) {
		this.encodeQuyType = encodeQuyType;
	}

	public DPB0067Req() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getStartDate() {
		return startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getEncodeReqType() {
		return encodeReqType;
	}

	public void setEncodeReqType(String encodeReqType) {
		this.encodeReqType = encodeReqType;
	}

	public String getEncodeReqSubtype() {
		return encodeReqSubtype;
	}

	public void setEncodeReqSubtype(String encodeReqSubtype) {
		this.encodeReqSubtype = encodeReqSubtype;
	}

}
