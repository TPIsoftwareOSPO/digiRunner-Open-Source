package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0057Resp {

	/** 刪除結果	<id, "Ok">, <id, "Fail">....etc */
	private Map<Long, String> resultMap;

	/** 檔案刪除結果	<fileId, "Ok">, <fileId, "Fail">....etc */
	private Map<Long, String> resultFileMap;

	public DPB0057Resp() {}

	public Map<Long, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<Long, String> resultMap) {
		this.resultMap = resultMap;
	}

	public Map<Long, String> getResultFileMap() {
		return resultFileMap;
	}

	public void setResultFileMap(Map<Long, String> resultFileMap) {
		this.resultFileMap = resultFileMap;
	}
	
}
