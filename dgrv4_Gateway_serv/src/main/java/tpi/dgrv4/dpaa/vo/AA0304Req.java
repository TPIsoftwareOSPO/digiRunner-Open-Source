package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0304Req extends ReqValidator {

	/** API ID, PK, TSMP_API.api_key */
	private String apiKey;

	/** 模組名稱, PK, TSMP_API.module_name */
	private String moduleName;

	/** API名稱, TSMP_API.api_name */
	private String apiName;

	/** 狀態, "1"=啟用, "2"=停用, TSMP_API.api_status */
	private String apiStatus;

	/** JWT設定(Request), bcrypt加密，ITEM_NO = 'API_JWT_FLAG', TSMP_API.jwe_flag */
	private String jweFlag;

	/** JWT設定(Response)	, bcrypt加密，ITEM_NO = 'API_JWT_FLAG', TSMP_API.jwe_flag_resp */
	private String jweFlagResp;

	/** API說明, TSMP_API.api_desc */
	private String apiDesc;

	public String getApiKey() {
		return apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getJweFlag() {
		return jweFlag;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}

	public String getJweFlagResp() {
		return jweFlagResp;
	}
	
	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiKey")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("moduleName")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiName")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiStatus")
				.isRequired()
				.maxLength(1)
				.minLength(1)
				.pattern("^[1|2|]$")
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("jweFlag")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("jweFlagResp")
				.isRequired()
				.build()
		});
	}
	
}