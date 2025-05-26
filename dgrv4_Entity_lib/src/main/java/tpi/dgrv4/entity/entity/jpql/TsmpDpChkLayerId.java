package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpChkLayerId implements Serializable {

	private String reviewType;

	private Integer layer;

	private String roleId;

	public TsmpDpChkLayerId() {}

	public TsmpDpChkLayerId(String reviewType, Integer layer, String roleId) {
		super();
		this.reviewType = reviewType;
		this.layer = layer;
		this.roleId = roleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((layer == null) ? 0 : layer.hashCode());
		result = prime * result + ((reviewType == null) ? 0 : reviewType.hashCode());
		result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpDpChkLayerId other = (TsmpDpChkLayerId) obj;
		if (layer == null) {
			if (other.layer != null)
				return false;
		} else if (!layer.equals(other.layer))
			return false;
		if (reviewType == null) {
			if (other.reviewType != null)
				return false;
		} else if (!reviewType.equals(other.reviewType))
			return false;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TsmpDpChkLayerId [reviewType=" + reviewType + ", layer=" + layer + ", roleId=" + roleId + "]";
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

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
}
