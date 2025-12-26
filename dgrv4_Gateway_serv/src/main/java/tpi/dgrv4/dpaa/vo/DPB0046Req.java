package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0046Req {
	
	/**PK , <newsId, newsId, ...etc>*/
	List<Long> delList;

	public List<Long> getDelList() {
		return delList;
	}

	public void setDelList(List<Long> delList) {
		this.delList = delList;
	}
	
}
