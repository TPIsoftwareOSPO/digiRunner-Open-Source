package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0319Req {

	/** 批號 */
	private Integer batchNo;

	/** API 清單 */
	private List<AA0319ReqItem> apiList;

	public Integer getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(Integer batchNo) {
		this.batchNo = batchNo;
	}

	public List<AA0319ReqItem> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0319ReqItem> apiList) {
		this.apiList = apiList;
	}

}