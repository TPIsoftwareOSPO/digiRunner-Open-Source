package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0095Resp {
	
	private List<DPB0095Item> dataList;

	@Override
	public String toString() {
		return "DPB0095Resp [dataList=" + dataList + "]";
	}

	public List<DPB0095Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0095Item> dataList) {
		this.dataList = dataList;
	}
 
}
