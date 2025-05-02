package tpi.dgrv4.dpaa.component.req;

import java.util.List;
import java.util.Map;

public class DpReqServiceUpdateReq {

	private Long reqOrdermId;

	private Long lv;

	/** tsmp_dp_req_orderm.client_id */
	private String clientId;

	private String reqUserId;

	private String orgId;

	private String reqDesc;

	/** yyyy/MM/dd */
	private String effectiveDate;

	/** from tsmp_user.user_name / tsmp_client.client_name */
	private String updateUser;

	/** 新增的檔案(暫存檔名) */
	private List<String> newTempFileNames;

	/**
	 * 異動的檔案: Map<原有檔名, 更新檔名><br>
	 * ex: 假設此單已有名為"測試.txt"的附件, 則:<br>
	 * 1. "更新"檔案時: map.put("測試.txt", "12371853.wait.測試2.txt");<br>
	 * 2. "刪除"檔案時: map.put("測試.txt", null);<br>
	 */
	private Map<String, String> oldFileMapping;

	public DpReqServiceUpdateReq() {
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getReqDesc() {
		return reqDesc;
	}

	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public List<String> getNewTempFileNames() {
		return newTempFileNames;
	}

	public void setNewTempFileNames(List<String> newTempFileNames) {
		this.newTempFileNames = newTempFileNames;
	}

	public Map<String, String> getOldFileMapping() {
		return oldFileMapping;
	}

	public void setOldFileMapping(Map<String, String> oldFileMapping) {
		this.oldFileMapping = oldFileMapping;
	}

}
