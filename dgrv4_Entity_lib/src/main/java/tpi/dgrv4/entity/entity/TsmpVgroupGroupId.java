package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpVgroupGroupId implements Serializable {

	private String vgroupId;

	private String groupId;

	public TsmpVgroupGroupId() {}

	public TsmpVgroupGroupId(String vgroupId, String groupId) {
		super();
		this.vgroupId = vgroupId;
		this.groupId = groupId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((vgroupId == null) ? 0 : vgroupId.hashCode());
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
		TsmpVgroupGroupId other = (TsmpVgroupGroupId) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (vgroupId == null) {
			if (other.vgroupId != null)
				return false;
		} else if (!vgroupId.equals(other.vgroupId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TsmpVgroupGroupId [vgroupId=" + vgroupId + ", groupId=" + groupId + "]";
	}

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	
}
