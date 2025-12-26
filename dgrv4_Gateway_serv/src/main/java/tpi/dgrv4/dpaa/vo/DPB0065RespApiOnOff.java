package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0065RespApiOnOff {

	/**  */
	private Long reqOrdermId;

	/** v3.6 -> 申請單主檔版號, from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** v3.6 -> 儲存"草稿"時只會有一筆審核狀態(申請者本身) 
	private List<Long> reqOrdersIds;
	*/
	private Long reqOrdersId;

	/** Map<Long, List<reqOrderd2dId>> */
	private Map<Long, List<Long>> reqOrderd2Ids;

	/** v3.6 -> 儲存"草稿"時不記Log
	private Long chkLogId;
	*/

	/** Map<d2Id, fileId> */
	private Map<Long, Long> reqOrderd2FileIds;

	public DPB0065RespApiOnOff() {}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}
	
	public Long getReqOrdermId() {
		return reqOrdermId;
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

	public Map<Long, List<Long>> getReqOrderd2Ids() {
		return reqOrderd2Ids;
	}

	public void setReqOrderd2Ids(Map<Long, List<Long>> reqOrderd2Ids) {
		this.reqOrderd2Ids = reqOrderd2Ids;
	}

	public Map<Long, Long> getReqOrderd2FileIds() {
		return reqOrderd2FileIds;
	}

	public void setReqOrderd2FileIds(Map<Long, Long> reqOrderd2FileIds) {
		this.reqOrderd2FileIds = reqOrderd2FileIds;
	}
	
}
