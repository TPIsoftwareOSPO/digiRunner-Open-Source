package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpReqOrderd2dId implements Serializable {

	private Long reqOrderd2Id;

	private String apiUid;

	private Long refThemeId;

	public TsmpDpReqOrderd2dId() {}

	public TsmpDpReqOrderd2dId(Long reqOrderd2Id, String apiUid, Long refThemeId) {
		super();
		this.reqOrderd2Id = reqOrderd2Id;
		this.apiUid = apiUid;
		this.refThemeId = refThemeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apiUid == null) ? 0 : apiUid.hashCode());
		result = prime * result + ((refThemeId == null) ? 0 : refThemeId.hashCode());
		result = prime * result + ((reqOrderd2Id == null) ? 0 : reqOrderd2Id.hashCode());
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
		TsmpDpReqOrderd2dId other = (TsmpDpReqOrderd2dId) obj;
		if (apiUid == null) {
			if (other.apiUid != null)
				return false;
		} else if (!apiUid.equals(other.apiUid))
			return false;
		if (refThemeId == null) {
			if (other.refThemeId != null)
				return false;
		} else if (!refThemeId.equals(other.refThemeId))
			return false;
		if (reqOrderd2Id == null) {
			if (other.reqOrderd2Id != null)
				return false;
		} else if (!reqOrderd2Id.equals(other.reqOrderd2Id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TsmpDpReqOrderd2dId [reqOrderd2Id=" + reqOrderd2Id + ", apiUid=" + apiUid + ", refThemeId=" + refThemeId
				+ "]";
	}

	public Long getReqOrderd2Id() {
		return reqOrderd2Id;
	}

	public void setReqOrderd2Id(Long reqOrderd2Id) {
		this.reqOrderd2Id = reqOrderd2Id;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public Long getRefThemeId() {
		return refThemeId;
	}

	public void setRefThemeId(Long refThemeId) {
		this.refThemeId = refThemeId;
	}
	
}
