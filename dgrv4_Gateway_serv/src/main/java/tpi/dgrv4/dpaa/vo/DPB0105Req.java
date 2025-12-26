package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0105Req {

	/** 動作	U-更新/P-暫停/A-啟動/I-略過/S-作廢 */
	private String act;

	/** 週期排程UID */
	private String apptRjobId;

	/** 週期排程版號 */
	private Long lv;

	/** 名稱 */
	private String rjobName;

	/** 備註 */
	private String remark;

	/** 週期設定表單資料 */
	private DPB0105Cron cronJson;

	/** 開始於	yyyy/MM/dd HH:mm:ss */
	private String effDateTime;

	/** 結束於	yyyy/MM/dd HH:mm:ss */
	private String invDateTime;

	/** 原有資料清單 */
	private List<DPB0105Items> oriDataList;

	/** 修改後的資料清單 */
	private List<DPB0105Items> newDataList;

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getRjobName() {
		return rjobName;
	}

	public void setRjobName(String rjobName) {
		this.rjobName = rjobName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public DPB0105Cron getCronJson() {
		return cronJson;
	}

	public void setCronJson(DPB0105Cron cronJson) {
		this.cronJson = cronJson;
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

	public List<DPB0105Items> getOriDataList() {
		return oriDataList;
	}

	public void setOriDataList(List<DPB0105Items> oriDataList) {
		this.oriDataList = oriDataList;
	}

	public List<DPB0105Items> getNewDataList() {
		return newDataList;
	}

	public void setNewDataList(List<DPB0105Items> newDataList) {
		this.newDataList = newDataList;
	}
	
}
