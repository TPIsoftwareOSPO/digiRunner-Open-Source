package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpApiAppId implements Serializable {
	
	public Long refAppId;

	public String refApiUid;

	public TsmpDpApiAppId() {}

	public TsmpDpApiAppId(Long refAppId, String refApiUid) {
		this.refAppId = refAppId;
		this.refApiUid = refApiUid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refApiUid == null) ? 0 : refApiUid.hashCode());
		result = prime * result + ((refAppId == null) ? 0 : refAppId.hashCode());
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
		TsmpDpApiAppId other = (TsmpDpApiAppId) obj;
		if (refApiUid == null) {
			if (other.refApiUid != null)
				return false;
		} else if (!refApiUid.equals(other.refApiUid))
			return false;
		if (refAppId == null) {
			if (other.refAppId != null)
				return false;
		} else if (!refAppId.equals(other.refAppId))
			return false;
		return true;
	}

	public Long getRefAppId() {
		return refAppId;
	}

	public void setRefAppId(Long refAppId) {
		this.refAppId = refAppId;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}
	
}
