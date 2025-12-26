package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Entity
@Table(name = "dgr_ac_idp_info_ldap")
public class DgrAcIdpInfoLdap implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "ac_idp_info_ldap_id")
	private Long acIdpInfoLdapId;

	@Column(name = "ldap_url")
	private String ldapUrl;

	@Column(name = "ldap_dn")
	private String ldapDn;

	@Column(name = "ldap_timeout")
	private Integer ldapTimeout;

	@Column(name = "ldap_status")
	private String ldapStatus;

	@Column(name = "approval_result_mail")
	private String approvalResultMail;

	@Column(name = "icon_file")
	private String iconFile;

	@Column(name = "page_title")
	private String pageTitle;

	@Column(name = "ldap_base_dn")
	private String ldapBaseDn;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@Override
	public Long getPrimaryKey() {
		return acIdpInfoLdapId;
	}

	@Override
	public String toString() {
		return "DgrAcIdpInfoLdap [acIdpInfoLdapId=" + acIdpInfoLdapId + ", ldapUrl=" + ldapUrl + ", ldapDn=" + ldapDn
				+ ", ldapTimeout=" + ldapTimeout + ", ldapStatus=" + ldapStatus + ", approvalResultMail="
				+ approvalResultMail + ", iconFile=" + iconFile + ", pageTitle=" + pageTitle + ", ldapBaseDn="
				+ ldapBaseDn + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getAcIdpInfoLdapId() {
		return acIdpInfoLdapId;
	}

	public void setAcIdpInfoLdapId(Long acIdpInfoLdapId) {
		this.acIdpInfoLdapId = acIdpInfoLdapId;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
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

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
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
	
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
