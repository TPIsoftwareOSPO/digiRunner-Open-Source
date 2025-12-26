package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0075Resp {
	
	private List<DPB0075RespItem> dataList;

	public List<DPB0075RespItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0075RespItem> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "DPB0075Resp [dataList=" + dataList + "]";
	}
}
