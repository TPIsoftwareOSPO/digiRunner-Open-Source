package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0804Req  extends ReqValidator {

	/** 主機名稱*/
	private String regHost;
	
	/** 註冊主機序號ID*/
	private String regHostID;
	
	

	public String getRegHost() {
		return regHost;
	}

	public void setRegHost(String regHost) {
		this.regHost = regHost;
	}

	public String getRegHostID() {
		return regHostID;
	}

	public void setRegHostID(String regHostID) {
		this.regHostID = regHostID;
	}
	
	@Override
	public String toString() {
		return "AA0804Req [regHost=" + regHost + ", regHostID=" + regHostID + "]";
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
					.field("regHostID")
					.isRequired()
					.maxLength(10)
					.build()
		});
	}
}
