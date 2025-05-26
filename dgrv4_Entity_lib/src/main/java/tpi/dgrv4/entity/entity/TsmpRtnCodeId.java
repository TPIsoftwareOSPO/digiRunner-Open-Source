package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpRtnCodeId implements Serializable {
	
	private String tsmpRtnCode;
	
	private String locale;
	
	public TsmpRtnCodeId() {}
	
	public TsmpRtnCodeId(String tsmpRtnCode, String locale) {
		super();
		this.tsmpRtnCode = tsmpRtnCode;
		this.locale = locale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((tsmpRtnCode == null) ? 0 : tsmpRtnCode.hashCode());
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
		TsmpRtnCodeId other = (TsmpRtnCodeId) obj;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		if (tsmpRtnCode == null) {
			if (other.tsmpRtnCode != null)
				return false;
		} else if (!tsmpRtnCode.equals(other.tsmpRtnCode))
			return false;
		return true;
	}

	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	
}
