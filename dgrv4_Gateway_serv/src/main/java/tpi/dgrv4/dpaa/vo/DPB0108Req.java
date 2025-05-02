package tpi.dgrv4.dpaa.vo;

public class DPB0108Req {

	/** Event PK */
	private Long eventId;

	/** 是否保留 Y / N */
	private String keepFlag;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getKeepFlag() {
		return keepFlag;
	}

	public void setKeepFlag(String keepFlag) {
		this.keepFlag = keepFlag;
	}

}
