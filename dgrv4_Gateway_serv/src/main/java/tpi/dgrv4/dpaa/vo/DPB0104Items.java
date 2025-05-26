package tpi.dgrv4.dpaa.vo;

public class DPB0104Items {

	/** 工作ID */
	private Long apptJobId;

	/** 大分類代碼 */
	private String refItemNo;

	/** 大分類名稱 */
	private String refItemName;

	/** 子項目代碼 */
	private String refSubitemNo;

	/** 子項目名稱 */
	private String refSubitemName;

	/** 排程順序, 除正整數外, 後端也會回傳"N", 表示找不到排程順序, 該項目已不在排程清單中 */
	private String sortBy;

	/** 排程時間	yyyy/MM/dd HH:mm:ss */
	private String periodNexttime;

	/** 更新時間	yyyy/MM/dd HH:mm:ss */
	private String updateDateTime;

	/** 狀態代碼	ITEM_NO = 'JOB_STATUS' */
	private String status;

	/** 狀態名稱 */
	private String statusName;

	/** 執行結果	ITEM_NO = 'SCHED_MSG' */
	private String execResult;

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public String getRefItemNo() {
		return refItemNo;
	}

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}

	public String getRefItemName() {
		return refItemName;
	}

	public void setRefItemName(String refItemName) {
		this.refItemName = refItemName;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}

	public String getRefSubitemName() {
		return refSubitemName;
	}

	public void setRefSubitemName(String refSubitemName) {
		this.refSubitemName = refSubitemName;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getPeriodNexttime() {
		return periodNexttime;
	}

	public void setPeriodNexttime(String periodNexttime) {
		this.periodNexttime = periodNexttime;
	}

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getExecResult() {
		return execResult;
	}

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}
	
}
