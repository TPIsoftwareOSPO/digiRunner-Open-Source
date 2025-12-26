package tpi.dgrv4.entity.entity;

import java.io.Serializable;

public class TsmpGroupAuthoritiesMapId implements Serializable{
	private String groupId;

	private String groupAuthoritieId;
	
	public TsmpGroupAuthoritiesMapId() {}

	public TsmpGroupAuthoritiesMapId(String groupId, String groupAuthoritieId) {
		super();
		this.groupId = groupId;
		this.groupAuthoritieId = groupAuthoritieId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupAuthoritieId == null) ? 0 : groupAuthoritieId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
		TsmpGroupAuthoritiesMapId other = (TsmpGroupAuthoritiesMapId) obj;
		if (groupAuthoritieId == null) {
			if (other.groupAuthoritieId != null)
				return false;
		} else if (!groupAuthoritieId.equals(other.groupAuthoritieId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}

	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}
	
	
}
