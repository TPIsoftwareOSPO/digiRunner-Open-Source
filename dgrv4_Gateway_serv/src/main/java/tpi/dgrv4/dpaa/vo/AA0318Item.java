package tpi.dgrv4.dpaa.vo;

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
}