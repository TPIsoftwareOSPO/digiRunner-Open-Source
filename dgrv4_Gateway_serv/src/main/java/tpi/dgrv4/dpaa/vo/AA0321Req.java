package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0321Req {

	/** PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String apiKey;

	/** PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String moduleName;

	/** 模糊搜尋 每一個字串可以使用"空白鍵" 隔開 */
	private String keyword;
	
	/** 已經挑選API清單需要過濾掉 */
	private List<String> apiUidList;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}
	
	public String getKeyword() {
		return keyword;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getApiUidList() {
		return apiUidList;
	}

	public void setApiUidList(List<String> apiUidList) {
		this.apiUidList = apiUidList;
	}

}
