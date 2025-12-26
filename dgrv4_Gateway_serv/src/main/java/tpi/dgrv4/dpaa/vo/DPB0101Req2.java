package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0101Req2 extends ReqValidator {

	/** 備註 */
	private String remark;

	/** 排序 */
	private Integer sortBy;

	/** Params */
	private String inParams;

	/** 識別資料 */
	private String identifData;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}
	
	public Integer getSortBy() {
		return sortBy;
	}

	public String getInParams() {
		return inParams;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}
	
	public String getIdentifData() {
		return identifData;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("remark")
				.maxLength(150)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("sortBy")
				.isRequired()
				.min(0)
				.max(Integer.MAX_VALUE)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("inParams")
				.maxLength(2000)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("identifData")
				.maxLength(2000)
				.build()
		});
	}
}
