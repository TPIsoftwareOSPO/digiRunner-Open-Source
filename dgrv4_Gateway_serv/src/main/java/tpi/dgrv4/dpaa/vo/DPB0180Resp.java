package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0180Resp {
	private String masterId;
	private String masterLongId;
	private String status;
	private Integer ldapTimeout;
	private String policy;
	private String approvalResultMail;
	private String pageTitle;
	private String iconFile;
	private List<DPB0180LdapDataItem> ldapDataList;

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String getMasterLongId() {
		return masterLongId;
	}

	public void setMasterLongId(String masterLongId) {
		this.masterLongId = masterLongId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getLdapTimeout() {
		return ldapTimeout;
	}

	public void setLdapTimeout(Integer ldapTimeout) {
		this.ldapTimeout = ldapTimeout;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public String getApprovalResultMail() {
		return approvalResultMail;
	}

	public void setApprovalResultMail(String approvalResultMail) {
		this.approvalResultMail = approvalResultMail;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public List<DPB0180LdapDataItem> getLdapDataList() {
		return ldapDataList;
	}

	public void setLdapDataList(List<DPB0180LdapDataItem> ldapDataList) {
		this.ldapDataList = ldapDataList;
	}
}