package tpi.dgrv4.dpaa.vo;

public class DPB0127RespItem {
	private Long auditLongId;
	private Long auditExtId;
	private String txnUid;
	private String userName;
	private String clientId;
	private String apiUrl;
	private String origApiUrl;
	private String eventNo;
	private String eventName;
	private String userIp;
	private String userHostname;
	private String userRole;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String stackTrace;
	private boolean isUserRoleTruncated;
	private String truncatedUserRole;
	private String createDateTime;

	@Override
	public String toString() {
		return "DPB0127RespItem [auditLongId=" + auditLongId + ", auditExtId=" + auditExtId + ", txnUid=" + txnUid
				+ ", userName=" + userName + ", clientId=" + clientId + ", apiUrl=" + apiUrl + ", origApiUrl="
				+ origApiUrl + ", eventNo=" + eventNo + ", eventName=" + eventName + ", userIp=" + userIp
				+ ", userHostname=" + userHostname + ", userRole=" + userRole + ", param1=" + param1 + ", param2="
				+ param2 + ", param3=" + param3 + ", param4=" + param4 + ", param5=" + param5 + ", stackTrace="
				+ stackTrace + ", isUserRoleTruncated=" + isUserRoleTruncated + ", truncatedUserRole="
				+ truncatedUserRole + ", createDateTime=" + createDateTime + "]\n";
	}

	public Long getAuditLongId() {
		return auditLongId;
	}

	public void setAuditLongId(Long auditLongId) {
		this.auditLongId = auditLongId;
	}

	public void setAuditExtId(Long auditExtId) {
		this.auditExtId = auditExtId;
	}
	
	public Long getAuditExtId() {
		return auditExtId;
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

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getOrigApiUrl() {
		return origApiUrl;
	}

	public void setOrigApiUrl(String origApiUrl) {
		this.origApiUrl = origApiUrl;
	}

	public String getEventNo() {
		return eventNo;
	}

	public void setEventNo(String eventNo) {
		this.eventNo = eventNo;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public void setUserHostname(String userHostname) {
		this.userHostname = userHostname;
	}
	
	public String getUserHostname() {
		return userHostname;
	}

	public String getUserRole() {
		return userRole;
	}

	public String getParam1() {
		return param1;
	}
	
	public void setUserRole(String userRole) {
		this.userRole = userRole;
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
	
	public String getParam3() {
		return param3;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	public String getStackTrace() {
		return stackTrace;
	}

	public boolean getUserRoleTruncated() {
		return isUserRoleTruncated;
	}

	public void setUserRoleTruncated(boolean isUserRoleTruncated) {
		this.isUserRoleTruncated = isUserRoleTruncated;
	}

	public String getTruncatedUserRole() {
		return truncatedUserRole;
	}

	public void setTruncatedUserRole(String truncatedUserRole) {
		this.truncatedUserRole = truncatedUserRole;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

}
