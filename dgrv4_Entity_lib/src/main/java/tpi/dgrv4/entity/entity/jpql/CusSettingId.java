package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CusSettingId implements Serializable {
	
	private String settingNo;
	
	private String subsettingNo;
	
	public CusSettingId() {}
	
	public CusSettingId(String settingNo, String subsettingNo) {
		this.settingNo = settingNo;
		this.subsettingNo = subsettingNo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((settingNo == null) ? 0 : settingNo.hashCode());
		result = prime * result + ((subsettingNo == null) ? 0 : subsettingNo.hashCode());
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
		CusSettingId other = (CusSettingId) obj;
		if (settingNo == null) {
			if (other.settingNo != null)
				return false;
		} else if (!settingNo.equals(other.settingNo))
			return false;
		if (subsettingNo == null) {
			if (other.subsettingNo != null)
				return false;
		} else if (!subsettingNo.equals(other.subsettingNo))
			return false;
		return true;
	}

	public String getSettingNo() {
		return settingNo;
	}

	public void setSettingNo(String settingNo) {
		this.settingNo = settingNo;
	}

	public String getSubsettingNo() {
		return subsettingNo;
	}

	public void setSubsettingNo(String subsettingNo) {
		this.subsettingNo = subsettingNo;
	}
}
