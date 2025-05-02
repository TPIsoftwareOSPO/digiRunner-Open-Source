package tpi.dgrv4.dpaa.vo;

public class AA0202List {

	// 用戶端帳號
	private String clientAlias;

	// 用戶端代號
	private String clientId;

	// 用戶端名稱
	private String clientName;

	// 安全等級Id
	private String securityLevelId;

	// 安全等級名稱
	private String securityLevelName;

	// 入口網開放狀態代碼
	private String publicFlag;

	// 入口網開放狀態名稱 parser ITEM_NO="API_AUTHORITY"
	private String publicFlagName;

	// 狀態代碼
	private String status;

	// 狀態名稱 parser:ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:DB值 (PARAM1) = 中文說明; 1=啟用,
	// 2=停用, 3=鎖定, 不帶SQL條件=全部,
	private String statusName;

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusName() {
		return statusName;
	}
	
	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

}
