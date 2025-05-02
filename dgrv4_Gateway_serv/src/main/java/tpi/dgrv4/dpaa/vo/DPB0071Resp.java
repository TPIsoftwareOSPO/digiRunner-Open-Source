package tpi.dgrv4.dpaa.vo;

public class DPB0071Resp {

	/** log 流水號 */
	private Long chkLogId;

	/** 狀態流水號 */
	private Long reqOrdersId;

	/** 狀態的lockVersion */
	private Long lv;

	public DPB0071Resp() {}

	public Long getChkLogId() {
		return chkLogId;
	}

	public void setChkLogId(Long chkLogId) {
		this.chkLogId = chkLogId;
	}

	public Long getReqOrdersId() {
		return reqOrdersId;
	}

	public void setReqOrdersId(Long reqOrdersId) {
		this.reqOrdersId = reqOrdersId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

}
