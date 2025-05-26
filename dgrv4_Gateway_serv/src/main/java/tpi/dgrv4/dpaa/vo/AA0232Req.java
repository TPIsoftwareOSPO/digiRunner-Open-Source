package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0232Req extends ReqValidator{

	/** PK */
	private String logSeq;

	/** 模糊搜尋 */
	private String keyword;

	/** 起始日期,YYYY-MM-DD HH:MM */
	private String startTime;

	/** 結束日期,YYYY-MM-DD HH:MM */
	private String endTime;

	/** 事件類型 */
	private String eventType;

	public String getLogSeq() {
		return logSeq;
	}

	public void setLogSeq(String logSeq) {
		this.logSeq = logSeq;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	
		
	@Override
	public String toString() {
		return "AA0232Req [logSeq=" + logSeq + ", keyword=" + keyword + ", startTime=" + startTime + ", endTime="
				+ endTime + ", eventType=" + eventType + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("startTime")
					.isRequired()
					.pattern("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]")
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("endTime")
					.isRequired()
					.pattern("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]")
					.build()
			});
	}

}
