package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
	
public class AA1202Req extends ReqValidator {

	private String endDate;
	private String timeType;
	private List<String> apiUidList;
	private String startDate;
	private String startHour;
	private String endHour;

	public String getTimeType() {
		return timeType;
	}

	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}


	public List<String> getApiUidList() {
		return apiUidList;
	}

	public void setApiUidList(List<String> apiUidList) {
		this.apiUidList = apiUidList;
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

	public String getStartDate() {
		return startDate;
	}
	
	public String getStartHour() {
		return startHour;
	}

	public void setStartHour(String startHour) {
		this.startHour = startHour;
	}

	public String getEndHour() {
		return endHour;
	}

	public void setEndHour(String endHour) {
		this.endHour = endHour;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("endDate")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("timeType")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("startDate")
					.isRequired()
					.build()
		});
	}
}
