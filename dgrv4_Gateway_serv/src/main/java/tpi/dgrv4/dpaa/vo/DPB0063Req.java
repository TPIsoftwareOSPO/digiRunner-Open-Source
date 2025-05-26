package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0063Req {
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	private Map<String, Map<Integer , List<DPB0063PkReq>>> dataMap;

	public Map<String, Map<Integer, List<DPB0063PkReq>>> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Map<Integer, List<DPB0063PkReq>>> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String toString() {
		return "DPB0063Req [dataMap=" + dataMap + "]";
	}
}
