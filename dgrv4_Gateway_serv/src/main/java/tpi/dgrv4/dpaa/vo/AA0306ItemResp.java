package tpi.dgrv4.dpaa.vo;

public class AA0306ItemResp {

	private String moduleName;
	private String apiKey;

	private boolean processResult;
	private String errMsg;

	private long enableScheduledDate = 0L;
	private long disableScheduledDate = 0L;

	public AA0306ItemResp() {
	}

	public AA0306ItemResp(String apiKey, String moduleName) {
		setApiKey(apiKey);
		setModuleName(moduleName);
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isProcessResult() {
		return processResult;
	}

	public void setProcessResult(boolean processResult) {
		this.processResult = processResult;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public long getEnableScheduledDate() {
		return enableScheduledDate;
	}

	public void setEnableScheduledDate(long enableScheduledDate) {
		this.enableScheduledDate = enableScheduledDate;
	}

	public long getDisableScheduledDate() {
		return disableScheduledDate;
	}

	public void setDisableScheduledDate(long disableScheduledDate) {
		this.disableScheduledDate = disableScheduledDate;
	}

}
