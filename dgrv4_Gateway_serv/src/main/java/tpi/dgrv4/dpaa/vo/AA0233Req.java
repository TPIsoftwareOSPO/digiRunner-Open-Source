package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0233Req extends ReqValidator {

	private String moduleName;//PK

	private String keyword;//模糊搜尋

	private List<String> selectedModuleNameList;//已經選擇的ModuleName

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}


	public List<String> getSelectedModuleNameList() {
		return selectedModuleNameList;
	}

	public void setSelectedModuleNameList(List<String> selectedModuleNameList) {
		this.selectedModuleNameList = selectedModuleNameList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {

		});
	}
}
