package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0006Req extends ReqValidator{
	
	/** 使用者編號*/
	private String userId;
	
	/** 使用者帳號 */
	private String userName;

	/** 使用者帳號 */
	private String newUserName;

	/** 原使用者名稱 */
	private String userAlias;

	/** 使用者名稱 */
	private String newUserAlias;

	/** 原密碼(前端用Base64編碼) */
	private String userBlock;

	/** 密碼(前端用Base64編碼) */
	private String newUserBlock;
	
	/** 原使用者E-mail */
	private String userMail;
	
	/** 使用者E-mail */
	private String newUserMail;

	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getUserBlock() {
		return userBlock;
	}

	public void setUserBlock(String userBlock) {
		this.userBlock = userBlock;
	}

	public String getNewUserBlock() {
		return newUserBlock;
	}

	public void setNewUserBlock(String newUserBlock) {
		this.newUserBlock = newUserBlock;
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

	@Override
	public String toString() {
		return "AA0006Req [userId=" + userId + ", userName=" + userName + ", newUserName=" + newUserName
				+ ", userAlias=" + userAlias + ", newUserAlias=" + newUserAlias + ", userBlock=" + userBlock
				+ ", newUserBlock=" + newUserBlock + ", userMail=" + userMail + ", newUserMail=" + newUserMail + "]";
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
			});
	}
	
}
