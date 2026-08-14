package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0428Req extends ReqValidator{
	/** 模組名稱	*/
	private String moduleName;
	
	/** API ID*/
	private String apiKey;
	
	/** 標籤 */
	private List<String> labelList;
	private List<String> apiSrc;
	
	/** 是否分頁 */
	private String paging;

	private String include;

	public String getModuleName() {
		return moduleName;
	}


	public String getApiKey() {
		return apiKey;
	}


	public List<String> getLabelList() {
		return labelList;
	}


	public String getPaging() {
		return paging;
	}


	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}


	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public void setLabelList(List<String> labelList) {
		this.labelList = labelList;
	}


	public void setPaging(String paging) {
		this.paging = paging;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}


	public List<String> getApiSrc() {
		return apiSrc;
	}


	public void setApiSrc(List<String> apiSrc) {
		this.apiSrc = apiSrc;
	}


	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("labelList")
					.min(1)
					.build()
			});
	}

}
