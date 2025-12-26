package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0104Resp {

	/** 週期排程UID */
	private String apptRjobId;

	/** 名稱 */
	private String rjobName;

	/** 備註 */
	private String remark;

	/** 歷程清單 */
	private List<DPB0104Items> historyList;

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
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

	public List<DPB0104Items> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<DPB0104Items> historyList) {
		this.historyList = historyList;
	}

}
