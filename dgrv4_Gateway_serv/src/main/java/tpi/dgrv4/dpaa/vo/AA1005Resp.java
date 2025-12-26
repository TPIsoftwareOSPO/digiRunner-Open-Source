package tpi.dgrv4.dpaa.vo;

public class AA1005Resp {

	/* 組織序號*/
	private String orgId;
	
	/* 組織名稱*/
	private String orgName;
	
	/* 上層組織名稱*/
	private String parentName;
	
	/* 建立人員*/
	private String createUser;
	
	/* 建立日期*/
	private String createTime;
	
	/* 聯絡人姓名*/
	private String contactName;
	
	/* 組織代碼*/
	private String orgCode;
	
	/* 聯絡人電話*/
	private String contactTel;
	
	/* 聯絡人信箱*/
	private String contactMail;

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getContactTel() {
		return contactTel;
	}

	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}

	public String getContactMail() {
		return contactMail;
	}

	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}

	@Override
	public String toString() {
		return "AA1005Resp [orgId=" + orgId + ", orgName=" + orgName + ", parentName=" + parentName + ", createUser="
				+ createUser + ", createTime=" + createTime + ", contactName=" + contactName + ", orgCode=" + orgCode
				+ ", contactTel=" + contactTel + ", contactMail=" + contactMail + "]";
	}
	
}
