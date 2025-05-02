package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "TSMP_GROUP_AUTHORITIES_MAP")
@IdClass(TsmpGroupAuthoritiesMapId.class)
public class TsmpGroupAuthoritiesMap {
	@Id
	@Column(name = "GROUP_ID")
	private String groupId;

	@Id
	@Column(name = "GROUP_AUTHORITIE_ID")
	private String groupAuthoritieId;

	@Override
	public String toString() {
		return "TsmpGroupAuthoritiesMap [groupId=" + groupId + ", groupAuthoritieId=" + groupAuthoritieId + "]";
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
