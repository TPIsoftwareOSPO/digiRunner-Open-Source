package tpi.dgrv4.dpaa.vo;

public class DPB0064Items {

	private Long chkLayerId;

	/** PK	item_no*/
	private String reviewType;

	/** PK */
	private Integer layer;

	/** PK */
	private String roleId;

	private String status;

	/** Version */
	private Long lv;

	/** 角色名稱 */
	private String roleName;

	public Long getChkLayerId() {
		return chkLayerId;
	}

	public void setChkLayerId(Long chkLayerId) {
		this.chkLayerId = chkLayerId;
	}

	public void setReviewType(String reviewType) {
		this.reviewType = reviewType;
	}
	
	public String getReviewType() {
		return reviewType;
	}

	public Integer getLayer() {
		return layer;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
	public String getRoleId() {
		return roleId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "\t\n DPB0064Items [chkLayerId=" + chkLayerId + ", reviewType=" + reviewType + ", layer=" + layer
				+ ", roleId=" + roleId + ", status=" + status + ", lv=" + lv + ", roleName=" + roleName + "]\n";
	}
}
