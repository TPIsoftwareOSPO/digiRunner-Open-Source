package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0064Resp {

	Map<String, Map<Integer, List<DPB0064Items>>> dataMap;

	Map<String, String> typeMap;

	public Map<String, Map<Integer, List<DPB0064Items>>> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Map<Integer, List<DPB0064Items>>> dataMap) {
		this.dataMap = dataMap;
	}

	public Map<String, String> getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(Map<String, String> typeMap) {
		this.typeMap = typeMap;
	}

	@Override
	public String toString() {
		return "DPB0064Resp [\n dataMap=" + dataMap + ", \n typeMap=" + typeMap + "]";
	}

}
