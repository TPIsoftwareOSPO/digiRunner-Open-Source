package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0206Req extends ReqValidator{

	/** Agent名稱 */
	private String agent;
	
	/** Event Message */
	private String eventMsg;
	
	/** Event Type */
	private String eventType;
	
	/** 是否登入 */
	private String isLogin;
	
	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getEventMsg() {
		return eventMsg;
	}

	public void setEventMsg(String eventMsg) {
		this.eventMsg = eventMsg;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getIsLogin() {
		return isLogin;
	}

	public void setIsLogin(String isLogin) {
		this.isLogin = isLogin;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("agent")
					.isRequired()
					.maxLength(500)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("eventMsg")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("eventType")
					.isRequired()
					.maxLength(4)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("isLogin")
					.isRequired()
					.build()
			});
	}


}
