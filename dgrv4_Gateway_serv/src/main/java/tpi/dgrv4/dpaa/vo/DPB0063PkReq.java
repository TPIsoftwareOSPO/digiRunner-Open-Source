package tpi.dgrv4.dpaa.vo;

public class DPB0063PkReq {
	
	private String reviewType;
	
	private Integer layer;
	
	private String roleId;
	
	/** Version	null 表示新增 */
	private Long lv;

	public String getReviewType() {
		return reviewType;
	}

	public void setReviewType(String reviewType) {
		this.reviewType = reviewType;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}
	
	public Integer getLayer() {
		return layer;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	@Override
	public String toString() {
		return "DPB0063PkReq [reviewType=" + reviewType + ", layer=" + layer + ", roleId=" + roleId + ", lv=" + lv
				+ "]\n";
	}
	
}
