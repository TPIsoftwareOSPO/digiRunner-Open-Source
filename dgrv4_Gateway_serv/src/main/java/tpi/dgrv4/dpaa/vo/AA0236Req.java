package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0236Req extends ReqValidator{

	// 群組代碼(PK)
	private String groupId;

	// API代碼(PK)
	private String apiKey;

	// 模組名稱(PK)
	private String moduleName;

	// 模糊搜尋
	private String keyword;

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getGroupId() {
		return groupId;
	}

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
	

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupId")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("moduleName")
					.isRequired()
					.build()
			});
	}


}
