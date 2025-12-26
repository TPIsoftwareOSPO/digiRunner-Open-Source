package tpi.dgrv4.common.utils.autoInitSQL.vo;


import java.util.Date;

public class TsmpOrganizationVo {

	private String orgId;

	private String orgName;

	private String parentId;

	private String orgPath;

	private String orgCode;

	private String createUser;

	private Date createTime;

	private String updateUser;

	private Date updateTime;

	private String contactName;

	private String contactTel;

	private String contactMail;

	/* constructors */

	public TsmpOrganizationVo() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpOrganization [orgId=" + orgId + ", orgName=" + orgName + ", parentId=" + parentId + ", orgPath="
				+ orgPath + ", orgCode=" + orgCode + ", createUser=" + createUser + ", updateUser=" + updateUser + ", contactName=" + contactName
				+ ", contactTel=" + contactTel + ", contactMail=" + contactMail + "]\n";
	}

	/* getters and setters */

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

	public String getParentId() {
		return parentId;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getOrgPath() {
		return orgPath;
	}
	
	public String getCreateUser() {
		return createUser;
	}

	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactTel() {
		return contactTel;
	}

	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}
	
	public String getUpdateUser() {
		return updateUser;
	}

	public String getContactMail() {
		return contactMail;
	}

	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}
	
}

