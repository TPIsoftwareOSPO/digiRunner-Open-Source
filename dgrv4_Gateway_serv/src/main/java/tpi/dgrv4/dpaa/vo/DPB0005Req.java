package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0005Req {

	/** 多組Client ID	Client ID,Client ID,....etc */
	private List<String> clientIds;

	/** 狀態=2：放行，3：退回 */
	private String regStatus;

	/** 審核備註: "不通過"則審核備註為必填 */
	private String reviewRemark;

	/** 公開/私有=0: 全部, 1: 公開, 2:私有 (會員可查看的權限) */
	private String publicFlag;

	/** 是否寄送 mail(0: 寄送, 1: 不要送) */
	private String mailFlag;

	public DPB0005Req() {}

	public List<String> getClientIds() {
		return clientIds;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public String getReviewRemark() {
		return reviewRemark;
	}

	public void setReviewRemark(String reviewRemark) {
		this.reviewRemark = reviewRemark;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getMailFlag() {
		return mailFlag;
	}

	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}
	
}
