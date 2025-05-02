package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0127Resp {
	private List<DPB0127RespItem> dataList;

	public List<DPB0127RespItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0127RespItem> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "DPB0127Resp [dataList=" + dataList + "]\n";
	}
}
