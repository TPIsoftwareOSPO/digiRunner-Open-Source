package tpi.dgrv4.dpaa.vo;

public class DPB0118Resp {
	private String majorVersionNo;
	private String version;
	private String edition;
	private String expiryDate;
	private Long timestamp = Long.valueOf(System.currentTimeMillis());
	private Long nearWarnDays;
	private Long overBufferDays;
	private String account;
	private String env;
	private String remoteAddr;
	private String forwardResp;
	
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getMajorVersionNo() {
		return majorVersionNo;
	}
	public void setMajorVersionNo(String majorVersionNo) {
		this.majorVersionNo = majorVersionNo;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public Long getNearWarnDays() {
		return nearWarnDays;
	}
	public void setNearWarnDays(Long nearWarnDays) {
		this.nearWarnDays = nearWarnDays;
	}
	public Long getOverBufferDays() {
		return overBufferDays;
	}
	public void setOverBufferDays(Long overBufferDays) {
		this.overBufferDays = overBufferDays;
	}
	@Override
	public String toString() {
		return "DPB0118Resp [majorVersionNo=" + majorVersionNo + ", version=" + version + ", edition=" + edition + ", expiryDate=" + expiryDate
				+ ", timestamp=" + timestamp + ", nearWarnDays=" + nearWarnDays + ", overBufferDays=" + overBufferDays + ", account=" + account + "]";
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public String getRemoteAddr() {
		return remoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public String getForwardResp() {
		return forwardResp;
	}
	public void setForwardResp(String forwardResp) {
		this.forwardResp = forwardResp;
	}
}
