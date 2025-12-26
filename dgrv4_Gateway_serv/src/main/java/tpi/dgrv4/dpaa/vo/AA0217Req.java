package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0217Req  extends ReqValidator{

	/** 用戶端帳號 */
	private String clientID;

	/** 用戶端代號 */
	private String clientName;

	/** 原安全等級 */
	private String securityID;

	/** 安全等級 */
	private String newSecurityID;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getSecurityID() {
		return securityID;
	}

	public void setSecurityID(String securityID) {
		this.securityID = securityID;
	}

	public String getNewSecurityID() {
		return newSecurityID;
	}

	public void setNewSecurityID(String newSecurityID) {
		this.newSecurityID = newSecurityID;
	}

	@Override
	public String toString() {
		return "AA0217Req [clientID=" + clientID + ", clientName=" + clientName + ", securityID=" + securityID
				+ ", newSecurityID=" + newSecurityID + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientID")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("clientName")
					.isRequired()
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("newSecurityID")
					.isRequired()
					.build()
			});
	}

}
