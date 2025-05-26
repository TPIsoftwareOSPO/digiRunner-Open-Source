package tpi.dgrv4.dpaa.vo;

public class AA1001Req {
	
	/* 上層組織名稱*/
	private String parentId;
	
	/* 組織名稱*/
	private String orgName;
	
	/* 組織代碼*/
	private String orgCode;
	
	/* 聯絡人電話*/
	private String contactTel;
	
	/* 聯絡人姓名*/
	private String contactName;
	
	/* 聯絡人信箱*/
	private String contactMail;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMail() {
		return contactMail;
	}

	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}

	@Override
	public String toString() {
		return "AA1001Req [parentId=" + parentId + ", orgName=" + orgName + ", orgCode=" + orgCode + ", contactTel="
				+ contactTel + ", contactName=" + contactName + ", contactMail=" + contactMail + "]";
	}
	
}
