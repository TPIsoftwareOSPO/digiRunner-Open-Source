package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0231Req  extends ReqValidator{
	
	/**用戶端帳號*/
	private String clientID;
	
	/**重置密碼*/
	private String resetBlock;
	
	/**密碼(前端已經用Base64編碼過)*/
	private String clientBlock;
	
	/**新密碼(前端已經用Base64編碼過)*/
	private String newClientBlock;
	
	/**再次確認密碼(前端已經用Base64編碼過)*/
	private String confirmNewClientBlock;
	
	
	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	
	public String getResetBlock() {
		return resetBlock;
	}

	public void setResetBlock(String resetBlock) {
		this.resetBlock = resetBlock;
	}

	public String getClientBlock() {
		return clientBlock;
	}

	public void setClientBlock(String clientBlock) {
		this.clientBlock = clientBlock;
	}

	public String getNewClientBlock() {
		return newClientBlock;
	}

	public void setNewClientBlock(String newClientBlock) {
		this.newClientBlock = newClientBlock;
	}

	public String getConfirmNewClientBlock() {
		return confirmNewClientBlock;
	}

	public void setConfirmNewClientBlock(String confirmNewClientBlock) {
		this.confirmNewClientBlock = confirmNewClientBlock;
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
				.buildString(locale)
				.field("resetBlock")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("clientBlock")
				.maxLength(256)
				.build()
				,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("newClientBlock")
				.maxLength(256)
				.build()
				,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("confirmNewClientBlock")
				.maxLength(256)
				.build()
		});
		
		
	}
	
}
