package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_client_group")
@IdClass(TsmpClientGroupId.class)
public class TsmpClientGroup {

	@Id
	@Column(name = "client_id")
	private String clientId;

	@Id
	@Column(name = "group_id")
	private String groupId;

	/* constructors */

	public TsmpClientGroup() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpClientGroup [clientId=" + clientId + ", groupId=" + groupId + "]";
	}

	/* getters and setters */

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
}
