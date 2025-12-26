package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "DGR_AUDIT_LOGM")
@IdClass(value = DgrAuditLogMId.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class DgrAuditLogM {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "AUDIT_LONG_ID")
	private Long auditLongId;
	
	/**
	 * 底層需填入System.currentTimeMillis()
	 * 這欄位是為了讓底層方便寫入流水號,
	 * 當底層寫入的 AUDIT_LONG_ID 和 AC 寫入的 AUDIT_LONG_ID 重複時,不會出錯
	 */
	@Id
	@Column(name = "AUDIT_EXT_ID")
	private Long auditExtId = System.currentTimeMillis();

	@Column(name = "TXN_UID")
	private String txnUid;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "CLIENT_ID")
	private String clientId;

	@Column(name = "API_URL")
	private String apiUrl;

	@Column(name = "EVENT_NO")
	private String eventNo;

	@Column(name = "USER_IP")
	private String userIp;

	@Column(name = "USER_HOSTNAME")
	private String userHostname;

	@Column(name = "USER_ROLE")
	private String userRole;

	@Column(name = "PARAM1")
	private String param1;

	@Column(name = "PARAM2")
	private String param2;

	@Column(name = "PARAM3")
	private String param3;

	@Column(name = "PARAM4")
	private String param4;

	@Column(name = "PARAM5")
	private String param5;

	@Column(name = "STACK_TRACE")
	private String stackTrace;
	
	@Column(name = "ORIG_API_URL")
	private String origApiUrl;
	
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
		return "DgrAuditLogM [auditLongId=" + auditLongId + ", auditExtId=" + auditExtId + ", txnUid=" + txnUid
				+ ", userName=" + userName + ", clientId=" + clientId + ", apiUrl=" + apiUrl + ", eventNo=" + eventNo
				+ ", userIp=" + userIp + ", userHostname=" + userHostname + ", userRole=" + userRole + ", param1="
				+ param1 + ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4 + ", param5=" + param5
				+ ", stackTrace=" + stackTrace + ", origApiUrl=" + origApiUrl + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]\n";
	}

	public Long getAuditLongId() {
		return auditLongId;
	}

	public void setAuditLongId(Long auditLongId) {
		this.auditLongId = auditLongId;
	}

	public Long getAuditExtId() {
		return auditExtId;
	}

	public void setAuditExtId(Long auditExtId) {
		this.auditExtId = auditExtId;
	}

	public String getTxnUid() {
		return txnUid;
	}

	public void setTxnUid(String txnUid) {
		this.txnUid = txnUid;
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

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getEventNo() {
		return eventNo;
	}

	public void setEventNo(String eventNo) {
		this.eventNo = eventNo;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getUserHostname() {
		return userHostname;
	}

	public void setUserHostname(String userHostname) {
		this.userHostname = userHostname;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getOrigApiUrl() {
		return origApiUrl;
	}

	public void setOrigApiUrl(String origApiUrl) {
		this.origApiUrl = origApiUrl;
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
