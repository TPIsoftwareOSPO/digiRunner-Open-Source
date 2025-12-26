package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0001Req extends ReqValidator{

	/** 使用者帳號 */
	private String userName;

	/** 使用者名稱 */
	private String userAlias;

	/** 密碼(前端用Base64編碼) */
	private String userBlock;
	
	/** 使用者E-mail */
	private String userMail;
	
	/** 角色清單 */
	private List<String> roleIDList;
	
	/** 組織名稱 */
	private String orgID;
	
	/**狀態 */
	private String encodeStatus;

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getUserBlock() {
		return userBlock;
	}

	public void setUserBlock(String userBlock) {
		this.userBlock = userBlock;
	}

	public String getUserMail() {
		return userMail;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public List<String> getRoleIDList() {
		return roleIDList;
	}

	public void setRoleIDList(List<String> roleIDList) {
		this.roleIDList = roleIDList;
	}

	public String getOrgID() {
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public String getEncodeStatus() {
		return encodeStatus;
	}

	public void setEncodeStatus(String encodeStatus) {
		this.encodeStatus = encodeStatus;
	}

	@Override
	public String toString() {
		return "AA0001Req [userName=" + userName + ", userAlias=" + userAlias + ", userBlock=" + userBlock
				+ ", userMail=" + userMail + ", roleIDList=" + roleIDList + ", orgID=" + orgID + ", encodeStatus="
				+ encodeStatus + "]";
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("userName")
					.isRequired()
					.maxLength(50)
					.pattern(RegexpConstant.ENGLISH_NUMBER_AT, TsmpDpAaRtnCode._2023.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("userAlias")
					.maxLength(30)
					.isRequired()
					.pattern(RegexpConstant.CHINESE_ENGLISH_NUMBER, TsmpDpAaRtnCode._2022.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("userMail")
					.isRequired()
					.maxLength(100)
					.pattern(RegexpConstant.EMAIL, TsmpDpAaRtnCode._2007.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("roleIDList")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("orgID")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("encodeStatus")
					.isRequired()
					.build(),
			});
	}
}
