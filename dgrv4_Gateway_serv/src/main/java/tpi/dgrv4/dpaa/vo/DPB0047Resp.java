package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0047Resp {
	
	/** 子類型清單	(subItems/defaultVal 2個屬性2擇1) */
	public List<DPB0047SubItems> subItems;
	
	/** 取得subItemNo值, ex: DISABLE	(subItems/defaultVal 2個屬性2擇1) */
	public String defaultVal;

	public List<DPB0047SubItems> getSubItems() {
		return subItems;
	}

	public void setSubItems(List<DPB0047SubItems> subItems) {
		this.subItems = subItems;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

}
