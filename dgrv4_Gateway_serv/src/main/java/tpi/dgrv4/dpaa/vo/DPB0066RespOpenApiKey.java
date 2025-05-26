package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0066RespOpenApiKey {

	/** PK from TSMP_DP_REQ_ORDERM.req_orderm_id */
	private Long reqOrdermId;

	/** 申請單主檔的版號 from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** PKs 新增完成回傳 */
	private List<Long> reqOrdersIds;

	/** PK 新增完成回傳 */
	private Long reqOrderd5Id;

	/** PKs 新增完成回傳 */
	private List<Long> reqOrderd5dIds;

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public List<Long> getReqOrdersIds() {
		return reqOrdersIds;
	}

	public void setReqOrdersIds(List<Long> reqOrdersIds) {
		this.reqOrdersIds = reqOrdersIds;
	}

	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public void setReqOrderd5Id(Long reqOrderd5Id) {
		this.reqOrderd5Id = reqOrderd5Id;
	}

	public List<Long> getReqOrderd5dIds() {
		return reqOrderd5dIds;
	}

	public void setReqOrderd5dIds(List<Long> reqOrderd5dIds) {
		this.reqOrderd5dIds = reqOrderd5dIds;
	}

}
