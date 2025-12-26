package tpi.dgrv4.entity.vo;

public class AA0237ReqB {
	
	/** 虛擬群組ID*/
	private String moduleName;
	
	/** 是否分頁*/
	private Boolean p = Boolean.TRUE;
	
	/** TSMP_GROUP_API.group_id*/
	private String groupId;
	
	/** API Key*/
	private String apiKey;
	
	/** 關鍵字*/
	private String keyword;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public Boolean getP() {
		return p;
	}

	public void setP(Boolean p) {
		this.p = p;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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

	@Override
	public String toString() {
		return "AA0237ReqB [moduleName=" + moduleName + ", p=" + p + ", groupId=" + groupId + ", apiKey=" + apiKey
				+ ", keyword=" + keyword + "]";
	}
	
	

}
