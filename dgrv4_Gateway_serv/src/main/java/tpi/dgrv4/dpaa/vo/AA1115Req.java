package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1115Req {

	// PK
	private String lastGroupAuthoritieId;

	// 模糊搜尋
	private String keyword;

	// 傳入前端已經選擇的groupAuthoritiesId
	private List<String> selectedGroupAuthoritieIdList;

	public String getLastGroupAuthoritieId() {
		return lastGroupAuthoritieId;
	}

	public void setLastGroupAuthoritieId(String lastGroupAuthoritieId) {
		this.lastGroupAuthoritieId = lastGroupAuthoritieId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getSelectedGroupAuthoritieIdList() {
		return selectedGroupAuthoritieIdList;
	}

	public void setSelectedGroupAuthoritieIdList(List<String> selectedGroupAuthoritieIdList) {
		this.selectedGroupAuthoritieIdList = selectedGroupAuthoritieIdList;
	}

}
