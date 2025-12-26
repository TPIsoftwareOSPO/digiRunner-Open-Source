package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0065RespApiApplication {

	/** 新增完成回傳 */
	private Long reqOrdermId;

	/** 申請單主檔版號,from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** 只有一筆(layer=0) */
	private Long reqOrdersId;

	/** PKs 新增完成回傳 */
	private List<Long> reqOrderd1Ids;

	/** 若有上傳才會回傳 */
	private Long fileId;

	/** 申請單附件檔名,若有上傳才會回傳 */
	private String fileName;

	public DPB0065RespApiApplication() {
		// TODO Auto-generated constructor stub
	}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public Long getLv() {
		return lv;
	}
	
	public Long getReqOrdersId() {
		return reqOrdersId;
	}

	public void setReqOrdersId(Long reqOrdersId) {
		this.reqOrdersId = reqOrdersId;
	}

	public List<Long> getReqOrderd1Ids() {
		return reqOrderd1Ids;
	}

	public void setReqOrderd1Ids(List<Long> reqOrderd1Ids) {
		this.reqOrderd1Ids = reqOrderd1Ids;
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
