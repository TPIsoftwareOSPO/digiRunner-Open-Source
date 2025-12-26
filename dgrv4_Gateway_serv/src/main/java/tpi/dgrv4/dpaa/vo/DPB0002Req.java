package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0002Req {

	/** PK: where 條件, ex: 1, 2, 3, 5, 6....以"," 分隔 */
	//private String apiAuthId;

	/** 申請狀態: (通過/不通過 : P/F) */
	private String statusParam;

	/** 審核備註: "不通過"則審核備註為必填 */
	private String reviewRemark;

	/** 是否寄送 mail(0: 寄送, 1: 不要送) */
	private String mailFlag;

	/** version(處理雙欄位更新, Map<apiAuthId,lv>) */
	private List<Map<String, Long>> lv;

	public DPB0002Req() {}

	/*
	public String getApiAuthId() {
		return apiAuthId;
	}

	public void setApiAuthId(String apiAuthId) {
		this.apiAuthId = apiAuthId;
	}
	*/

	public String getStatusParam() {
		return statusParam;
	}

	public void setStatusParam(String statusParam) {
		this.statusParam = statusParam;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}

	public String getMailFlag() {
		return mailFlag;
	}

	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}

	public List<Map<String, Long>> getLv() {
		return lv;
	}

	public void setLv(List<Map<String, Long>> lv) {
		this.lv = lv;
	}
	
}
