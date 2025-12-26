package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0237RespA {
	
	/** 模組名稱清單*/
	private List<String> moduleNameList ;

	public List<String> getModuleNameList() {
		return moduleNameList;
	}

	public void setModuleNameList(List<String> moduleNameList) {
		this.moduleNameList = moduleNameList;
	}

	@Override
	public String toString() {
		return "AA0237RespA [moduleNameList=" + moduleNameList + "]";
	}
	
	
}
