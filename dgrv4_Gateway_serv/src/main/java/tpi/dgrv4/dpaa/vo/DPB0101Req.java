package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0101Req extends ReqValidator {

	/** 名稱 */
	private String rjobName;

	/** 備註 */
	private String remark;

	/** 週期設定 */
	private DPB0101Cron cronJson;

	/** 開始日期	yyyy/MM/dd HH:mm:ss */
	private String effDateTime;

	/** 結束日期	yyyy/MM/dd HH:mm:ss */
	private String invDateTime;

	/** 執行內容 */
	private List<DPB0101Items> rjobItems;

	public String getRjobName() {
		return rjobName;
	}

	public void setRjobName(String rjobName) {
		this.rjobName = rjobName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public DPB0101Cron getCronJson() {
		return cronJson;
	}

	public void setCronJson(DPB0101Cron cronJson) {
		this.cronJson = cronJson;
	}

	public String getEffDateTime() {
		return effDateTime;
	}

	public void setEffDateTime(String effDateTime) {
		this.effDateTime = effDateTime;
	}

	public String getInvDateTime() {
		return invDateTime;
	}

	public void setInvDateTime(String invDateTime) {
		this.invDateTime = invDateTime;
	}

	public List<DPB0101Items> getRjobItems() {
		return rjobItems;
	}

	public void setRjobItems(List<DPB0101Items> rjobItems) {
		this.rjobItems = rjobItems;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("rjobName")
				.isRequired()
				.maxLength(30)
				.build()
		});
	}

}