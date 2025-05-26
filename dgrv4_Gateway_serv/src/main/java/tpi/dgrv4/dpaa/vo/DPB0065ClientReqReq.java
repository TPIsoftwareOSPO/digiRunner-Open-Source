package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0065ClientReqReq extends ReqValidator{
	

	/** 用戶ID */
	private String clientId;

	/** 用戶名稱 */
	private String clientName;
	
	private String encPublicFlag;
	
	private String reqDesc;
	

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public String getEncPublicFlag() {
		return encPublicFlag;
	}

	public void setEncPublicFlag(String encPublicFlag) {
		this.encPublicFlag = encPublicFlag;
	}

	public String getReqDesc() {
		return reqDesc;
	}
	
	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("clientId")
				.isRequired()
				.maxLength(40)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("clientName")
				.isRequired()
				.maxLength(150)
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("encPublicFlag")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("reqDesc")
				.isRequired()
				.maxLength(500)
				.build()
				
			});
	}
	
}
