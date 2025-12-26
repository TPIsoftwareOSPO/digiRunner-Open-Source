package tpi.dgrv4.dpaa.vo;

public class DPB0058Req {

	/** PK,做為分頁使用 */
	private Long apptJobId;

	/** 模糊搜尋 */
	private String keyword;

	/** 起始日期,YYYY/MM/DD,比對TSMP_DP_APPT_JOB.CREATE_DATE_TIME */
	private String startDate;

	/** 結束日期,YYYY/MM/DD,比對TSMP_DP_APPT_JOB.CREATE_DATE_TIME */
	private String endDate;

	/** 狀態,ex: A：全部，W：等待，R：執行中，E：失敗，D：完成，C：取消....TSMP_DP_ITEMS.ITEM_NO='JOB_STATUS' */
	private String status;

	public DPB0058Req() {}

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
