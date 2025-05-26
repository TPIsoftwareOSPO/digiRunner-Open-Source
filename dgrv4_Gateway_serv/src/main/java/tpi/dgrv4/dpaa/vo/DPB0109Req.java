package tpi.dgrv4.dpaa.vo;

public class DPB0109Req {

	/** Event PK */
	private Long eventId;

	/** 是否封存 Y / N */
	private String archiveFlag;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getArchiveFlag() {
		return archiveFlag;
	}

	public void setArchiveFlag(String archiveFlag) {
		this.archiveFlag = archiveFlag;
	}

}
