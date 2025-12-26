package tpi.dgrv4.dpaa.vo;

public class AA1004Req {
	
	/* 組織序號*/
	private String orgId;
	
	/* 組織名稱*/
	private String orgName;

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	
	@Override
	public String toString() {
		return "AA1004Req [orgId=" + orgId + ", orgName=" + orgName + "]";
	}
	
}
