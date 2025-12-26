package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0511Req extends ReqValidator {

	/** 授權類型, 在「API組合與設計」功能中呼叫時，前端固定傳入字串"Composer" */
	private String authType;

	/** 主類別, 由 authType 決定是否必填。authType = ""Composer"" 時，此欄位填入 [模組名稱] */
	private String resource;

	/** 次類別, 由 authType 決定是否必填。authType = ""Composer"" 時，此欄位填入 [API ID] */
	private String subclass;

	public AA0511Req() {
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getSubclass() {
		return subclass;
	}

	public void setSubclass(String subclass) {
		this.subclass = subclass;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
			.buildString(locale)
			.field("authType")
			.isRequired()
			.maxLength(20)
			.pattern("[a-zA-Z0-9_-]*", TsmpDpAaRtnCode._2008.getCode(), null)
			.build()
		});
	}

}