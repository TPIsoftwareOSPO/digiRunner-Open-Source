package tpi.dgrv4.entity.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_group_authorities")
public class TsmpGroupAuthorities implements Serializable {

	@Id
	@Column(name = "group_authoritie_id")
	private String groupAuthoritieId;

	@Column(name = "group_authoritie_name")
	private String groupAuthoritieName;

	@Column(name = "group_authoritie_level")
	private String groupAuthoritieLevel;

	@Column(name = "group_authoritie_desc")
	private String groupAuthoritieDesc;


	/* constructors */
	public TsmpGroupAuthorities() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpGroupAuthorities [groupAuthoritieId=" + groupAuthoritieId + ", groupAuthoritieName="
				+ groupAuthoritieName + ", groupAuthoritieLevel=" + groupAuthoritieLevel + ", groupAuthoritieDesc="
				+ groupAuthoritieDesc + "]";
	}

	/* getters and setters */
	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}
	
	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}
	
	public String getGroupAuthoritieName() {
		return groupAuthoritieName;
	}
	
	public void setGroupAuthoritieName(String groupAuthoritieName) {
		this.groupAuthoritieName = groupAuthoritieName;
	}
	
	public String getGroupAuthoritieLevel() {
		return groupAuthoritieLevel;
	}
	
	public void setGroupAuthoritieLevel(String groupAuthoritieLevel) {
		this.groupAuthoritieLevel = groupAuthoritieLevel;
	}
	
	public String getGroupAuthoritieDesc() {
		return groupAuthoritieDesc;
	}
	
	public void setGroupAuthoritieDesc(String groupAuthoritieDesc) {
		this.groupAuthoritieDesc = groupAuthoritieDesc;
	}

	

}
