package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0126Resp {

	/** Index清單*/
	private List<String> indexList;

	public List<String> getIndexList() {
		return indexList;
	}

	public void setIndexList(List<String> indexList) {
		this.indexList = indexList;
	}

	@Override
	public String toString() {
		return "EA0013Resp [indexList=" + indexList + "]";
	}

}