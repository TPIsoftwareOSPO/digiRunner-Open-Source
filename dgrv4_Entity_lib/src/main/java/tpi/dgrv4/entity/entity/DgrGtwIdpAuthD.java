package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "dgr_gtw_idp_auth_d")
@TableGenerator(name = "seq_store", initialValue = 2000000000)
public class DgrGtwIdpAuthD {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "gtw_idp_auth_d_id")
	private Long gtwIdpAuthDId;

	@Column(name = "ref_gtw_idp_auth_m_id")
	private Long refGtwIdpAuthMId;

	@Column(name = "scope")
	private String scope;

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
	public String toString() {
		return "DgrGtwIdpAuthD [gtwIdpAuthDId=" + gtwIdpAuthDId + ", refGtwIdpAuthMId=" + refGtwIdpAuthMId + ", scope="
				+ scope + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getGtwIdpAuthDId() {
		return gtwIdpAuthDId;
	}

	public void setGtwIdpAuthDId(Long gtwIdpAuthDId) {
		this.gtwIdpAuthDId = gtwIdpAuthDId;
	}

	public Long getRefGtwIdpAuthMId() {
		return refGtwIdpAuthMId;
	}

	public void setRefGtwIdpAuthMId(Long refGtwIdpAuthMId) {
		this.refGtwIdpAuthMId = refGtwIdpAuthMId;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
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
