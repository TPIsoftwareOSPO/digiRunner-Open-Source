package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA1103Req extends ReqValidator{

	/** 安全等級ID */
	private String securityLevelId;
	
	/** 原始資料-安全等級名稱 */
	private String oriSecurityLevelName;
	
	/** 新-安全等級名稱 */
	private String newSecurityLevelName;
	
	/** 新-安全等級描述 */
	private String newSecurityLevelDesc;

	
	public String getSecurityLevelId() {
		return securityLevelId;
	}


	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}


	public String getOriSecurityLevelName() {
		return oriSecurityLevelName;
	}


	public void setOriSecurityLevelName(String oriSecurityLevelName) {
		this.oriSecurityLevelName = oriSecurityLevelName;
	}


	public String getNewSecurityLevelName() {
		return newSecurityLevelName;
	}


	public void setNewSecurityLevelName(String newSecurityLevelName) {
		this.newSecurityLevelName = newSecurityLevelName;
	}


	public String getNewSecurityLevelDesc() {
		return newSecurityLevelDesc;
	}


	public void setNewSecurityLevelDesc(String newSecurityLevelDesc) {
		this.newSecurityLevelDesc = newSecurityLevelDesc;
	}


	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newSecurityLevelName")
					.isRequired()
					.maxLength(30)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newSecurityLevelDesc")
					.maxLength(60)
					.build()
			});
	}

}
