package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0313Req extends ReqValidator {

	/** API ID, PK, TSMP_API.api_key */
	private String apiKey;

	/** 模組名稱, PK, TSMP_API.module_name */
	private String moduleName;

	/** API名稱, TSMP_API.api_name */
	private String apiName;

	/** 狀態, "1"=啟用, "2"=停用, TSMP_API.api_status */
	private String apiStatus;

	/** JWT設定(Request), bcrypt加密，ITEM_NO = 'API_JWT_FLAG', TSMP_API.jwe_flag */
	private String jweFlag;

	/** JWT設定(Response), bcrypt加密，ITEM_NO = 'API_JWT_FLAG', TSMP_API.jwe_flag_resp */
	private String jweFlagResp;

	/** 來源URL的協定, 可以是 "https" 或 "http" (apiSrc = "C" 時，前端不會傳入值) */
	private String protocol;

	/** 來源URL, TSMP_API_REG.src_url */
	private String srcUrl;

	/** Path Parameter, TSMP_API_REG.url_rid, FALSE: unchecked, TRUE: checked */
	private Boolean urlRID;

	/** No OAuth, TSMP_API_REG.no_oauth, FALSE: unchecked, TRUE: checked */
	private Boolean noOAuth;

	/** 功能設定, TSMP_API_REG.fun_flag */
	private AA0313Func funFlag;

	/** Http Method, TSMP_API_REG.method_of_json, ex: ["POST","GET"] */
	private List<String> methodOfJson;

	/** 資料格式, TSMP_API.data_format, bcrypt加密, ITEM_NO = 'API_DATA_FORMAT' */
	private String dataFormat;

	/** 註冊主機ID, TSMP_API_REG.reghost_id，若傳入空字串則後端轉為 null */
	private String reghostId;

	/** API說明, TSMP_API.api_desc */
	private String apiDesc;
	
	private String apiCacheFlag;
	
	/** mock 狀態碼, TSMP_API.mock_status_code, 只為三個數字 */
	private String mockStatusCode;
	
	/** mock headers, TSMP_API.mock_headers, 有值才轉成 json 或空值存入 DB */
	private List<AA0313KeyVal> mockHeaders;
	
	/** mock body, TSMP_API.mock_body, 最長 2000 */ 
	private String mockBody;
	
	/**  是否依據IP分流 */
	private Boolean redirectByIp;
	
	private List<AA0313RedirectByIpData> redirectByIpDataList;
	/** header 遮罩資訊*/
	private String headerMaskKey;
	private String headerMaskPolicy;
	private Integer headerMaskPolicyNum;
	private String headerMaskPolicySymbol;

	private String bodyMaskKeyword;
	private String bodyMaskPolicy;
	private Integer bodyMaskPolicyNum;
	private String bodyMaskPolicySymbol;
	
	/** 失敗判定策略 */
	private String failDiscoveryPolicy;
	
	/** 失敗處置策略 */	
	private String failHandlePolicy;
	
	private List<String> labelList;
	private Integer fixedCacheTime;
	
	/** webhook notify list */
	private List<String> notifyNameList;
	
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
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getJweFlag() {
		return jweFlag;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}
	
	public String getApiStatus() {
		return apiStatus;
	}

	public String getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public Boolean getUrlRID() {
		return urlRID;
	}

	public void setUrlRID(Boolean urlRID) {
		this.urlRID = urlRID;
	}

	public Boolean getNoOAuth() {
		return noOAuth;
	}

	public void setNoOAuth(Boolean noOAuth) {
		this.noOAuth = noOAuth;
	}

	public AA0313Func getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(AA0313Func funFlag) {
		this.funFlag = funFlag;
	}

	public List<String> getMethodOfJson() {
		return methodOfJson;
	}

	public void setMethodOfJson(List<String> methodOfJson) {
		this.methodOfJson = methodOfJson;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getApiCacheFlag() {
		return apiCacheFlag;
	}

	public void setApiCacheFlag(String apiCacheFlag) {
		this.apiCacheFlag = apiCacheFlag;
	}

	public String getReghostId() {
		return reghostId;
	}

	public void setReghostId(String reghostId) {
		this.reghostId = reghostId;
	}

	public String getMockStatusCode() {
		return mockStatusCode;
	}

	public void setMockStatusCode(String mockStatusCode) {
		this.mockStatusCode = mockStatusCode;
	}

	public List<AA0313KeyVal> getMockHeaders() {
		return mockHeaders;
	}

	public void setMockHeaders(List<AA0313KeyVal> mockHeaders) {
		this.mockHeaders = mockHeaders;
	}

	public String getMockBody() {
		return mockBody;
	}

	public void setMockBody(String mockBody) {
		this.mockBody = mockBody;
	}

	public Boolean getRedirectByIp() {
		return redirectByIp;
	}

	public void setRedirectByIp(Boolean redirectByIp) {
		this.redirectByIp = redirectByIp;
	}

	public List<AA0313RedirectByIpData> getRedirectByIpDataList() {
		return redirectByIpDataList;
	}

	public void setRedirectByIpDataList(List<AA0313RedirectByIpData> redirectByIpDataList) {
		this.redirectByIpDataList = redirectByIpDataList;
	}

	public String getHeaderMaskKey() {
		return headerMaskKey;
	}

	public void setHeaderMaskKey(String headerMaskKey) {
		this.headerMaskKey = headerMaskKey;
	}

	public String getHeaderMaskPolicy() {
		return headerMaskPolicy;
	}

	public void setHeaderMaskPolicy(String headerMaskPolicy) {
		this.headerMaskPolicy = headerMaskPolicy;
	}

	public Integer getHeaderMaskPolicyNum() {
		return headerMaskPolicyNum;
	}

	public void setHeaderMaskPolicyNum(Integer headerMaskPolicyNum) {
		this.headerMaskPolicyNum = headerMaskPolicyNum;
	}

	public String getHeaderMaskPolicySymbol() {
		return headerMaskPolicySymbol;
	}

	public void setHeaderMaskPolicySymbol(String headerMaskPolicySymbol) {
		this.headerMaskPolicySymbol = headerMaskPolicySymbol;
	}

	public String getBodyMaskKeyword() {
		return bodyMaskKeyword;
	}

	public void setBodyMaskKeyword(String bodyMaskKeyword) {
		this.bodyMaskKeyword = bodyMaskKeyword;
	}

	public String getBodyMaskPolicy() {
		return bodyMaskPolicy;
	}

	public void setBodyMaskPolicy(String bodyMaskPolicy) {
		this.bodyMaskPolicy = bodyMaskPolicy;
	}

	public Integer getBodyMaskPolicyNum() {
		return bodyMaskPolicyNum;
	}

	public void setBodyMaskPolicyNum(Integer bodyMaskPolicyNum) {
		this.bodyMaskPolicyNum = bodyMaskPolicyNum;
	}

	public String getBodyMaskPolicySymbol() {
		return bodyMaskPolicySymbol;
	}

	public void setBodyMaskPolicySymbol(String bodyMaskPolicySymbol) {
		this.bodyMaskPolicySymbol = bodyMaskPolicySymbol;
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

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiKey")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("moduleName")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiName")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiStatus")
				.isRequired()
				.maxLength(1)
				.minLength(1)
				.pattern("^[1|2]$")
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("jweFlag")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("jweFlagResp")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("protocol")
				.pattern(RegexpConstant.HTTP_SCHEME)
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiCacheFlag")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("mockStatusCode")
				.maxLength(3)
				.pattern("^[0-9]{3}$",TsmpDpAaRtnCode._2021.getCode(), new String[] {})
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("mockBody")
				.maxLength(2000)
				.build(),
		});
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