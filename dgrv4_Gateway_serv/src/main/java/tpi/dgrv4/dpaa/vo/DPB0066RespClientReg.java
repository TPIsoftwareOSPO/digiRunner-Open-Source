package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0066RespClientReg {

	/** from TSMP_DP_REQ_ORDERM.req_orderm_id */
	private Long reqOrdermId;

	/** 申請單主檔的版號 from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** 新增完成回傳 */
	private List<Long> reqOrdersIds;

	/** from TSMP_DP_REQ_ORDERD3.req_orderd3_id */
	private Long reqOrderd3Id;

	/** 用戶延伸檔序號 from TSMP_DP_CLIENTEXT.client_seq_id */
	private Long clientSeqId;

	/** 若有上傳才會回傳 */
	private Long fileId;

	/** 上傳檔名 */
	private String fileName;

	public DPB0066RespClientReg() {
	}

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
	
	public Long getClientSeqId() {
		return clientSeqId;
	}

	public List<Long> getReqOrdersIds() {
		return reqOrdersIds;
	}

	public void setReqOrdersIds(List<Long> reqOrdersIds) {
		this.reqOrdersIds = reqOrdersIds;
	}

	public Long getReqOrderd3Id() {
		return reqOrderd3Id;
	}

	public void setReqOrderd3Id(Long reqOrderd3Id) {
		this.reqOrderd3Id = reqOrderd3Id;
	}

	public void setClientSeqId(Long clientSeqId) {
		this.clientSeqId = clientSeqId;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
