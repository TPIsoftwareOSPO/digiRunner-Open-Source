package tpi.dgrv4.dpaa.component.req;

import java.util.List;

/**
 * 儲存草稿共用
 * 
 * @author Kim
 *
 */
public class DpReqServiceSaveDraftReq {

	private String reqType;

	private String reqSubtype;

	/** tsmp_dp_req_orderm.client_id */
	private String clientId;

	private String reqUserId;

	private String orgId;

	private String reqDesc;

	/** yyyy/MM/dd */
	private String effectiveDate;

	/** from tsmp_user.user_name / tsmp_client.client_name */
	private String createUser;

	/** 暫存檔名, 若有值, 則會從暫存資料夾複製到 M_ATTACHMENT (申請單附件)資料夾下 */
	private List<String> tempFileNames;

	public DpReqServiceSaveDraftReq() {
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
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

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public List<String> getTempFileNames() {
		return tempFileNames;
	}

	public void setTempFileNames(List<String> tempFileNames) {
		this.tempFileNames = tempFileNames;
	}
}
