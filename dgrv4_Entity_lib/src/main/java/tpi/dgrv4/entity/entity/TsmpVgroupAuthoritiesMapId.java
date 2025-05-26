package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpVgroupAuthoritiesMapId implements Serializable {

	private String vgroupId;

	private String vgroupAuthoritieId;

	public TsmpVgroupAuthoritiesMapId() {}

	public TsmpVgroupAuthoritiesMapId(String vgroupId, String vgroupAuthoritieId) {
		super();
		this.vgroupId = vgroupId;
		this.vgroupAuthoritieId = vgroupAuthoritieId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vgroupId == null) ? 0 : vgroupId.hashCode());
		result = prime * result + ((vgroupAuthoritieId == null) ? 0 : vgroupAuthoritieId.hashCode());
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
		TsmpVgroupAuthoritiesMapId other = (TsmpVgroupAuthoritiesMapId) obj;
		if (vgroupId == null) {
			if (other.vgroupId != null)
				return false;
		} else if (!vgroupId.equals(other.vgroupId))
			return false;
		if (vgroupAuthoritieId == null) {
			if (other.vgroupAuthoritieId != null)
				return false;
		} else if (!vgroupAuthoritieId.equals(other.vgroupAuthoritieId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TsmpVgroupAuthoritiesMapId [vgroupId=" + vgroupId + ", vgroupAuthoritieId=" + vgroupAuthoritieId + "]";
	}

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
