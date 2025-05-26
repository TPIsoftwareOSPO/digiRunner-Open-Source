package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0228Req extends ReqValidator{

	/** PK */
	private String groupId;

	/** 模糊搜尋 */
	private String keyword;

	/** 安全等級 */
	private String securityLevelID;

	/** 用戶端帳號 */
	private String clientID;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public String getKeyword() {
		return keyword;
	}

	public String getSecurityLevelID() {
		return securityLevelID;
	}

	public void setSecurityLevelID(String securityLevelID) {
		this.securityLevelID = securityLevelID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	
	public String getClientID() {
		return clientID;
	}

	@Override
	public String toString() {
		return "AA0228Req [groupId=" + groupId + ", keyword=" + keyword + ", securityLevelID=" + securityLevelID
				+ ", clientID=" + clientID + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevelID")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("clientID")
					.isRequired()
					.build()
			});
	}

	
}
