package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0214Req extends ReqValidator{

	/** 群組編號 */
	private String groupID;

	/** 群組代碼 */
	private String groupName;

	/** 群組名稱 */
	private String groupAlias;
	
	/** 允許使用時間 */
	private Integer allowDays;
	
	/** 允許使用時間(單位) */
	private String allowDaysUnit;
	
	/** 授權次數上限 */
	private Integer allowTimes;
	
	/** 群組描述 */
	private String groupDesc;
	
	/** 安全等級 */
	private String securityLevel;
	
	/** 授權核身種類 */
	private List<String> groupAuthoritiesId;
	
	/** 原始apiList */
	private List<AA0214Api> oriApiList;
	
	/** 修改後apiList */
	private List<AA0214Api> newApiList;
	
	/** 允許的存取方法 */
	private List<String> groupAccess;

	
	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupAlias() {
		return groupAlias;
	}

	public void setGroupAlias(String groupAlias) {
		this.groupAlias = groupAlias;
	}

	public Integer getAllowDays() {
		return allowDays;
	}

	public void setAllowDays(Integer allowDays) {
		this.allowDays = allowDays;
	}

	public String getAllowDaysUnit() {
		return allowDaysUnit;
	}

	public void setAllowDaysUnit(String allowDaysUnit) {
		this.allowDaysUnit = allowDaysUnit;
	}

	public Integer getAllowTimes() {
		return allowTimes;
	}

	public void setAllowTimes(Integer allowTimes) {
		this.allowTimes = allowTimes;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}

	public String getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}

	public List<String> getGroupAuthoritiesId() {
		return groupAuthoritiesId;
	}

	public void setGroupAuthoritiesId(List<String> groupAuthoritiesId) {
		this.groupAuthoritiesId = groupAuthoritiesId;
	}

	public List<AA0214Api> getOriApiList() {
		return oriApiList;
	}

	public void setOriApiList(List<AA0214Api> oriApiList) {
		this.oriApiList = oriApiList;
	}

	public List<AA0214Api> getNewApiList() {
		return newApiList;
	}

	public void setNewApiList(List<AA0214Api> newApiList) {
		this.newApiList = newApiList;
	}

	public List<String> getGroupAccess() {
		return groupAccess;
	}

	public void setGroupAccess(List<String> groupAccess) {
		this.groupAccess = groupAccess;
	}

	@Override
	public String toString() {
		return "AA0214Req [groupID=" + groupID + ", groupName=" + groupName + ", groupAlias=" + groupAlias
				+ ", allowDays=" + allowDays + ", allowDaysUnit=" + allowDaysUnit + ", allowTimes=" + allowTimes
				+ ", groupDesc=" + groupDesc + ", securityLevel=" + securityLevel + ", groupAuthoritiesId="
				+ groupAuthoritiesId + ", oriApiList=" + oriApiList + ", newApiList=" + newApiList + ", groupAccess="
				+ groupAccess + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupID")
					.isRequired()
					.maxLength(10)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupName")
					.isRequired()
					.maxLength(50)
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAlias")
					.maxLength(50)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("allowDays")
					.isRequired()
					.min(0)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("allowDaysUnit")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("allowTimes")
					.isRequired()
					.min(0)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupDesc")
					.maxLength(500)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevel")
					.isRequired()
					.maxLength(10)
					.build(),
			});
	}


}
