package tpi.dgrv4.dpaa.vo;

public class DPB0063SaveItem {
	
	private Long chkLayerId;
	
	/** PK */
	private String reviewType;
	
	/** PK */
	private Integer layer;
	
	/** PK */
	private String roleId;
	
	private String status;
	
	/** Version */
	private Long lv;

	public Long getChkLayerId() {
		return chkLayerId;
	}

	public String getReviewType() {
		return reviewType;
	}

	public void setReviewType(String reviewType) {
		this.reviewType = reviewType;
	}

	public Integer getLayer() {
		return layer;
	}

	
	
	public void setChkLayerId(Long chkLayerId) {
		this.chkLayerId = chkLayerId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}
	
	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	@Override
	public String toString() {
		return "DPB0063SaveItem [chkLayerId=" + chkLayerId + ", reviewType=" + reviewType + ", layer=" + layer
				+ ", roleId=" + roleId + ", status=" + status + ", lv=" + lv + "]";
	}
	
}
