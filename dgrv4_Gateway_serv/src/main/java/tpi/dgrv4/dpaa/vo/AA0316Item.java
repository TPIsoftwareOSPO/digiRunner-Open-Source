package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0316Item {

	/** Http Method, ex: ["POST","GET"] */
	private List<String> methods;

	/** 完整的來源URL */
	private String srcUrl;

	/** API ID (原 API Key), TSMP_API_REG.api_key */
	private String apiId;

	/** 資料格式, bcrypt加密, ITEM_NO = 'API_DATA_FORMAT' */
	private String dataFormat;

	/** API說明, TSMP_API.api_desc */
	private String apiDesc;

	/** JWT設定(Request), bcrypt加密, ITEM_NO = 'API_JWT_FLAG' */
	private String jweFlag;

	/** JWT設定(Response), bcrypt加密, ITEM_NO = 'API_JWT_FLAG' */
	private String jweFlagResp;

	/** Path Parameter */
	private Boolean urlRID;

	/** No OAuth */
	private Boolean noOAuth;

	/** 功能設定 */
	private AA0316Func funFlag;

	/** Request Content Type, ex: ["application/json","application/xml"] */
	private List<String> consumes;

	/** Response Cotent Type, ex: ["application/json","application/xml"] */
	private List<String> produces;

	/** Http Headers */
	private List<String> headers;

	/** Parameters */
	private List<String> params;
	
	private String moduleName;
	
	private String summary;

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
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

	public AA0316Func getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(AA0316Func funFlag) {
		this.funFlag = funFlag;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}