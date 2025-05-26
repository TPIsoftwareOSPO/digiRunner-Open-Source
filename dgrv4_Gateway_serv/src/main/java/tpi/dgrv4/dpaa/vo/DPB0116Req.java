package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0116Req extends ReqValidator {
	private Long id;
	private String startDate;
	private String endDate;
	private String result;
	private String keyword;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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
				.field("startDate")
				.isRequired()
				.pattern("^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$")
				.build()
			,
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("endDate")
				.isRequired()
				.pattern("^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$")
				.build()
			,
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("result")
				.isRequired()
				.build()
		});
	}
}
