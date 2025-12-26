package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqServiceResp {

	/** from tsmp_dp_req_orderm.req_orderm_id */
	private Long reqOrdermId;

	/** from tsmp_dp_req_orderm.version */
	private Long lv;

	/** from tsmp_dp_req_orders.id where req_orderm_id */
	private List<Long> sIds;

	/** 當次操作所留下的log, from tsmp_dp_chk_log */
	private Long chkLogId;

	/** 申請單附件, from tsmp_dp_file where ref_file_cate_code = 'M_ATTACHMENT' */
	private List<Long> fileIds;

	/** 上傳失敗時所回傳的暫存檔名 */
	private List<String> failToUploads;

	public DpReqServiceResp() {
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

	public List<Long> getsIds() {
		return sIds;
	}

	public void setsIds(List<Long> sIds) {
		this.sIds = sIds;
	}

	public Long getChkLogId() {
		return chkLogId;
	}

	public void setChkLogId(Long chkLogId) {
		this.chkLogId = chkLogId;
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	public List<String> getFailToUploads() {
		return failToUploads;
	}

	public void setFailToUploads(List<String> failToUploads) {
		this.failToUploads = failToUploads;
	}

}
