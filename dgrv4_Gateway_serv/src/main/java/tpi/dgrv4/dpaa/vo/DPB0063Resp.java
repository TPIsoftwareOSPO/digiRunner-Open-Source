package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0063Resp {
	
	private Map<String, Map<Integer , List<DPB0063SaveItem>>> dataMap;

	public Map<String, Map<Integer, List<DPB0063SaveItem>>> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Map<Integer, List<DPB0063SaveItem>>> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String toString() {
		return "DPB0063Resp [dataMap=" + dataMap + "]";
	}
	
}
