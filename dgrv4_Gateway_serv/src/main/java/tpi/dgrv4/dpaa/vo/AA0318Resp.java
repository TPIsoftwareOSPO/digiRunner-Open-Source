package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0318Resp {
	
	/** 批號 */
	private Integer batchNo;
	
	/** API清單 */
	private List<AA0318Item> apiList;

	public Integer getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(Integer batchNo) {
		this.batchNo = batchNo;
	}

	public List<AA0318Item> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0318Item> apiList) {
		this.apiList = apiList;
	}

}