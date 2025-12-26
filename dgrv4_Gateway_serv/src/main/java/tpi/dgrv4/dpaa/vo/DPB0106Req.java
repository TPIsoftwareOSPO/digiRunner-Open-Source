package tpi.dgrv4.dpaa.vo;

public class DPB0106Req {

	/** Event PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Long eventId;

	/** 起始日期,YYYY/MM/DD */
	private String startDate;

	/** 結束日期,YYYY/MM/DD */
	private String endDate;

	/** 模糊搜尋 */
	private String keyword;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
