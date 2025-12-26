package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0706Req extends ReqValidator{

	/** PK*/
	private String lastAlertId;
	
	/** 狀態*/
	private String alertEnabled;
	
	/** 角色代號*/
	private String roleName;
	
	/** 關鍵字搜尋*/
	private String keyword;
	
	public String getLastAlertId() {
		return lastAlertId;
	}

	public void setLastAlertId(String lastAlertId) {
		this.lastAlertId = lastAlertId;
	}

	public String getAlertEnabled() {
		return alertEnabled;
	}

	public void setAlertEnabled(String alertEnabled) {
		this.alertEnabled = alertEnabled;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertEnabled")
					.isRequired()
					.build()
				
			});
	}


}
