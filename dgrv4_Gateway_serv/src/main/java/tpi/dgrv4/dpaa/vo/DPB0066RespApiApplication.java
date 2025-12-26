package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0066RespApiApplication {

	/** from TSMP_DP_REQ_ORDERM.req_orderm_id */
	private Long reqOrdermId;

	/** 申請單主檔的版號 from TSMP_DP_REQ_ORDERM.version */
	private Long lv;

	/** 新增完成回傳 */
	private List<Long> reqOrdersIds;

	/** 新增完成回傳 */
	private List<Long> reqOrderd1Ids;

	/** from TSMP_DP_API_AUTH2.api_auth_id */
	private List<Long> apiAuthIds;

	/** 若有上傳才會回傳 */
	private Long fileId;

	/** 上傳檔名 */
	private String fileName;

	public DPB0066RespApiApplication() {
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
	
	public List<Long> getReqOrderd1Ids() {
		return reqOrderd1Ids;
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

	public void setReqOrderd1Ids(List<Long> reqOrderd1Ids) {
		this.reqOrderd1Ids = reqOrderd1Ids;
	}

	public List<Long> getApiAuthIds() {
		return apiAuthIds;
	}

	public void setApiAuthIds(List<Long> apiAuthIds) {
		this.apiAuthIds = apiAuthIds;
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
