package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0311Req extends ReqValidator {

	/** API來源, R：註冊，C：組合, 從「API註冊-自訂」功能呼叫時，前端固定傳入"R", 從「API組合與設計」功能呼叫則傳入 "C" */
	private String apiSrc;

	/** 來源URL的協定, 可以是 "https" 或 "http" (apiSrc = "C" 時，此欄位不會傳入值) */
	private String protocol;

	/** 來源URL / digiRunner URL, 當 apiSrc = "C" 時，前端將 [digiRunner URL] 的值帶入此欄位；apiSrc = "R" 時則為 [來源 URL] */
	private String srcUrl;

	/** 模組名稱 */
	private String moduleName;

	/** API ID (原 API Key), TSMP_API_REG.api_key */
	private String apiId;

	/** Path Parameter, FALSE: unchecked, TRUE: checked */
	private Boolean urlRID;

	/** No OAuth, FALSE: unchecked, TRUE: checked */
	private Boolean noOAuth;

	/** 功能設定 */
	private AA0311Func funFlag;

	/** Http Method, ex: ["POST","GET"] */
	private List<String> methods;

	/** 資料格式, bcrypt加密, ITEM_NO = 'API_DATA_FORMAT' */
	private String dataFormat;

	/** JWT設定(Request), bcrypt加密, ITEM_NO = 'API_JWT_FLAG' */
	private String jweFlag;

	/** JWT設定(Response), bcrypt加密, ITEM_NO = 'API_JWT_FLAG' */
	private String jweFlagResp;

	/** API說明, TSMP_API.api_desc */
	private String apiDesc;

	/** Request Content Type, 前端不顯示，僅由 [複製] 功能帶入，若未傳入則後端預設empty list */
	private List<String> consumes;

	/** Response Cotent Type, 前端不顯示，僅由 [複製] 功能帶入，若未傳入則後端預設empty list */
	private List<String> produces;

	/** Http Headers, 前端不顯示，僅由 [複製] 功能帶入，若未傳入則後端預設empty list */
	private List<String> headers;

	/** Parameters, 前端不顯示，僅由 [複製] 功能帶入，若未傳入則後端預設empty list */
	private List<String> params;
	
	private String apiName;
	
	private Integer type;
	
	/**  是否依據IP分流 */
	private Boolean redirectByIp;
	
	private List<AA0311RedirectByIpData> redirectByIpDataList;
	
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
	
	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
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

	public AA0311Func getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(AA0311Func funFlag) {
		this.funFlag = funFlag;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
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

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}
	
	public List<String> getProduces() {
		return produces;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}
	
	public List<String> getParams() {
		return params;
	}
	
	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Boolean getRedirectByIp() {
		return redirectByIp;
	}

	public void setRedirectByIp(Boolean redirectByIp) {
		this.redirectByIp = redirectByIp;
	}

	public List<AA0311RedirectByIpData> getRedirectByIpDataList() {
		return redirectByIpDataList;
	}

	public void setRedirectByIpDataList(List<AA0311RedirectByIpData> redirectByIpDataList) {
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

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
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
				.field("apiSrc")
				.isRequired()
				.pattern("^[R|C]$")
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("protocol")
				.pattern(RegexpConstant.HTTP_SCHEME)
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildCollection(locale)
				.field("methods")
				.isRequired()
				.build(),

				
		});
	}
}