package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0234Req extends ReqValidator{

	/** PK */
	private String apiKey;
	
	/** PK */
	private String moduleName;
	
	/** 模糊搜尋 */
	private String keyword;
	
	/** 已經選擇的apiKey */
	private List<String> selectedApiKeyList;

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getSelectedApiKeyList() {
		return selectedApiKeyList;
	}

	public void setSelectedApiKeyList(List<String> selectedApiKeyList) {
		this.selectedApiKeyList = selectedApiKeyList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("moduleName")
					.isRequired()
					.build()
				
			});
	}


}
