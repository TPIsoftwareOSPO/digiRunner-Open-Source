package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0301Req extends ReqValidator{

	/** 模組名稱	*/
	private String moduleName;
	
	/** API ID*/
	private String apiKey;
	
	/** 關鍵字*/
	private String keyword;
	
	/** API來源*/
	private List<String> apiSrc;
	
	/** API狀態	*/
	private String apiStatus;
	
	/** 開放權限	*/
	private String publicFlag;
	
	/** JWT設定(Req)	*/
	private String jweFlag;
	
	/** JWT設定(Resp)*/
	private String jweFlagResp;
	
	/** 排序設定	*/
	private Map<String, String> sortBy;
	
	/** 是否分頁 */
	private String paging;

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

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(List<String> apiSrc) {
		this.apiSrc = apiSrc;
	}
	
	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}
	
	public String getJweFlag() {
		return jweFlag;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}
	
	public String getJweFlagResp() {
		return jweFlagResp;
	}

	public Map<String, String> getSortBy() {
		return sortBy;
	}

	public void setSortBy(Map<String, String> sortBy) {
		this.sortBy = sortBy;
	}

	public String getPaging() {
		return paging;
	}

	public void setPaging(String paging) {
		this.paging = paging;
	}

	@Override
	public String toString() {
		return "AA0301Req [moduleName=" + moduleName + ", apiKey=" + apiKey + ", keyword=" + keyword + ", apiSrc="
				+ apiSrc + ", apiStatus=" + apiStatus + ", publicFlag=" + publicFlag + ", jweFlag=" + jweFlag
				+ ", jweFlagResp=" + jweFlagResp + ", sortBy=" + sortBy + ", paging=" + paging + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildMap(locale)
					.field("sortBy")
					.max(1)
					.build()
			});
	}
}
