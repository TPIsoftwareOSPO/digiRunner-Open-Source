package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0004Req extends ReqValidator{

	/** 使用者編號 */
	private String userID;

	/** 重置密碼錯誤次數 */
	private boolean resetPwdFailTimes;
	
	/** 重置密碼 */
	private boolean resetBlock;
	
	/** 原使用者帳號 */
	private String userName;
	
	/** 使用者帳號 */
	private String newUserName;
	
	/** 原使用者E-mail */
	private String userMail;
	
	/** 使用者E-mail */
	private String newUserMail;
	
	/** 原使用者名稱 */
	private String userAlias;
	
	/** 使用者名稱 */
	private String newUserAlias;
	
	/** 原狀態 
	 * 使用BcryptParam, 
     * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
     * DB值 (PARAM1) = 中文說明; 
     * 1=啟用, 2=停用
	 */
	private String status;
	
	/** 狀態 
	 * 使用BcryptParam, 
     * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
     * DB值 (PARAM1) = 中文說明; 
     * 1=啟用, 2=停用
	 */
	private String newStatus;
	
	/** 原角色清單 */
	private List<String> roleIDList;
	
	/** 角色清單 */
	private List<String> newRoleIDList;
	
	/** 原組織單位名稱 */
	private String orgID;
	
	/** 組織單位名稱 */
	private String newOrgID;
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public boolean isResetPwdFailTimes() {
		return resetPwdFailTimes;
	}

	public void setResetPwdFailTimes(boolean resetPwdFailTimes) {
		this.resetPwdFailTimes = resetPwdFailTimes;
	}

	public boolean isResetBlock() {
		return resetBlock;
	}

	public void setResetBlock(boolean resetBlock) {
		this.resetBlock = resetBlock;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNewUserName() {
		return newUserName;
	}

	public void setNewUserName(String newUserName) {
		this.newUserName = newUserName;
	}

	public String getUserMail() {
		return userMail;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public String getNewUserMail() {
		return newUserMail;
	}

	public void setNewUserMail(String newUserMail) {
		this.newUserMail = newUserMail;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getNewUserAlias() {
		return newUserAlias;
	}

	public void setNewUserAlias(String newUserAlias) {
		this.newUserAlias = newUserAlias;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}

	public List<String> getRoleIDList() {
		return roleIDList;
	}

	public void setRoleIDList(List<String> roleIDList) {
		this.roleIDList = roleIDList;
	}

	public List<String> getNewRoleIDList() {
		return newRoleIDList;
	}

	public void setNewRoleIDList(List<String> newRoleIDList) {
		this.newRoleIDList = newRoleIDList;
	}

	public String getOrgID() {
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public String getNewOrgID() {
		return newOrgID;
	}

	public void setNewOrgID(String newOrgID) {
		this.newOrgID = newOrgID;
	}

	@Override
	public String toString() {
		return "AA0004Req [userID=" + userID + ", resetPwdFailTimes=" + resetPwdFailTimes + ", resetBlock=" + resetBlock
				+ ", userName=" + userName + ", newUserName=" + newUserName + ", userMail=" + userMail
				+ ", newUserMail=" + newUserMail + ", userAlias=" + userAlias + ", newUserAlias=" + newUserAlias
				+ ", status=" + status + ", newStatus=" + newStatus + ", roleIDList=" + roleIDList + ", newRoleIDList="
				+ newRoleIDList + ", orgID=" + orgID + ", newOrgID=" + newOrgID + "]";
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newUserName")
					.isRequired()
					.maxLength(50)
					.pattern(RegexpConstant.ENGLISH_NUMBER_AT, TsmpDpAaRtnCode._2023.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newUserAlias")
					.maxLength(30)
					.pattern(RegexpConstant.CHINESE_ENGLISH_NUMBER, TsmpDpAaRtnCode._2022.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newUserMail")
					.isRequired()
					.maxLength(100)
					.pattern(RegexpConstant.EMAIL, TsmpDpAaRtnCode._2007.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("newRoleIDList")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newOrgID")
					.isRequired()
					.build(),
			});
	}

}
