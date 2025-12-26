package tpi.dgrv4.entity.vo;

import java.util.List;

import tpi.dgrv4.entity.entity.TsmpApiId;

public class DPB0018SearchCriteria {

	private TsmpApiId lastId;

	private String[] words;

	private Integer pageSize;

	private String apiStatus;

	private String publicFlag;

	private List<String> orgDescList;

	public DPB0018SearchCriteria() {}

	public TsmpApiId getLastId() {
		return lastId;
	}

	public void setLastId(TsmpApiId lastId) {
		this.lastId = lastId;
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
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

	public List<String> getOrgDescList() {
		return orgDescList;
	}

	public void setOrgDescList(List<String> orgDescList) {
		this.orgDescList = orgDescList;
	}

}
