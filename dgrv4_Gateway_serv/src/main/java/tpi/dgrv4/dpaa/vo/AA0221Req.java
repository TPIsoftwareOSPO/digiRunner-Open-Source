package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0221Req extends ReqValidator{
	
	/** 虛擬群組代碼*/
	private String vgroupName;

	/** 虛擬群組名稱*/
	private String vgroupAlias;

	/** 允許使用時間*/
	private Integer allowDays;
	
	/** 允許使用時間(單位)*/
	private String timeUnit;

	/** 授權次數上限*/
	private Integer allowTimes;
	
	/** 授權核身種類ID*/
	private List<String> vgroupAuthoritieIds;
	
	/** 安全等級ID*/
	private String securityLevelId;
	
	/** 虛擬群組描述*/
	private String vgroupDesc;
	
	/** API Key 清單資料*/
	private List<AA0221Item> dataList;

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}
	
	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public Integer getAllowDays() {
		return allowDays;
	}

	public void setAllowDays(Integer allowDays) {
		this.allowDays = allowDays;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	public String getTimeUnit() {
		return timeUnit;
	}

	public Integer getAllowTimes() {
		return allowTimes;
	}

	public void setAllowTimes(Integer allowTimes) {
		this.allowTimes = allowTimes;
	}

	public List<String> getVgroupAuthoritieIds() {
		return vgroupAuthoritieIds;
	}

	public void setVgroupAuthoritieIds(List<String> vgroupAuthoritieIds) {
		this.vgroupAuthoritieIds = vgroupAuthoritieIds;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
	}

	public List<AA0221Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<AA0221Item> dataList) {
		this.dataList = dataList;
	}

	@Override
	public String toString() {
		return "AA0221Req [vgroupName=" + vgroupName + ", vgroupAlias=" + vgroupAlias + ", allowDays=" + allowDays
				+ ", timeUnit=" + timeUnit + ", allowTimes=" + allowTimes + ", vgroupAuthoritieIds="
				+ vgroupAuthoritieIds + ", securityLevelId=" + securityLevelId + ", vgroupDesc=" + vgroupDesc
				+ ", dataList=" + dataList + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("vgroupName")
					.isRequired()
					.maxLength(50)
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("vgroupAlias")
					.maxLength(85)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("allowDays")
					.isRequired()
					.min(0)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("allowTimes")
					.isRequired()
					.min(0)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevelId")
					.isRequired()
					.maxLength(10)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("vgroupDesc")
					.maxLength(500)
					.build()
			});
	}
	
}
