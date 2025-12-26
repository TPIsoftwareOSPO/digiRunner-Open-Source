package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_client_vgroup")
@IdClass(value = TsmpClientVgroupId.class)
public class TsmpClientVgroup {

	@Id
	@Column(name = "client_id")
	private String clientId;

	@Id
	@Column(name = "vgroup_id")
	private String vgroupId;

	/* constructors */

	public TsmpClientVgroup() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpClientVgroup [clientId=" + clientId + ", vgroupId=" + vgroupId + "]";
	}

	/* getters and setters */
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	

	
}
