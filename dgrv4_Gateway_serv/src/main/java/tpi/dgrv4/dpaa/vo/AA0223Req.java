package tpi.dgrv4.dpaa.vo;

public class AA0223Req{

	/** 虛擬群組ID */
	private String vgroupId;

	/** 虛擬群組代碼 */
	private String vgroupName;


	public String getVgroupId() {
		return vgroupId;
	}



	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}



	public String getVgroupName() {
		return vgroupName;
	}



	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}


	@Override
	public String toString() {
		return "AA0223Req [vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + "]";
	}

}
