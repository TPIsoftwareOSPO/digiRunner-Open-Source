package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0423RespItem {
	private String apiKey;
	private String moduleName;
	private String apiStatus;
	private String apiName;
	private List<AA0423RespSrcUrlListItem> srcUrlList;
	private List<String> labelList;
	private Boolean noOauth;

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

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public Boolean getNoOauth() {
		return noOauth;
	}

	public void setNoOauth(Boolean noOauth) {
		this.noOauth = noOauth;
	}

	public List<AA0423RespSrcUrlListItem> getSrcUrlList() {
		return srcUrlList;
	}

	public void setSrcUrlList(List<AA0423RespSrcUrlListItem> srcUrlList) {
		this.srcUrlList = srcUrlList;
	}

	public List<String> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}

}
