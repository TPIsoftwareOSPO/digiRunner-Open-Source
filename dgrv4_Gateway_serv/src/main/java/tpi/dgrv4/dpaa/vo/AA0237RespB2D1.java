package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0237RespB2D1 {
	
	/** 模組名稱*/
	private String moduleName;
	
	/** apiKeyList*/
	private List<AA0237RespB2D2> apiKeyList;
	
	/** apiKeyList是否被截斷*/
	private Boolean isApiKeyTrunc;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public List<AA0237RespB2D2> getApiKeyList() {
		return apiKeyList;
	}

	public void setApiKeyList(List<AA0237RespB2D2> apiKeyList) {
		this.apiKeyList = apiKeyList;
	}

	public Boolean getIsApiKeyTrunc() {
		return isApiKeyTrunc;
	}

	public void setIsApiKeyTrunc(Boolean isApiKeyTrunc) {
		this.isApiKeyTrunc = isApiKeyTrunc;
	}

	@Override
	public String toString() {
		return "AA0237RespB2D1 [moduleName=" + moduleName + ", apiKeyList=" + apiKeyList + ", isApiKeyTrunc="
				+ isApiKeyTrunc + "]";
	}

	
}
