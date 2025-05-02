package tpi.dgrv4.dpaa.vo;

public class AA1002List {

	/** 組織單位ID */
	private String orgID;
	
	/** 組織代碼 */
	private String orgCode;
	
	/** 組織單位名稱 */
	private String orgName;
	
	/** 父組織單位ID */
	private String parentID;

	
	public String getOrgID() {
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	@Override
	public String toString() {
		return "AA1002List [orgID=" + orgID + ", orgCode=" + orgCode + ", orgName=" + orgName + ", parentID=" + parentID
				+ "]";
	}

	
}
