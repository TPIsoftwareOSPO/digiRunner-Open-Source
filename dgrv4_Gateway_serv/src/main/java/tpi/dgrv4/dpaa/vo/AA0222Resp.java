package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0222Resp {
	

	private List<AA0222Item> dataList;

	public List<AA0222Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0222Item> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "AA0222Resp [dataList=" + dataList + ", getDataList()=" + getDataList() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

	
}
