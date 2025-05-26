package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpSecurityLevelVo  {
	
	private String securityLevelId;

	private String securityLevelName;

	private String securityLevelDesc;

	@Override
	public String toString() {
		return "TsmpSecurityLevel [securityLevelId=" + securityLevelId + ", securityLevelName=" + securityLevelName
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

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getSecurityLevelDesc() {
		return securityLevelDesc;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}

}
