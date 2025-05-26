package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0065RespOpenApiKey {

	/** PK 新增完成回傳 */
	private Long reqOrdermId;

	/** 申請單主檔版號 from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** PK 只有一筆(layer=0) */
	private Long reqOrdersId;

	/** PK 只有一筆(layer=0) */
	private Long reqOrderd5Id;

	/** PKs 新增或刪除完成回傳 */
	private List<Long> reqOrderd5dIds;

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}
	
	public Long getReqOrderd5Id() {
		return reqOrderd5Id;
	}

	public Long getLv() {
		return lv;
	}
	
	public void setLv(Long lv) {
		this.lv = lv;
	}

	public Long getReqOrdersId() {
		return reqOrdersId;
	}

	public void setReqOrdersId(Long reqOrdersId) {
		this.reqOrdersId = reqOrdersId;
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
