package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0303Req {

	/** 是否忽略警示訊, "Y"=是, "N"=否, 前端只有 [刪除] 時才會傳入 "N" */
	private String ignoreAlert;

	/** API清單 */
	private List<AA0303Item> apiList;

	/** API狀態, 0:Delete(真刪除，非改狀態) 1: Enabled, 2:Disabled */
	private String apiStatus;

	/** JWT設定 (Request), 0:不使用, 1:JWE, 2:JWS */
	private String jweFlag;

	/** JWT設定 (Response) */
	private String jweFlagResp;

	private long scheduledDate;

	public long getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(long scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public String getIgnoreAlert() {
		return ignoreAlert;
	}

	public void setIgnoreAlert(String ignoreAlert) {
		this.ignoreAlert = ignoreAlert;
	}

	public List<AA0303Item> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0303Item> apiList) {
		this.apiList = apiList;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}

	public String getJweFlag() {
		return jweFlag;
	}

	public String getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

}