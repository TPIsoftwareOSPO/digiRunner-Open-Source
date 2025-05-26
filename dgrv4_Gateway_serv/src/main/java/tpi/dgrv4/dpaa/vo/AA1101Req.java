package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA1101Req extends ReqValidator{

	/** 安全等級ID */
	private String securityLevelId;
	
	/** 安全等級名稱 */
	private String securityLevelName;
	
	/** 安全等級描述 */
	private String securityLevelDesc;

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}
	
	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public String getSecurityLevelDesc() {
		return securityLevelDesc;
	}

	public void setSecurityLevelDesc(String securityLevelDesc) {
		this.securityLevelDesc = securityLevelDesc;
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevelId")
					.isRequired()
					.maxLength(10)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevelName")
					.isRequired()
					.maxLength(30)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("securityLevelDesc")
					.maxLength(60)
					.build()
			});
	}

}
