package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0803Req extends ReqValidator{

	/** 主機名稱*/
	private String regHost;
	
	/** 啟用心跳*/
	private String enabled;
	
	/** 用戶端帳號*/
	private String clientID;
	
	/** 備註*/
	private String memo;

	/** 註冊主機序號ID*/
	private String regHostID;
	
	/** 主機狀態*/
	private String regHostStatus;
	
	public String getRegHost() {
		return regHost;
	}

	public void setRegHost(String regHost) {
		this.regHost = regHost;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getRegHostID() {
		return regHostID;
	}

	public void setRegHostID(String regHostID) {
		this.regHostID = regHostID;
	}

	public String getRegHostStatus() {
		return regHostStatus;
	}

	public void setRegHostStatus(String regHostStatus) {
		this.regHostStatus = regHostStatus;
	}

	@Override
	public String toString() {
		return "AA0803Req [regHost=" + regHost + ", enabled=" + enabled + ", clientID=" + clientID + ", memo=" + memo
				+ ", regHostID=" + regHostID + ", regHostStatus=" + regHostStatus + "]";
	}

	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("regHost")
					.isRequired()
					.maxLength(30)
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientID")
					.isRequired()
					.maxLength(40)
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("memo")
					.maxLength(150)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("regHostID")
					.isRequired()
					.maxLength(10)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("enabled")
					.isRequired()
					.build()
		});
	}
}
