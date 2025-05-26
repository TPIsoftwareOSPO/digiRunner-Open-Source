package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0046Resp {
	
	/** 刪除結果, <id, "Ok">, <id, "Fail">....etc*/
	Map<Long, String> resultMap;

	public Map<Long, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<Long, String> resultMap) {
		this.resultMap = resultMap;
	}
	
	
}
