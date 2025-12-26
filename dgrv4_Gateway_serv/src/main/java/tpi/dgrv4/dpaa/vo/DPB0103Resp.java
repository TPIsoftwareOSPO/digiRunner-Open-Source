package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0103Resp {

	/** 週期排程UID */
	private String apptRjobId;

	/** 週期排程版號, from tsmp_dp_appt_rjob.version */
	private Long lv;

	/** 名稱 */
	private String rjobName;

	/** 備註 */
	private String remark;

	/** 週期設定表單資料 */
	private DPB0103Cron cronJson;

	/** 表達式, 用於[頻率]的hover顯示 */
	private String cronExpression;

	/** 開始於, yyyy/MM/dd HH:mm:ss */
	private String effDateTime;

	/** 結束於, yyyy/MM/dd HH:mm:ss */
	private String invDateTime;

	/** 狀態代碼 */
	private String status;

	/** 原有資料清單 */
	private List<DPB0103Items> oriDataList;

	public String getApptRjobId() {
		return apptRjobId;
	}

	public String getRjobName() {
		return rjobName;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}
	
	public void setRjobName(String rjobName) {
		this.rjobName = rjobName;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public DPB0103Cron getCronJson() {
		return cronJson;
	}

	public void setCronJson(DPB0103Cron cronJson) {
		this.cronJson = cronJson;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getEffDateTime() {
		return effDateTime;
	}

	public void setEffDateTime(String effDateTime) {
		this.effDateTime = effDateTime;
	}

	public String getInvDateTime() {
		return invDateTime;
	}

	public void setInvDateTime(String invDateTime) {
		this.invDateTime = invDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<DPB0103Items> getOriDataList() {
		return oriDataList;
	}

	public void setOriDataList(List<DPB0103Items> oriDataList) {
		this.oriDataList = oriDataList;
	}

}
