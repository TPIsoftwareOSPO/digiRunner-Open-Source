package tpi.dgrv4.entity.vo;

import java.util.List;

import tpi.dgrv4.entity.entity.TsmpApi;

public class AA0301SearchCriteria {

	private TsmpApi lastTsmpApi;

	private String[] keywords;

	private Integer pageSize;
	
	private List<String> orgList;
	
	private List<String> apiSrc;
	
	private String apiStatus;
	
	private String publicFlag;
	
	private String jweFlag;

	private String jweFlagResp;
	
	private String apikey;
	
	private String moduleName;
	
	private String paging;
	
	private String sortColumn;
	
	private String sort;
	
	private List<String> labeList;
	
	public AA0301SearchCriteria() {}

	public TsmpApi getLastTsmpApi() {
		return lastTsmpApi;
	}

	public void setLastTsmpApi(TsmpApi lastTsmpApi) {
		this.lastTsmpApi = lastTsmpApi;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public List<String> getOrgList() {
		return orgList;
	}

	public void setOrgList(List<String> orgList) {
		this.orgList = orgList;
	}

	public List<String> getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(List<String> apiSrc) {
		this.apiSrc = apiSrc;
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

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getPaging() {
		return paging;
	}

	public void setPaging(String paging) {
		this.paging = paging;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public List<String> getLabeList() {
		return labeList;
	}

	public void setLabeList(List<String> labeList) {
		this.labeList = labeList;
	}
	
}
