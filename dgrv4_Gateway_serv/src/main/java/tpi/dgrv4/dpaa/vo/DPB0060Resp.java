package tpi.dgrv4.dpaa.vo;

public class DPB0060Resp {

	/** 工作ID */
	private Long apptJobId;

	/** 狀態 */
	private String status;

	/** lock version */
	private Long lv;

	public DPB0060Resp() {}

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

}
