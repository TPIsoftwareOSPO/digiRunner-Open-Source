package tpi.dgrv4.dpaa.vo;

public class DPB0065RespClientReg {

	/**  */
	private Long reqOrdermId;

	/** 申請單主檔版號, from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** 只有一筆(layer=0) */
	private Long reqOrdersId;

	/** 新增完成回傳 */
	private Long reqOrderd3Id;

	/** 若有上傳才會回傳 */
	private Long fileId;

	/** 申請單附件檔名 */
	private String fileName;

	public DPB0065RespClientReg() {
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

	public Long getReqOrdersId() {
		return reqOrdersId;
	}

	public void setReqOrdersId(Long reqOrdersId) {
		this.reqOrdersId = reqOrdersId;
	}

	public Long getReqOrderd3Id() {
		return reqOrderd3Id;
	}

	public void setReqOrderd3Id(Long reqOrderd3Id) {
		this.reqOrderd3Id = reqOrderd3Id;
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
