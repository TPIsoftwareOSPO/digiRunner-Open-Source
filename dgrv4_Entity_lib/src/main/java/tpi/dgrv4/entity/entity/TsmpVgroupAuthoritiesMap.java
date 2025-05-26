package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity 
@Table(name = "tsmp_vgroup_authorities_map")
@IdClass(value = TsmpVgroupAuthoritiesMapId.class)
public class TsmpVgroupAuthoritiesMap {

	@Id
	@Column(name = "vgroup_id")
	private String vgroupId;

	@Id
	@Column(name = "vgroup_authoritie_id")
	private String vgroupAuthoritieId;

	
	/* constructors */

	public TsmpVgroupAuthoritiesMap() {}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpVgroupAuthoritiesMap [vgroupId=" + vgroupId + ", vgroupAuthoritieId=" + vgroupAuthoritieId + "]";
	}
	
	/* getters and setters */
	

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getVgroupAuthoritieId() {
		return vgroupAuthoritieId;
	}

	public void setVgroupAuthoritieId(String vgroupAuthoritieId) {
		this.vgroupAuthoritieId = vgroupAuthoritieId;
	}

	

	
	
}
