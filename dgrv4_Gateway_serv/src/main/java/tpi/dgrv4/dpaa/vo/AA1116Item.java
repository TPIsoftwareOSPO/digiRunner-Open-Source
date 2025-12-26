package tpi.dgrv4.dpaa.vo;

public class AA1116Item {

	/** 安全等級名稱 用來當作 AA0316Item 的 apiId */
	private String securityLevelName;

	/** 安全等級描述 */
	private String securityLevelDesc;
	
	/** 安全等級ID */
	private String securityLevelId;

	@Override
	public String toString() {
		return "AA1116Item [securityLevelId=" + securityLevelId + ", securityLevelName=" + securityLevelName
				+ ", securityLevelDesc=" + securityLevelDesc + "]";
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

	public String getSecurityLevelDesc() {
		return securityLevelDesc;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}
	
	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

}
