package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class AA0318Item {

	/** API ID */
	private AA0318Trunc apiKey;

	/** 模組名稱 */
	private AA0318Trunc moduleName;

	/** API名稱 */
	private AA0318Trunc apiName;

	/** API來源 */
	private AA0318Pair apiSrc;

	/** 端點 */
	private String endpoint;

	/** 檢查結果 */
	private AA0318Pair checkAct;

	/** 描述 */
	private AA0318Trunc memo;
	
	/** Target URL */
	private String srcURL;
	
	/** ip 分流的srcUrl  */
	private  Map<String, String> srcURLByIpRedirectMap;
	
	/** 失敗判定策略 */
	private String failDiscoveryPolicy;
	
	/** 失敗處置策略 */	
	private String failHandlePolicy;

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
	

	private List<String> notifyNameList;

	public String getSrcURL() {
		return srcURL;
	}

	public void setSrcURL(String srcURL) {
		this.srcURL = srcURL;
	}

	public AA0318Trunc getApiKey() {
		return apiKey;
	}

	public void setApiKey(AA0318Trunc apiKey) {
		this.apiKey = apiKey;
	}

	public AA0318Trunc getModuleName() {
		return moduleName;
	}

	public void setModuleName(AA0318Trunc moduleName) {
		this.moduleName = moduleName;
	}

	public AA0318Trunc getApiName() {
		return apiName;
	}

	public void setApiName(AA0318Trunc apiName) {
		this.apiName = apiName;
	}

	public AA0318Pair getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(AA0318Pair apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public AA0318Pair getCheckAct() {
		return checkAct;
	}

	public void setCheckAct(AA0318Pair checkAct) {
		this.checkAct = checkAct;
	}

	public AA0318Trunc getMemo() {
		return memo;
	}

	public void setMemo(AA0318Trunc memo) {
		this.memo = memo;
	}

	public Map<String, String> getSrcURLByIpRedirectMap() {
		return srcURLByIpRedirectMap;
	}

	public void setSrcURLByIpRedirectMap(Map<String, String> srcURLByIpRedirectMap) {
		this.srcURLByIpRedirectMap = srcURLByIpRedirectMap;
	}

    public List<String> getNotifyNameList() {
        return notifyNameList;
    }

    public void setNotifyNameList(List<String> notifyNameList) {
        this.notifyNameList = notifyNameList;
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