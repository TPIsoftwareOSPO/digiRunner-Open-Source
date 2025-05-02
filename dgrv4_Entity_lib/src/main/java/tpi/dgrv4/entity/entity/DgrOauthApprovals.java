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
@Table(name = "dgr_oauth_approvals")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class DgrOauthApprovals {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "OAUTH_APPROVALS_ID")
	private Long oauthApprovalsId;
	
	/** 使用者名稱 */
	@Column(name = "USER_NAME")
	private String userName;
	
	/** 用戶端ID */
	@Column(name = "CLIENT_ID")
	private String clientId;
	
	/** 授權項目 */
	@Column(name = "SCOPE")
	private String scope;
	
	/** 是否授權 */
	@Column(name = "STATUS")
	private String status;
	
	/** 過期時間 */
	@Column(name = "EXPIRES_AT")
	private Date expiresAt;
	
	/** 最後更新時間 */
	@Column(name = "LAST_MODIFIED_AT")
	private Date lastModifiedAt;

	@Column(name = "CREATE_DATE_TIME")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "CREATE_USER")
	private String createUser = "SYSTEM";

	@Column(name = "UPDATE_DATE_TIME")
	private Date updateDateTime;

	@Column(name = "UPDATE_USER")
	private String updateUser;

	@Version
	@Column(name = "VERSION")
	private Long version = 1L;

	@Override
	public String toString() {
		return "DgrOauthApprovals [oauthApprovalsId=" + oauthApprovalsId + ", userName=" + userName + ", clientId="
				+ clientId + ", scope=" + scope + ", status=" + status + ", expiresAt=" + expiresAt
				+ ", lastModifiedAt=" + lastModifiedAt + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]";
	}

	public Long getOauthApprovalsId() {
		return oauthApprovalsId;
	}

	public void setOauthApprovalsId(Long oauthApprovalsId) {
		this.oauthApprovalsId = oauthApprovalsId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}

	public Date getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(Date lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
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
