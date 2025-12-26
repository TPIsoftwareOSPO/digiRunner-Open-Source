package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0159RespItem {
	private String Id;
	private String ldapUrl;
	private String ldapBaseDn;
	private String ldapDn;
	private Integer ldapTimeout;
	private String ldapStatus;
	private String approvalResultMail;
	private String iconFile;
	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;
	private String pageTitle;

	@Override
	public String toString() {
		return "DPB0159RespItem [Id=" + Id + ", ldapUrl=" + ldapUrl + ", ldapBaseDn=" + ldapBaseDn + ", ldapDn="
				+ ldapDn + ", ldapTimeout=" + ldapTimeout + ", ldapStatus=" + ldapStatus + ", approvalResultMail="
				+ approvalResultMail + ", iconFile=" + iconFile + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", pageTitle="
				+ pageTitle + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

	public String getLdapDn() {
		return ldapDn;
	}

	public void setLdapDn(String ldapDn) {
		this.ldapDn = ldapDn;
	}

	public Integer getLdapTimeout() {
		return ldapTimeout;
	}

	public void setLdapTimeout(Integer ldapTimeout) {
		this.ldapTimeout = ldapTimeout;
	}

	public String getLdapStatus() {
		return ldapStatus;
	}

	public void setLdapStatus(String ldapStatus) {
		this.ldapStatus = ldapStatus;
	}

	public String getApprovalResultMail() {
		return approvalResultMail;
	}

	public void setApprovalResultMail(String approvalResultMail) {
		this.approvalResultMail = approvalResultMail;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
}
