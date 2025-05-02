package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.entity.entity.TsmpApi;

public class AA0303Item {

	/** 模組名稱 */
	private String moduleName;

	/** API ID */
	private String apiKey;

	/** 預定啟用日期 */
	private long enableScheduledDate;

	/** 預定停用日期 */
	private long disableScheduledDate;

	private boolean processResult;

	private String errMsg;

	private String apiStatus;

	private String apiStatusName;

	/** TSMP_API, 此欄位不應被解析 */
	@JsonIgnore
	private TsmpApi tsmpApi;

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

	public TsmpApi getTsmpApi() {
		return tsmpApi;
	}

	public void setTsmpApi(TsmpApi tsmpApi) {
		this.tsmpApi = tsmpApi;
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

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getApiStatusName() {
		return apiStatusName;
	}

	public void setApiStatusName(String apiStatusName) {
		this.apiStatusName = apiStatusName;
	}

}