package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB9915Resp {
	private String autoDeleteDays;
	private List<DPB9915Item> dataList;
	
	public String getAutoDeleteDays() {
		return autoDeleteDays;
	}
	public void setAutoDeleteDays(String autoDeleteDays) {
		this.autoDeleteDays = autoDeleteDays;
	}
	public List<DPB9915Item> getDataList() {
		return dataList;
	}
	public void setDataList(List<DPB9915Item> dataList) {
		this.dataList = dataList;
	}
}
