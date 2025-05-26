package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpApiThemeId implements Serializable {
	
	public Long refApiThemeId;

	public String refApiUid;

	public TsmpDpApiThemeId() {}

	public TsmpDpApiThemeId(Long refApiThemeId, String refApiUid) {
		this.refApiThemeId = refApiThemeId;
		this.refApiUid = refApiUid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refApiThemeId == null) ? 0 : refApiThemeId.hashCode());
		result = prime * result + ((refApiUid == null) ? 0 : refApiUid.hashCode());
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
		TsmpDpApiThemeId other = (TsmpDpApiThemeId) obj;
		if (refApiThemeId == null) {
			if (other.refApiThemeId != null)
				return false;
		} else if (!refApiThemeId.equals(other.refApiThemeId))
			return false;
		if (refApiUid == null) {
			if (other.refApiUid != null)
				return false;
		} else if (!refApiUid.equals(other.refApiUid))
			return false;
		return true;
	}

	public Long getRefApiThemeId() {
		return refApiThemeId;
	}

	public void setRefApiThemeId(Long refApiThemeId) {
		this.refApiThemeId = refApiThemeId;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}
	
}
