package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DgrAuditLogMId implements Serializable {

	private Long auditLongId;

	private Long auditExtId;

	public DgrAuditLogMId() {}

	public DgrAuditLogMId(Long auditLongId, Long auditExtId) {
		super();
		this.auditLongId = auditLongId;
		this.auditExtId = auditExtId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auditLongId == null) ? 0 : auditLongId.hashCode());
		result = prime * result + ((auditExtId == null) ? 0 : auditExtId.hashCode());
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
		DgrAuditLogMId other = (DgrAuditLogMId) obj;
		if (auditLongId == null) {
			if (other.auditLongId != null)
				return false;
		} else if (!auditLongId.equals(other.auditLongId))
			return false;
		if (auditExtId == null) {
			if (other.auditExtId != null)
				return false;
		} else if (!auditExtId.equals(other.auditExtId))
			return false;
		return true;
	}
 
	public Long getAuditLongId() {
		return auditLongId;
	}

	public void setAuditLongId(Long auditLongId) {
		this.auditLongId = auditLongId;
	}

	public Long getAuditExtId() {
		return auditExtId;
	}

	public void setAuditExtId(Long auditExtId) {
		this.auditExtId = auditExtId;
	}
}
