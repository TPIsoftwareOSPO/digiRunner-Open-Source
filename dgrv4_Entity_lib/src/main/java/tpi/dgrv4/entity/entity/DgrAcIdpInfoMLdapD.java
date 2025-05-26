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
@Table(name = "dgr_ac_idp_info_mldap_d")
public class DgrAcIdpInfoMLdapD implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "ac_idp_info_mldap_d_id")
	private Long acIdpInfoMLdapDId;

	@Column(name = "ref_ac_idp_info_mldap_m_id")
	private Long refAcIdpInfoMLdapMId;

	@Column(name = "order_no")
	private Integer orderNo;

	@Column(name = "ldap_url")
	private String ldapUrl;

	@Column(name = "ldap_dn")
	private String ldapDn;

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
		return acIdpInfoMLdapDId;
	}

	@Override
	public String toString() {
		return "DgrAcIdpInfoMLdapD [acIdpInfoMLdapDId=" + acIdpInfoMLdapDId + ", refAcIdpInfoMLdapMId="
				+ refAcIdpInfoMLdapMId + ", orderNo=" + orderNo + ", ldapUrl=" + ldapUrl + ", ldapDn=" + ldapDn
				+ ", ldapBaseDn=" + ldapBaseDn + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version
				+ ", getPrimaryKey()=" + getPrimaryKey() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

	public Long getAcIdpInfoMLdapDId() {
		return acIdpInfoMLdapDId;
	}

	public void setAcIdpInfoMLdapDId(Long acIdpInfoMLdapDId) {
		this.acIdpInfoMLdapDId = acIdpInfoMLdapDId;
	}

	public Long getRefAcIdpInfoMLdapMId() {
		return refAcIdpInfoMLdapMId;
	}

	public void setRefAcIdpInfoMLdapMId(Long refAcIdpInfoMLdapMId) {
		this.refAcIdpInfoMLdapMId = refAcIdpInfoMLdapMId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
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
