package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
		
public class AA0211Req extends ReqValidator{

	// 允許使用時間
	private Integer allowAccessDays;

	// 授權次數上限
	private Integer allowAccessUseTimes;

	// API 清單
	private List<AA0211APIKey> apiKeyList;

	// 群組名稱
	private String groupAlias;

	// 授權核身種類
	private List<String> groupAuthorities;

	// 群組描述
	private String groupDesc;

	// 群組代碼
	private String groupName;

	// 安全等級
	private String securityLevel;

	// 允許使用時間(單位)
	private String allowAccessUseTimesTimeUnit;
	
	

	public Integer getAllowAccessDays() {
		return allowAccessDays;
	}



	public void setAllowAccessDays(Integer allowAccessDays) {
		this.allowAccessDays = allowAccessDays;
	}



	public Integer getAllowAccessUseTimes() {
		return allowAccessUseTimes;
	}



	public void setAllowAccessUseTimes(Integer allowAccessUseTimes) {
		this.allowAccessUseTimes = allowAccessUseTimes;
	}



	public List<AA0211APIKey> getApiKeyList() {
		return apiKeyList;
	}



	public void setApiKeyList(List<AA0211APIKey> apiKeyList) {
		this.apiKeyList = apiKeyList;
	}



	public String getGroupAlias() {
		return groupAlias;
	}



	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}



	public List<String> getGroupAuthorities() {
		return groupAuthorities;
	}



	public void setGroupAuthorities(List<String> groupAuthorities) {
		this.groupAuthorities = groupAuthorities;
	}



	public String getGroupDesc() {
		return groupDesc;
	}



	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}



	public String getGroupName() {
		return groupName;
	}



	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}



	public String getSecurityLevel() {
		return securityLevel;
	}



	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}



	public String getAllowAccessUseTimesTimeUnit() {
		return allowAccessUseTimesTimeUnit;
	}



	public void setAllowAccessUseTimesTimeUnit(String allowAccessUseTimesTimeUnit) {
		this.allowAccessUseTimesTimeUnit = allowAccessUseTimesTimeUnit;
	}



	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("allowAccessDays")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("allowAccessUseTimes")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("groupAlias")
				.maxLength(50)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("groupDesc")
				.maxLength(500)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("groupName")
				.isRequired()
				.maxLength(50)
				.pattern(RegexpConstant.ENGLISH_NUMBER)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("allowAccessUseTimesTimeUnit")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("securityLevel")
				.isRequired()
				.maxLength(10)
				.build()
		,
		});
	}

}
