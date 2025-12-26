package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1204DataSetResp {
	private String label;
	private List<Integer> data;

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

	public List<Integer> getData() {
		return data;
	}

	public void setData(List<Integer> data) {
		this.data = data;
	}

}
