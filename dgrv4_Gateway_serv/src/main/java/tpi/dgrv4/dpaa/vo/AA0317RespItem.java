package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0317RespItem {

	/** API Key */
	private String apiKey;

	/** Module名稱 */
	private String moduleName;

	/** API名稱 */
	private String apiName;

	/** API說明 */
	private String apiDesc;

	/** API擁有者 */
	private String apiOwner;

	/** ResourceID in URL, Source URL中是否會有ResourceID. "0": 沒有; "1":有 */
	private String urlRID;

	/** Source, R':Registered, 'C': Composed */
	private String apiSrc;

	/** 來源URL, (Registered API only) */
	private String srcURL;

	/** API UUID, (Composered API only) */
	private String apiUUID;

	/** CONSUMES */
	private String contentType;

	/** End Point */
	private String enpoint;

	/** HEADERS */
	private String httpHeader;

	/** Http Method */
	private String httpMethod;

	/** PARAMS */
	private String params;

	/** PRODUCES */
	private String produce;

	/** Composer Flow, (composer flow configuration in mongodb) */
	private String flow;

	/** OAuth轉導設定, 0 : tsmpg 不轉導 , 1: tsmpg 允許轉導, default : 0 */
	private String no_oauth;

	/** 有無JWE FLAG(Request), 0：不使用, 1：JWE, 2：JWS, null:不使用 */
	private String jweFlag;

	/** 有無JWE FLAG(Response), 0：不使用, 1：JWE, 2：JWS, null:不使用 */
	private String jweFlagResp;

	/** 功能設定, 1:tokenpayload:會將token的payload往後帶 */
	private Integer funFlag;
	
	/** Mock 狀態碼 */
	private String mockStatusCode;

	/** Mock Headers 的 Json 格式 */
	private String mockHeaders;

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

	private String isRedirectByIp;

	/** 失敗判定策略 */
	private String failDiscoveryPolicy;
	
	/** 失敗處置策略 */	
	private String failHandlePolicy;
	
	private List<AA0317RedirectByIpData> redirectByIpDataList;
	
	private List<String> labelList;
	
	private String apiCacheFlag;
	private Integer fixedCacheTime;
	private String apiStatus;
	
	private String publicFlag;
	private String apiReleaseTime;
	
	private String scheduledLaunchDate;
	private String scheduledRemovalDate;
	private String enableScheduledDate;
	private String disableScheduledDate;

	private  List<String> notifyNameList;
	

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
	
	public String getApiKey() {
		return apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getApiOwner() {
		return apiOwner;
	}
	
	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiOwner(String apiOwner) {
		this.apiOwner = apiOwner;
	}

	public String getUrlRID() {
		return urlRID;
	}

	public void setUrlRID(String urlRID) {
		this.urlRID = urlRID;
	}

	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getSrcURL() {
		return srcURL;
	}

	public void setSrcURL(String srcURL) {
		this.srcURL = srcURL;
	}

	public String getApiUUID() {
		return apiUUID;
	}

	public void setApiUUID(String apiUUID) {
		this.apiUUID = apiUUID;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEnpoint() {
		return enpoint;
	}

	public void setEnpoint(String enpoint) {
		this.enpoint = enpoint;
	}

	public String getHttpHeader() {
		return httpHeader;
	}

	public void setHttpHeader(String httpHeader) {
		this.httpHeader = httpHeader;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getProduce() {
		return produce;
	}

	public void setProduce(String produce) {
		this.produce = produce;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public String getNo_oauth() {
		return no_oauth;
	}

	public void setNo_oauth(String no_oauth) {
		this.no_oauth = no_oauth;
	}

	public String getJweFlag() {
		return jweFlag;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}

	public String getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public Integer getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(Integer funFlag) {
		this.funFlag = funFlag;
	}

	public String getMockStatusCode() {
		return mockStatusCode;
	}

	public void setMockStatusCode(String mockStatusCode) {
		this.mockStatusCode = mockStatusCode;
	}

	public String getMockHeaders() {
		return mockHeaders;
	}

	public void setMockHeaders(String mockHeaders) {
		this.mockHeaders = mockHeaders;
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



	public List<AA0317RedirectByIpData> getRedirectByIpDataList() {
		return redirectByIpDataList;
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

	public void setRedirectByIpDataList(List<AA0317RedirectByIpData> redirectByIpDataList) {
		this.redirectByIpDataList = redirectByIpDataList;
	}

	public String getIsRedirectByIp() {
		return isRedirectByIp;
	}

	public void setIsRedirectByIp(String isRedirectByIp) {
		this.isRedirectByIp = isRedirectByIp;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public String getApiCacheFlag() {
		return apiCacheFlag;
	}

	public void setApiCacheFlag(String apiCacheFlag) {
		this.apiCacheFlag = apiCacheFlag;
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

	public String getFailHandlePolicy() {
		return failHandlePolicy;
	}

	public void setFailHandlePolicy(String failHandlePolicy) {
		this.failHandlePolicy = failHandlePolicy;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getApiReleaseTime() {
		return apiReleaseTime;
	}

	public void setApiReleaseTime(String apiReleaseTime) {
		this.apiReleaseTime = apiReleaseTime;
	}

	public String getScheduledLaunchDate() {
		return scheduledLaunchDate;
	}

	public void setScheduledLaunchDate(String scheduledLaunchDate) {
		this.scheduledLaunchDate = scheduledLaunchDate;
	}

	public String getScheduledRemovalDate() {
		return scheduledRemovalDate;
	}

	public void setScheduledRemovalDate(String scheduledRemovalDate) {
		this.scheduledRemovalDate = scheduledRemovalDate;
	}

	public String getEnableScheduledDate() {
		return enableScheduledDate;
	}

	public void setEnableScheduledDate(String enableScheduledDate) {
		this.enableScheduledDate = enableScheduledDate;
	}

	public String getDisableScheduledDate() {
		return disableScheduledDate;
	}

	public void setDisableScheduledDate(String disableScheduledDate) {
		this.disableScheduledDate = disableScheduledDate;
	}

    public List<String> getNotifyNameList() {
        return notifyNameList;
    }

    public void setNotifyNameList(List<String> notifyNameList) {
        this.notifyNameList = notifyNameList;
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