package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpReqOrderd5dId implements Serializable {
	
	private Long refReqOrderd5Id;

	private String refApiUid;

	public TsmpDpReqOrderd5dId() {}
	
	public TsmpDpReqOrderd5dId(Long refReqOrderd5Id, String refApiUid) {
		super();
		this.refReqOrderd5Id = refReqOrderd5Id;
		this.refApiUid = refApiUid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refApiUid == null) ? 0 : refApiUid.hashCode());
		result = prime * result + ((refReqOrderd5Id == null) ? 0 : refReqOrderd5Id.hashCode());
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
		TsmpDpReqOrderd5dId other = (TsmpDpReqOrderd5dId) obj;
		if (refApiUid == null) {
			if (other.refApiUid != null)
				return false;
		} else if (!refApiUid.equals(other.refApiUid))
			return false;
		if (refReqOrderd5Id == null) {
			if (other.refReqOrderd5Id != null)
				return false;
		} else if (!refReqOrderd5Id.equals(other.refReqOrderd5Id))
			return false;
		return true;
	} 
	
	
}
