package tpi.dgrv4.dpaa.vo;

public class DPB0068Req {

	private Long reqOrdermId;

	/**
	是否只傳個人的申請單
	不是審核者也可以查個人申請單(依個人身份查出個人填寫的 "申請單")
	前台使用 clientId, 後台使用 userName
	Y: 查個人申請單, N: 只查審核者工作單
	*/
	@Deprecated
	private String isPersonal;

	/**
	 * 申請審核單查詢類別-REQ: 申請單, EXA: 待審單, REV: 已審單<br/>
	 * 使用BcryptParam(ITEM_NO = ORDERM_QUY_TYPE)<br/>
	 * 前台使用 clientId, 後台使用 userName<br/>
	 * 申請單：個人自己的申請單。
	 * 待審單：擁有審核權的申請單。
	 * 已審單：擁有審核權的申請單且已被審核，可能由它人審核。
	 */
	private String encodeQuyType;

	public DPB0068Req() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	@Deprecated
	public String getIsPersonal() {
		return isPersonal;
	}

	@Deprecated
	public void setIsPersonal(String isPersonal) {
		this.isPersonal = isPersonal;
	}

	public String getEncodeQuyType() {
		return encodeQuyType;
	}

	public void setEncodeQuyType(String encodeQuyType) {
		this.encodeQuyType = encodeQuyType;
	}

}
