package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0224Req extends ReqValidator{

	/** 虛擬群組ID*/
	private String vgroupId;
	
	/** 原始資料-虛擬群組代碼*/
	private String oriVgroupName;
	
	/** 新-虛擬群組代碼*/
	private String newVgroupName;
	
	/** 原始資料-虛擬群組名稱*/
	private String oriVgroupAlias;
	
	/** 新-虛擬群組名稱*/
	private String newVgroupAlias;
	
	/** 原始資料-允許使用時間*/
	private Integer oriAllowDays;
	
	/** 新-允許使用時間*/
	private Integer newAllowDays;
	
	/** 原始資料-允許使用時間(單位)*/
	private String oriTimeUnit;
	
	/** 新-允許使用時間(單位)*/
	private String newTimeUnit;
	
	/** 原始資料-授權次數上限*/
	private Integer oriAllowTimes;
	
	/** 新-授權次數上限*/
	private Integer newAllowTimes;
	
	/** 粗略時間*/
//	private String approximateTimeUnit;
	
	/** 原始資料-授權核身種類ID*/
	private List<String> oriVgroupAuthoritieIds;
	
	/** 新-授權核身種類ID*/
	private List<String> newVgroupAuthoritieIds;
	
	/** 原始資料-安全等級ID*/
	private String oriSecurityLevelId;
	
	/** 新-安全等級ID*/
	private String newSecurityLevelId;
	
	/** 原始資料-虛擬群組描述*/
	private String oriVgroupDesc;
	
	/** 新-虛擬群組描述*/
	private String newVgroupDesc;
	
	/** 原始資料-授權核身種類ID*/
	private List<AA0224Item> oriApiList;
	
	/** 新-授權核身種類ID*/
	private List<AA0224Item> newApiList;

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getOriVgroupName() {
		return oriVgroupName;
	}

	public void setOriVgroupName(String oriVgroupName) {
		this.oriVgroupName = oriVgroupName;
	}

	public String getNewVgroupName() {
		return newVgroupName;
	}

	public void setNewVgroupName(String newVgroupName) {
		this.newVgroupName = newVgroupName;
	}

	public String getOriVgroupAlias() {
		return oriVgroupAlias;
	}

	public void setOriVgroupAlias(String oriVgroupAlias) {
		this.oriVgroupAlias = oriVgroupAlias;
	}

	public String getNewVgroupAlias() {
		return newVgroupAlias;
	}

	public void setNewVgroupAlias(String newVgroupAlias) {
		this.newVgroupAlias = newVgroupAlias;
	}

	public Integer getOriAllowDays() {
		return oriAllowDays;
	}

	public void setOriAllowDays(Integer oriAllowDays) {
		this.oriAllowDays = oriAllowDays;
	}

	public Integer getNewAllowDays() {
		return newAllowDays;
	}

	public void setNewAllowDays(Integer newAllowDays) {
		this.newAllowDays = newAllowDays;
	}

	public String getOriTimeUnit() {
		return oriTimeUnit;
	}

	public void setOriTimeUnit(String oriTimeUnit) {
		this.oriTimeUnit = oriTimeUnit;
	}

	public String getNewTimeUnit() {
		return newTimeUnit;
	}

	public void setNewTimeUnit(String newTimeUnit) {
		this.newTimeUnit = newTimeUnit;
	}

	public Integer getOriAllowTimes() {
		return oriAllowTimes;
	}

	public void setOriAllowTimes(Integer oriAllowTimes) {
		this.oriAllowTimes = oriAllowTimes;
	}

	public Integer getNewAllowTimes() {
		return newAllowTimes;
	}

	public void setNewAllowTimes(Integer newAllowTimes) {
		this.newAllowTimes = newAllowTimes;
	}

	public List<String> getOriVgroupAuthoritieIds() {
		return oriVgroupAuthoritieIds;
	}

	public void setOriVgroupAuthoritieIds(List<String> oriVgroupAuthoritieIds) {
		this.oriVgroupAuthoritieIds = oriVgroupAuthoritieIds;
	}

	public List<String> getNewVgroupAuthoritieIds() {
		return newVgroupAuthoritieIds;
	}

	public void setNewVgroupAuthoritieIds(List<String> newVgroupAuthoritieIds) {
		this.newVgroupAuthoritieIds = newVgroupAuthoritieIds;
	}

	public String getOriSecurityLevelId() {
		return oriSecurityLevelId;
	}

	public void setOriSecurityLevelId(String oriSecurityLevelId) {
		this.oriSecurityLevelId = oriSecurityLevelId;
	}

	public String getNewSecurityLevelId() {
		return newSecurityLevelId;
	}

	public void setNewSecurityLevelId(String newSecurityLevelId) {
		this.newSecurityLevelId = newSecurityLevelId;
	}

	public String getOriVgroupDesc() {
		return oriVgroupDesc;
	}

	public void setOriVgroupDesc(String oriVgroupDesc) {
		this.oriVgroupDesc = oriVgroupDesc;
	}

	public String getNewVgroupDesc() {
		return newVgroupDesc;
	}

	public void setNewVgroupDesc(String newVgroupDesc) {
		this.newVgroupDesc = newVgroupDesc;
	}

	public List<AA0224Item> getOriApiList() {
		return oriApiList;
	}

	public void setOriApiList(List<AA0224Item> oriApiList) {
		this.oriApiList = oriApiList;
	}

	public List<AA0224Item> getNewApiList() {
		return newApiList;
	}

	public void setNewApiList(List<AA0224Item> newApiList) {
		this.newApiList = newApiList;
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("vgroupId")
					.isRequired()
					.maxLength(10)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("oriVgroupName")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newVgroupName")
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.isRequired()
					.maxLength(50)
					.build()
					,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newVgroupAlias")
					.maxLength(85)
					.build()
					,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("newAllowDays")
					.isRequired()
					.min(0)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("newAllowTimes")
					.isRequired()
					.min(0)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newSecurityLevelId")
					.isRequired()
					.maxLength(10)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newVgroupDesc")
					.maxLength(500)
					.build()
			});
	}

	@Override
	public String toString() {
		return "AA0224Req [vgroupId=" + vgroupId + ", oriVgroupName=" + oriVgroupName + ", newVgroupName="
				+ newVgroupName + ", oriVgroupAlias=" + oriVgroupAlias + ", newVgroupAlias=" + newVgroupAlias
				+ ", oriAllowDays=" + oriAllowDays + ", newAllowDays=" + newAllowDays + ", oriTimeUnit=" + oriTimeUnit
				+ ", newTimeUnit=" + newTimeUnit + ", oriAllowTimes=" + oriAllowTimes + ", newAllowTimes="
				+ newAllowTimes + ", oriVgroupAuthoritieIds=" + oriVgroupAuthoritieIds + ", newVgroupAuthoritieIds="
				+ newVgroupAuthoritieIds + ", oriSecurityLevelId=" + oriSecurityLevelId + ", newSecurityLevelId="
				+ newSecurityLevelId + ", oriVgroupDesc=" + oriVgroupDesc + ", newVgroupDesc=" + newVgroupDesc
				+ ", oriApiList=" + oriApiList + ", newApiList=" + newApiList + "]";
	}

}
