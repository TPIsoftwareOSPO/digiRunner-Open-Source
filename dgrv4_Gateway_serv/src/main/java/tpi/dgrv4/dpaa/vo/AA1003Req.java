package tpi.dgrv4.dpaa.vo;

public class AA1003Req {
	
	/* 原上層組織名稱*/
	private String parentId;
	
	/* 上層組織名稱*/
	private String newParentId;
	
	/* 組織單位ID*/
	private String orgId;
	
	/* 原組織名稱*/
	private String orgName;
	
	/* 組織名稱*/
	private String newOrgName;
	
	/* 原組織代碼*/
	private String orgCode;
	
	/* 組織代碼*/
	private String newOrgCode;
	
	/* 原聯絡人電話*/
	private String contactTel;
	
	/* 聯絡人電話*/
	private String newContactTel;
	
	/* 原聯絡人姓名*/
	private String contactName;
	
	/* 聯絡人姓名*/
	private String newContactName;
	
	/* 原聯絡人信箱*/
	private String contactMail;
	
	/* 聯絡人信箱*/
	private String newContactMail;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getNewParentId() {
		return newParentId;
	}

	public void setNewParentId(String newParentId) {
		this.newParentId = newParentId;
	}

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

	public String getNewOrgName() {
		return newOrgName;
	}

	public void setNewOrgName(String newOrgName) {
		this.newOrgName = newOrgName;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getNewOrgCode() {
		return newOrgCode;
	}

	public void setNewOrgCode(String newOrgCode) {
		this.newOrgCode = newOrgCode;
	}

	public String getContactTel() {
		return contactTel;
	}

	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}

	public String getNewContactTel() {
		return newContactTel;
	}

	public void setNewContactTel(String newContactTel) {
		this.newContactTel = newContactTel;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getNewContactName() {
		return newContactName;
	}

	public void setNewContactName(String newContactName) {
		this.newContactName = newContactName;
	}

	public String getContactMail() {
		return contactMail;
	}

	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}

	public String getNewContactMail() {
		return newContactMail;
	}

	public void setNewContactMail(String newContactMail) {
		this.newContactMail = newContactMail;
	}

	@Override
	public String toString() {
		return "AA1003Req [parentId=" + parentId + ", newParentId=" + newParentId + ", orgId=" + orgId + ", orgName="
				+ orgName + ", newOrgName=" + newOrgName + ", orgCode=" + orgCode + ", newOrgCode=" + newOrgCode
				+ ", contactTel=" + contactTel + ", newContactTel=" + newContactTel + ", contactName=" + contactName
				+ ", newContactName=" + newContactName + ", contactMail=" + contactMail + ", newContactMail="
				+ newContactMail + "]";
	}
	
}
