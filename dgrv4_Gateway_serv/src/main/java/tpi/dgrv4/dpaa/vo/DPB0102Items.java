package tpi.dgrv4.dpaa.vo;

public class DPB0102Items {

	/** 週期排程UID */
	private String apptRjobId;

	/** 週期排程版號	from tsmp_dp_appt_rjob.version */
	private Long lv;

	/** 名稱 */
	private String rjobName;

	/** 狀態名稱 */
	private String statusName;

	/** 頻率 */
	private String cronDesc;

	/** 下次執行時間: yyyy/MM/dd HH:mm:ss, 如果狀態是"暫停"或"作廢", 則後端傳回一個"-", 用以隱藏下次執行時間 */
	private String nextDateTime;

	/** 效期 yyyy/MM/dd HH:mm:ss ~ yyyy/MM/dd HH:mm:ss */
	private String effPeriod;

	/** 備註 */
	private String remark;

	/** 是否可操作[明細] */
	private Boolean detailFlag;

	/** 是否可操作[更新] */
	private Boolean updateFlag;

	/** 是否顯示[暫停] */
	private Boolean pauseVisible;

	/** 是否可操作[暫停] */
	private Boolean pauseFlag;

	/** 是否顯示[啟動] */
	private Boolean activeVisible;

	/** 是否可操作[啟動]	 */
	private Boolean activeFlag;

	/** 是否可操作[略過一次] */
	private Boolean skipFlag;

	/** 是否可操作[作廢] */
	private Boolean inactiveFlag;

	/** 是否可操作[歷程] */
	private Boolean historyFlag;

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}
	
	public String getApptRjobId() {
		return apptRjobId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public void setRjobName(String rjobName) {
		this.rjobName = rjobName;
	}
	
	public String getRjobName() {
		return rjobName;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getCronDesc() {
		return cronDesc;
	}

	public void setCronDesc(String cronDesc) {
		this.cronDesc = cronDesc;
	}

	public String getNextDateTime() {
		return nextDateTime;
	}

	public void setNextDateTime(String nextDateTime) {
		this.nextDateTime = nextDateTime;
	}

	public String getEffPeriod() {
		return effPeriod;
	}

	public void setEffPeriod(String effPeriod) {
		this.effPeriod = effPeriod;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Boolean getDetailFlag() {
		return detailFlag;
	}

	public void setDetailFlag(Boolean detailFlag) {
		this.detailFlag = detailFlag;
	}

	public Boolean getUpdateFlag() {
		return updateFlag;
	}

	public void setUpdateFlag(Boolean updateFlag) {
		this.updateFlag = updateFlag;
	}

	public Boolean getPauseVisible() {
		return pauseVisible;
	}

	public void setPauseVisible(Boolean pauseVisible) {
		this.pauseVisible = pauseVisible;
	}

	public Boolean getPauseFlag() {
		return pauseFlag;
	}

	public void setPauseFlag(Boolean pauseFlag) {
		this.pauseFlag = pauseFlag;
	}

	public Boolean getActiveVisible() {
		return activeVisible;
	}

	public void setActiveVisible(Boolean activeVisible) {
		this.activeVisible = activeVisible;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Boolean getSkipFlag() {
		return skipFlag;
	}

	public void setSkipFlag(Boolean skipFlag) {
		this.skipFlag = skipFlag;
	}

	public Boolean getInactiveFlag() {
		return inactiveFlag;
	}

	public void setInactiveFlag(Boolean inactiveFlag) {
		this.inactiveFlag = inactiveFlag;
	}

	public Boolean getHistoryFlag() {
		return historyFlag;
	}

	public void setHistoryFlag(Boolean historyFlag) {
		this.historyFlag = historyFlag;
	}

}
