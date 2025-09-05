package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0302Resp {
	/** API狀態 */
	private AA0302Pair apiStatus;

	/** 模組編號 */
	private Long moduleId;

	/** 模組名稱 */
	private AA0302Trunc moduleName;

	/** API ID */
	private AA0302Trunc apiKey;

	/** API名稱 */
	private AA0302Trunc apiName;

	/** API來源 */
	private AA0302Pair apiSrc;

	/** JWT設定(Request) */
	private AA0302Pair jweFlag;

	/** JWT設定(Response) */
	private AA0302Pair jweFlagResp;

	/** Http Method */
	private String methodOfJson;

	/** 端點 */
	private AA0302Trunc pathOfJson;

	/** 來源URL的協定 */
	private String protocol;

	/** 來源URL */
	private AA0302Trunc srcUrl;

	/** Path Parameter */
	private String urlRID;

	/** Path Parameter */
	private String noOAuth;

	/** Token Payload */
	private Integer funFlag;

	/** 資料格式 */
	private AA0302Pair dataFormat;

	/** 表頭 */
	private AA0302Trunc headersOfJson;

	/** 參數值 */
	private AA0302Trunc paramsOfJson;

	/** CONSUMES */
	private AA0302Trunc consumesOfJson;

	/** PRODUCES */
	private AA0302Trunc producesOfJson;

	/** API說明 */
	private String apiDesc;

	/** API UUID */
	private String apiUUID;

	/** 組織單位ID */
	private String orgId;

	/** 組織單位名稱 */
	private String orgName;

	/** 欄位控制項 */
	private AA0302Controls controls;

	private AA0302Pair apiCacheFlag;

	private String pathType;

	private String dgrPath;

	/** Mock 狀態碼 */
	private String mockStatusCode;

	/** Mock Headers 更新用，從 Json 轉回 */
	private List<AA0302KeyVal> mockHeaders;

	/** Mock Headers 的 Json 格式(明細用) */
	private String mockHeadersOfJson;

	/** Mock 表身 */
	private String mockBody;

	private String headerMaskKey;
	private String headerMaskPolicy;
	private Integer headerMaskPolicyNum;
	private String headerMaskPolicySymbol;

	private String bodyMaskKeyword;
	private String bodyMaskPolicy;
	private Integer bodyMaskPolicyNum;
	private String bodyMaskPolicySymbol;

	private Boolean isRedirectByIp;

	// 模式, "0":tsmpc , "1":dgrc
	private String type;

	/** 失敗判定策略 */
	private String failDiscoveryPolicy;

	/** 失敗處置策略 */
	private String failHandlePolicy;
	
	/** webhook notify list */
	private List<String> notifyNameList;

	private List<String> labelList;
	private List<AA0302RedirectByIpData> redirectByIpDataList;
	private Integer fixedCacheTime;

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
	
	// CORS header
	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Origin"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Origin"
	 */
	private String isCorsAllowOrigin;

	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Methods"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Methods"
	 */
	private String isCorsAllowMethods;

	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Headers"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Headers"
	 */
	private String isCorsAllowHeaders;

	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Origin" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Origin" of the CORS Header
	 */
	private String corsAllowOrigin;
	
	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Methods" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Methods" of the CORS Header
	 */
	private String corsAllowMethods;

	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Headers" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Headers" of the CORS Header
	 */
	private String corsAllowHeaders;

	@Override
	public String toString() {
		return "AA0302Resp [apiStatus=" + apiStatus + ", moduleId=" + moduleId + ", moduleName=" + moduleName
				+ ", apiKey=" + apiKey + ", apiName=" + apiName + ", apiSrc=" + apiSrc + ", jweFlag=" + jweFlag
				+ ", jweFlagResp=" + jweFlagResp + ", methodOfJson=" + methodOfJson + ", pathOfJson=" + pathOfJson
				+ ", protocol=" + protocol + ", srcUrl=" + srcUrl + ", urlRID=" + urlRID + ", noOAuth=" + noOAuth
				+ ", funFlag=" + funFlag + ", dataFormat=" + dataFormat + ", headersOfJson=" + headersOfJson
				+ ", paramsOfJson=" + paramsOfJson + ", consumesOfJson=" + consumesOfJson + ", producesOfJson="
				+ producesOfJson + ", apiDesc=" + apiDesc + ", apiUUID=" + apiUUID + ", orgId=" + orgId + ", orgName="
				+ orgName + ", controls=" + controls + ", apiCacheFlag=" + apiCacheFlag + ", pathType=" + pathType
				+ ", dgrPath=" + dgrPath + ", mockStatusCode=" + mockStatusCode + ", mockHeaders=" + mockHeaders
				+ ", mockHeadersOfJson=" + mockHeadersOfJson + ", mockBody=" + mockBody + ", headerMaskKey="
				+ headerMaskKey + ", headerMaskPolicy=" + headerMaskPolicy + ", headerMaskPolicyNum="
				+ headerMaskPolicyNum + ", headerMaskPolicySymbol=" + headerMaskPolicySymbol + ", bodyMaskKeyword="
				+ bodyMaskKeyword + ", bodyMaskPolicy=" + bodyMaskPolicy + ", bodyMaskPolicyNum=" + bodyMaskPolicyNum
				+ ", bodyMaskPolicySymbol=" + bodyMaskPolicySymbol + ", isRedirectByIp=" + isRedirectByIp + ", type="
				+ type + ", failDiscoveryPolicy=" + failDiscoveryPolicy + ", failHandlePolicy=" + failHandlePolicy
				+ ", notifyNameList=" + notifyNameList + ", labelList=" + labelList + ", redirectByIpDataList="
				+ redirectByIpDataList + ", fixedCacheTime=" + fixedCacheTime + ", enableScheduledDate="
				+ enableScheduledDate + ", disableScheduledDate=" + disableScheduledDate + ", createDate=" + createDate
				+ ", createUser=" + createUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser
				+ ", isCorsAllowOrigin=" + isCorsAllowOrigin + ", isCorsAllowMethods=" + isCorsAllowMethods
				+ ", isCorsAllowHeaders=" + isCorsAllowHeaders + ", corsAllowOrigin=" + corsAllowOrigin
				+ ", corsAllowMethods=" + corsAllowMethods + ", corsAllowHeaders=" + corsAllowHeaders + "]";
	}

	public AA0302Pair getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(AA0302Pair apiStatus) {
		this.apiStatus = apiStatus;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public AA0302Trunc getModuleName() {
		return moduleName;
	}

	public void setModuleName(AA0302Trunc moduleName) {
		this.moduleName = moduleName;
	}

	public AA0302Trunc getApiKey() {
		return apiKey;
	}

	public void setApiKey(AA0302Trunc apiKey) {
		this.apiKey = apiKey;
	}

	public AA0302Trunc getApiName() {
		return apiName;
	}

	public void setApiName(AA0302Trunc apiName) {
		this.apiName = apiName;
	}

	public AA0302Pair getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(AA0302Pair apiSrc) {
		this.apiSrc = apiSrc;
	}

	public AA0302Pair getJweFlag() {
		return jweFlag;
	}

	public void setJweFlag(AA0302Pair jweFlag) {
		this.jweFlag = jweFlag;
	}

	public AA0302Pair getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(AA0302Pair jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public String getMethodOfJson() {
		return methodOfJson;
	}

	public void setMethodOfJson(String methodOfJson) {
		this.methodOfJson = methodOfJson;
	}

	public AA0302Trunc getPathOfJson() {
		return pathOfJson;
	}

	public void setPathOfJson(AA0302Trunc pathOfJson) {
		this.pathOfJson = pathOfJson;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public AA0302Trunc getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(AA0302Trunc srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getUrlRID() {
		return urlRID;
	}

	public void setUrlRID(String urlRID) {
		this.urlRID = urlRID;
	}

	public String getNoOAuth() {
		return noOAuth;
	}

	public void setNoOAuth(String noOAuth) {
		this.noOAuth = noOAuth;
	}

	public Integer getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(Integer funFlag) {
		this.funFlag = funFlag;
	}

	public AA0302Pair getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(AA0302Pair dataFormat) {
		this.dataFormat = dataFormat;
	}

	public AA0302Trunc getHeadersOfJson() {
		return headersOfJson;
	}

	public void setHeadersOfJson(AA0302Trunc headersOfJson) {
		this.headersOfJson = headersOfJson;
	}

	public AA0302Trunc getParamsOfJson() {
		return paramsOfJson;
	}

	public void setParamsOfJson(AA0302Trunc paramsOfJson) {
		this.paramsOfJson = paramsOfJson;
	}

	public AA0302Trunc getConsumesOfJson() {
		return consumesOfJson;
	}

	public void setConsumesOfJson(AA0302Trunc consumesOfJson) {
		this.consumesOfJson = consumesOfJson;
	}

	public AA0302Trunc getProducesOfJson() {
		return producesOfJson;
	}

	public void setProducesOfJson(AA0302Trunc producesOfJson) {
		this.producesOfJson = producesOfJson;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getApiUUID() {
		return apiUUID;
	}

	public void setApiUUID(String apiUUID) {
		this.apiUUID = apiUUID;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public AA0302Controls getControls() {
		return controls;
	}

	public void setControls(AA0302Controls controls) {
		this.controls = controls;
	}

	public AA0302Pair getApiCacheFlag() {
		return apiCacheFlag;
	}

	public void setApiCacheFlag(AA0302Pair apiCacheFlag) {
		this.apiCacheFlag = apiCacheFlag;
	}

	public String getPathType() {
		return pathType;
	}

	public void setPathType(String pathType) {
		this.pathType = pathType;
	}

	public String getDgrPath() {
		return dgrPath;
	}

	public void setDgrPath(String dgrPath) {
		this.dgrPath = dgrPath;
	}

	public String getMockStatusCode() {
		return mockStatusCode;
	}

	public void setMockStatusCode(String mockStatusCode) {
		this.mockStatusCode = mockStatusCode;
	}

	public List<AA0302KeyVal> getMockHeaders() {
		return mockHeaders;
	}

	public void setMockHeaders(List<AA0302KeyVal> mockHeaders) {
		this.mockHeaders = mockHeaders;
	}

	public String getMockHeadersOfJson() {
		return mockHeadersOfJson;
	}

	public void setMockHeadersOfJson(String mockHeadersOfJson) {
		this.mockHeadersOfJson = mockHeadersOfJson;
	}

	public String getMockBody() {
		return mockBody;
	}

	public void setMockBody(String mockBody) {
		this.mockBody = mockBody;
	}

	public String getHeaderMaskKey() {
		return headerMaskKey;
	}

	public String getHeaderMaskPolicy() {
		return headerMaskPolicy;
	}

	public Integer getHeaderMaskPolicyNum() {
		return headerMaskPolicyNum;
	}

	public String getHeaderMaskPolicySymbol() {
		return headerMaskPolicySymbol;
	}

	public String getBodyMaskKeyword() {
		return bodyMaskKeyword;
	}

	public String getBodyMaskPolicy() {
		return bodyMaskPolicy;
	}

	public Integer getBodyMaskPolicyNum() {
		return bodyMaskPolicyNum;
	}

	public String getBodyMaskPolicySymbol() {
		return bodyMaskPolicySymbol;
	}

	public void setHeaderMaskKey(String headerMaskKey) {
		this.headerMaskKey = headerMaskKey;
	}

	public void setHeaderMaskPolicy(String headerMaskPolicy) {
		this.headerMaskPolicy = headerMaskPolicy;
	}

	public void setHeaderMaskPolicyNum(Integer headerMaskPolicyNum) {
		this.headerMaskPolicyNum = headerMaskPolicyNum;
	}

	public void setHeaderMaskPolicySymbol(String headerMaskPolicySymbol) {
		this.headerMaskPolicySymbol = headerMaskPolicySymbol;
	}

	public void setBodyMaskKeyword(String bodyMaskKeyword) {
		this.bodyMaskKeyword = bodyMaskKeyword;
	}

	public void setBodyMaskPolicy(String bodyMaskPolicy) {
		this.bodyMaskPolicy = bodyMaskPolicy;
	}

	public void setBodyMaskPolicyNum(Integer bodyMaskPolicyNum) {
		this.bodyMaskPolicyNum = bodyMaskPolicyNum;
	}

	public void setBodyMaskPolicySymbol(String bodyMaskPolicySymbol) {
		this.bodyMaskPolicySymbol = bodyMaskPolicySymbol;
	}

	public Boolean getIsRedirectByIp() {
		return isRedirectByIp;
	}

	public void setIsRedirectByIp(Boolean isRedirectByIp) {
		this.isRedirectByIp = isRedirectByIp;
	}

	public List<AA0302RedirectByIpData> getRedirectByIpDataList() {
		return redirectByIpDataList;
	}

	public void setRedirectByIpDataList(List<AA0302RedirectByIpData> redirectByIpDataList) {
		this.redirectByIpDataList = redirectByIpDataList;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public Integer getFixedCacheTime() {
		return fixedCacheTime;
	}

	public void setFixedCacheTime(Integer fixedCacheTime) {
		this.fixedCacheTime = fixedCacheTime;
	}

	public String getFailDiscoveryPolicy() {
		return failDiscoveryPolicy;
	}

	public void setFailDiscoveryPolicy(String failDiscoveryPolicy) {
		this.failDiscoveryPolicy = failDiscoveryPolicy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFailHandlePolicy() {
		return failHandlePolicy;
	}

	public void setFailHandlePolicy(String failHandlePolicy) {
		this.failHandlePolicy = failHandlePolicy;
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

	public List<String> getNotifyNameList() {
		return notifyNameList;
	}

	public void setNotifyNameList(List<String> notifyNameList) {
		this.notifyNameList = notifyNameList;
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
	

	public String getIsCorsAllowOrigin() {
		return isCorsAllowOrigin;
	}

	public void setIsCorsAllowOrigin(String isCorsAllowOrigin) {
		this.isCorsAllowOrigin = isCorsAllowOrigin;
	}

	public String getIsCorsAllowMethods() {
		return isCorsAllowMethods;
	}

	public void setIsCorsAllowMethods(String isCorsAllowMethods) {
		this.isCorsAllowMethods = isCorsAllowMethods;
	}

	public String getIsCorsAllowHeaders() {
		return isCorsAllowHeaders;
	}

	public void setIsCorsAllowHeaders(String isCorsAllowHeaders) {
		this.isCorsAllowHeaders = isCorsAllowHeaders;
	}

	public String getCorsAllowOrigin() {
		return corsAllowOrigin;
	}

	public void setCorsAllowOrigin(String corsAllowOrigin) {
		this.corsAllowOrigin = corsAllowOrigin;
	}

	public String getCorsAllowMethods() {
		return corsAllowMethods;
	}

	public void setCorsAllowMethods(String corsAllowMethods) {
		this.corsAllowMethods = corsAllowMethods;
	}

	public String getCorsAllowHeaders() {
		return corsAllowHeaders;
	}

	public void setCorsAllowHeaders(String corsAllowHeaders) {
		this.corsAllowHeaders = corsAllowHeaders;
	}
}
