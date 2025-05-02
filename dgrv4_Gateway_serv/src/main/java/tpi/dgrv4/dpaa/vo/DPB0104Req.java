package tpi.dgrv4.dpaa.vo;

public class DPB0104Req {

	/** 工作ID, PK, 分頁用 */
	private Long apptJobId;

	/** 週期排程UID */
	private String apptRjobId;

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

}
