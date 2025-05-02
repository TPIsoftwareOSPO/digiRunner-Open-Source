package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0220Req  extends ReqValidator{
	
	/**用戶端帳號*/
	private String clientID;
	
	/**允許密碼錯誤上限*/
	private Integer resetFailLoginTreshhold;
	
	/**狀態*/
	private String encodeStatus;
	
	/**重置密碼錯誤次數*/
	private String resetPwdFailTimes;
	
	
	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public Integer getResetFailLoginTreshhold() {
		return resetFailLoginTreshhold;
	}

	public void setResetFailLoginTreshhold(Integer resetFailLoginTreshhold) {
		this.resetFailLoginTreshhold = resetFailLoginTreshhold;
	}

	public String getEncodeStatus() {
		return encodeStatus;
	}

	public void setEncodeStatus(String encodeStatus) {
		this.encodeStatus = encodeStatus;
	}

	public String getResetPwdFailTimes() {
		return resetPwdFailTimes;
	}

	public void setResetPwdFailTimes(String resetPwdFailTimes) {
		this.resetPwdFailTimes = resetPwdFailTimes;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("clientID")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildInt(locale)
				.field("resetFailLoginTreshhold")
				.isRequired()
				.min(1)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("encodeStatus")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("resetPwdFailTimes")
				.isRequired()
				.build()
		});
		
		
	}
	
}
