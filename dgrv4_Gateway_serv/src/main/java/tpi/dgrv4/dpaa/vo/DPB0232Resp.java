package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0232Resp {

	private String status; // 狀態, Y 或 N
	private List<DPB0232WhitelistItem> dataList; // 資料清單

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<DPB0232WhitelistItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0232WhitelistItem> dataList) {
		this.dataList = dataList;
	}
}
