package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9912Req extends ReqValidator{
	
	private String settingNo;
	private String settingName;
	private String subsettingNo;
	private String subsettingName;
	private Integer sortBy;
	private String isDefault;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	
	public String getSettingNo() {
		return settingNo;
	}
	public void setSettingNo(String settingNo) {
		this.settingNo = settingNo;
	}
	public String getSettingName() {
		return settingName;
	}
	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}
	public String getSubsettingNo() {
		return subsettingNo;
	}
	public void setSubsettingNo(String subsettingNo) {
		this.subsettingNo = subsettingNo;
	}
	public String getSubsettingName() {
		return subsettingName;
	}
	public void setSubsettingName(String subsettingName) {
		this.subsettingName = subsettingName;
	}
	public Integer getSortBy() {
		return sortBy;
	}
	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}
	public String getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	public String getParam1() {
		return param1;
	}
	public void setParam1(String param1) {
		this.param1 = param1;
	}
	public String getParam2() {
		return param2;
	}
	public void setParam2(String param2) {
		this.param2 = param2;
	}
	public String getParam3() {
		return param3;
	}
	public void setParam3(String param3) {
		this.param3 = param3;
	}
	public String getParam4() {
		return param4;
	}
	public void setParam4(String param4) {
		this.param4 = param4;
	}
	public String getParam5() {
		return param5;
	}
	public void setParam5(String param5) {
		this.param5 = param5;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("settingNo") //
				.isRequired()
				.maxLength(20)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("settingName") //
				.isRequired()
				.maxLength(100)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("subsettingNo") //
				.isRequired()
				.maxLength(20)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale) //
				.field("subsettingName") //
				.isRequired()
				.maxLength(100)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale) //
				.field("sortBy") //
				.isRequired()
				.build(),
		});
	}
	
}