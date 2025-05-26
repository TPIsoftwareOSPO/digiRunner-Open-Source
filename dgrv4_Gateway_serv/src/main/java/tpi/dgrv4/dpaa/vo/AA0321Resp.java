package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0321Resp {

	private List<AA0321RespItem> dataList;

	public List<AA0321RespItem> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0321RespItem> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "DPB0075Resp [dataList=" + dataList + "]";
	}
}
