package tpi.dgrv4.dpaa.vo;

public class AA1003Resp {

	/** 組織序號*/
	private String orgId;

	/** 現在時間*/
	private String updateTime;

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "AA1003Resp [orgId=" + orgId + ", updateTime=" + updateTime + "]";
	}

}
