package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpClientVgroupId implements Serializable {

	private String clientId;

	private String vgroupId;

	public TsmpClientVgroupId() {}

	public TsmpClientVgroupId(String clientId, String vgroupId) {
		super();
		this.clientId = clientId;
		this.vgroupId = vgroupId;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
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
		TsmpClientVgroupId other = (TsmpClientVgroupId) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
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
		return "TsmpClientVgroupId [clientId=" + clientId + ", vgroupId=" + vgroupId + "]";
	}

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
