package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0425RespItem {
	private int sort;
	private String apiKey;
	private String moduleName;
	private String apiName;
	private String apiStatus;
	private Boolean noAuth;
	private List<String> labelList;
	private List<AA0425RespIpAndSrcUrlList> oldSrcUrlList;
	private List<AA0425RespNewSrcUrlItem> newSrcUrlList;

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public List<AA0425RespIpAndSrcUrlList> getOldSrcUrlList() {
		return oldSrcUrlList;
	}

	public void setOldSrcUrlList(List<AA0425RespIpAndSrcUrlList> oldSrcUrlList) {
		this.oldSrcUrlList = oldSrcUrlList;
	}



	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public Boolean getNoAuth() {
		return noAuth;
	}

	public void setNoAuth(Boolean noAuth) {
		this.noAuth = noAuth;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

	public List<AA0425RespNewSrcUrlItem> getNewSrcUrlList() {
		return newSrcUrlList;
	}

	public void setNewSrcUrlList(List<AA0425RespNewSrcUrlItem> newSrcUrlList) {
		this.newSrcUrlList = newSrcUrlList;
	}

}
