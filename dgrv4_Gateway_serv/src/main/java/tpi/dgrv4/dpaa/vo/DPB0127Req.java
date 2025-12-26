package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0127Req extends ReqValidator{
	private Long auditLogId;
	private Long auditExtId;
	private String startDate;
	private String endDate;
	private String keywords;

	public Long getAuditLogId() {
		return auditLogId;
	}
 
	public void setAuditLogId(Long auditLogId) {
		this.auditLogId = auditLogId;
	}

	public Long getAuditExtId() {
		return auditExtId;
	}

	public void setAuditExtId(Long auditExtId) {
		this.auditExtId = auditExtId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("startDate")
				.isRequired()
				.pattern("^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$")
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("endDate")
				.isRequired()
				.pattern("^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$")
				.build()
			});
	}

}