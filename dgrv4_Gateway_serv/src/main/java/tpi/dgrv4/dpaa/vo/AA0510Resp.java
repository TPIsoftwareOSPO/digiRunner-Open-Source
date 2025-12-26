package tpi.dgrv4.dpaa.vo;

public class AA0510Resp {

	// TSMP Core Version
	private String coreVer;

	private String dcPrefix;

	// DP Rounting
	private Integer dp;

	// TSMP Edition
	private String edition;

	private String apiLogWriteRDB;

	private String net;
	
	// 登出/逾時後應要跳到指定頁
	private String logoutUrl;
	
	// 到期日
	private String expiryDate;
	
	private String account;
	
	private String env;

	public String getCoreVer() {
		return coreVer;
	}

	public void setCoreVer(String coreVer) {
		this.coreVer = coreVer;
	}

	public String getDcPrefix() {
		return dcPrefix;
	}

	public void setDcPrefix(String dcPrefix) {
		this.dcPrefix = dcPrefix;
	}

	public Integer getDp() {
		return dp;
	}

	public void setDp(Integer dp) {
		this.dp = dp;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getApiLogWriteRDB() {
		return apiLogWriteRDB;
	}

	public void setApiLogWriteRDB(String apiLogWriteRDB) {
		this.apiLogWriteRDB = apiLogWriteRDB;
	}

	public String getNet() {
		return net;
	}

	public void setNet(String net) {
		this.net = net;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
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
	
}
