package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0066OpenAPIKeyReq2 extends ReqValidator{
	
	/** 申請說明 */
	private String reqDesc;
	
	private String effectiveDate;
	
	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 使用次數上限 */
	private Integer timesThreshold;
	
	private String expiredAt;
	
	
	public String getReqDesc() {
		return reqDesc;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}
	
	public String getEffectiveDate() {
		return effectiveDate;
	}

	public Integer getTimesThreshold() {
		return timesThreshold;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}
	
	public String getExpiredAt() {
		return expiredAt;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("reqDesc")
				.isRequired()
				.maxLength(500)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("openApiKeyAlias")
				.isRequired()
				.maxLength(255)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("effectiveDate")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("timesThreshold")
				.isRequired()
				.min(-1)
				.max(Integer.MAX_VALUE)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("expiredAt")
				.isRequired()
				.build()
			});
	}
	
}
