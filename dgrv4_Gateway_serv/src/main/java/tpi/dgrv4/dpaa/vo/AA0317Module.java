package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0317Module {

	/** 模組名稱 */
	private String moduleName;

	/** 註冊模組資訊		[API來源]為註冊(R)，且模組存在於 TSMP_REG_MODULE 時才會有此資料 */
	private AA0317RegModule regModule;

	/** API清單 */
	private List<AA0317RespItem> apiList;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public AA0317RegModule getRegModule() {
		return regModule;
	}

	public void setRegModule(AA0317RegModule regModule) {
		this.regModule = regModule;
	}

	public List<AA0317RespItem> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0317RespItem> apiList) {
		this.apiList = apiList;
	}

}