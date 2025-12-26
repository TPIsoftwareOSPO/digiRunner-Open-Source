package tpi.dgrv4.entity.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDpItemsId implements Serializable {
	
	private String itemNo;
	
	private String subitemNo;
	
	private String locale;
	
	public TsmpDpItemsId() {}
	
	public TsmpDpItemsId(String itemNo, String subitemNo, String locale) {
		this.itemNo = itemNo;
		this.subitemNo = subitemNo;
		this.locale = locale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemNo == null) ? 0 : itemNo.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((subitemNo == null) ? 0 : subitemNo.hashCode());
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
		TsmpDpItemsId other = (TsmpDpItemsId) obj;
		if (itemNo == null) {
			if (other.itemNo != null)
				return false;
		} else if (!itemNo.equals(other.itemNo))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		if (subitemNo == null) {
			if (other.subitemNo != null)
				return false;
		} else if (!subitemNo.equals(other.subitemNo))
			return false;
		return true;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getSubitemNo() {
		return subitemNo;
	}

	public void setSubitemNo(String subitemNo) {
		this.subitemNo = subitemNo;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	
}
