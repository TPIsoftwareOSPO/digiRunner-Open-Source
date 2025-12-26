package tpi.dgrv4.entity.vo;

import java.util.List;

public class AA0419SearchCriteria {

	// 分頁用
	private String lastModuleName;

	private boolean isV2;

	private String[] keyword;

	private List<String> orgIdList;

	public String getLastModuleName() {
		return lastModuleName;
	}

	public void setLastModuleName(String lastModuleName) {
		this.lastModuleName = lastModuleName;
	}

	public boolean isV2() {
		return isV2;
	}

	public void setV2(boolean isV2) {
		this.isV2 = isV2;
	}

	public String[] getKeyword() {
		return keyword;
	}

	public void setKeyword(String[] keyword) {
		this.keyword = keyword;
	}

	public List<String> getOrgIdList() {
		return orgIdList;
	}

	public void setOrgIdList(List<String> orgIdList) {
		this.orgIdList = orgIdList;
	}

}