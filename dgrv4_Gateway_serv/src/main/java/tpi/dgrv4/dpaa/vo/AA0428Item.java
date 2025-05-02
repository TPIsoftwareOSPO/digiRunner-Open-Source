package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0428Item {
	/** 模組名稱 */
	private AA0428Trunc moduleName;

	/** API ID */
	private AA0428Trunc apiKey;

	/** 狀態 */
	private AA0428Pair apiStatus;

	/** API來源 */
	private AA0428Pair apiSrc;

	/** API名稱 */
	private AA0428Trunc apiName;

	/** API說明 */
	private AA0428Trunc apiDesc;

	/** JWT設定(Request) */
	private AA0428Pair jweFlag;

	/** JWT設定(Response) */
	private AA0428Pair jweFlagResp;

	/** 最近異動日期 */
	private String updateTime;

	/** 組織單位 */
	private AA0428Pair org;

	private List<String> labelList;

	/** 預定啟用日期 */
	private long enableScheduledDate;

	/** 預定停用日期 */
	private long disableScheduledDate;
	
	/** 建立日期 */
	private String createDate;
	
	/** 建立人員 */
	private String createUser;
	
	/** 更新日期 */
	private String updateDate;
	
	/** 更新人員 */
	private String updateUser;

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

	public AA0428Trunc getModuleName() {
		return moduleName;
	}

	public AA0428Trunc getApiKey() {
		return apiKey;
	}

	public AA0428Pair getApiStatus() {
		return apiStatus;
	}

	public AA0428Pair getApiSrc() {
		return apiSrc;
	}

	public AA0428Trunc getApiName() {
		return apiName;
	}

	public AA0428Trunc getApiDesc() {
		return apiDesc;
	}

	public AA0428Pair getJweFlag() {
		return jweFlag;
	}

	public AA0428Pair getJweFlagResp() {
		return jweFlagResp;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public AA0428Pair getOrg() {
		return org;
	}

	public void setModuleName(AA0428Trunc moduleName) {
		this.moduleName = moduleName;
	}

	public void setApiKey(AA0428Trunc apiKey) {
		this.apiKey = apiKey;
	}

	public void setApiStatus(AA0428Pair apiStatus) {
		this.apiStatus = apiStatus;
	}

	public void setApiSrc(AA0428Pair apiSrc) {
		this.apiSrc = apiSrc;
	}

	public void setApiName(AA0428Trunc apiName) {
		this.apiName = apiName;
	}

	public void setApiDesc(AA0428Trunc apiDesc) {
		this.apiDesc = apiDesc;
	}

	public void setJweFlag(AA0428Pair jweFlag) {
		this.jweFlag = jweFlag;
	}

	public void setJweFlagResp(AA0428Pair jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public void setOrg(AA0428Pair org) {
		this.org = org;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
}
