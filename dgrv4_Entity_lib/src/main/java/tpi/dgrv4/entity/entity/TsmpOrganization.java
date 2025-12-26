package tpi.dgrv4.entity.entity;


import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_organization")
public class TsmpOrganization implements Serializable {

	@Id
	@Column(name = "org_id")
	private String orgId;

	@Column(name = "org_name")
	private String orgName;

	@Column(name = "parent_id")
	private String parentId;

	@Column(name = "org_path")
	private String orgPath;

	@Column(name = "org_code")
	private String orgCode;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "contact_tel")
	private String contactTel;

	@Column(name = "contact_mail")
	private String contactMail;

	/* constructors */

	public TsmpOrganization() {}

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

