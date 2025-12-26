package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DPB0189Resp {

	private String rtnCode = "1000";
	@JsonProperty("isResultJson")
	private boolean isResultJson;
	private String result;
	private String timestamp;
	
	

	@Override
	public String toString() {
		return "DPB0189Resp [rtnCode=" + rtnCode + ", isResultJson=" + isResultJson + ", result=" + result
				+ ", timestamp=" + timestamp + "]";
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getTimestamp() {
		return String.valueOf(System.currentTimeMillis());
	}

	public String getRtnCode() {
		return rtnCode;
	}

	public void setRtnCode(String rtnCode) {
		this.rtnCode = rtnCode;
	}

	public boolean isResultJson() {
		return isResultJson;
	}

	@JsonProperty("isResultJson")
	public void setResultJson(boolean isResultJson) {
		this.isResultJson = isResultJson;
	}


	
}
