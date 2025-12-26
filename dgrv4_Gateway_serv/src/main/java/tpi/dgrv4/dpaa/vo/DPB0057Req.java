package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0057Req {

	/** <themeId, themeId, ...etc> */
	private List<Long> delList;

	public DPB0057Req() {}

	public List<Long> getDelList() {
		return delList;
	}

	public void setDelList(List<Long> delList) {
		this.delList = delList;
	}
	
}
