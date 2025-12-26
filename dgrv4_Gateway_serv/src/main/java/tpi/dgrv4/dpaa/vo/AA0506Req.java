package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0506Req extends ReqValidator{

	/** 報表ID*/
	private String reportID;
	
	/** 時間單位*/
	private String timeRange;

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}

	public String getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(String timeRange) {
		this.timeRange = timeRange;
	}

	@Override
	public String toString() {
		return "AA0506Req [reportID=" + reportID + ", timeRange=" + timeRange + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("reportID")
					.isRequired()
					.maxLength(6)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("timeRange")
					.isRequired()
					.maxLength(1)
					.pattern("[T|W|M|Y]", TsmpDpAaRtnCode._1435.getCode(), null)
					.build()
			});
	}
	
	
	

}
