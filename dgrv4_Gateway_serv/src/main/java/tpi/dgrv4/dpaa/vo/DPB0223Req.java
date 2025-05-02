package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0223Req extends ReqValidator {

	private String cusId;

	private String cusName;

	private String cusStatus;

	private String cusLoginUrl;

	private String cusBackendLoginUrl;

	private String cusUserDataUrl;

	public String getCusId() {
		return cusId;
	}

	public void setCusId(String cusId) {
		this.cusId = cusId;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getCusStatus() {
		return cusStatus;
	}

	public void setCusStatus(String cusStatus) {
		this.cusStatus = cusStatus;
	}

	public String getCusLoginUrl() {
		return cusLoginUrl;
	}

	public void setCusLoginUrl(String cusLoginUrl) {
		this.cusLoginUrl = cusLoginUrl;
	}

	public String getCusBackendLoginUrl() {
		return cusBackendLoginUrl;
	}

	public void setCusBackendLoginUrl(String cusBackendLoginUrl) {
		this.cusBackendLoginUrl = cusBackendLoginUrl;
	}

	public String getCusUserDataUrl() {
		return cusUserDataUrl;
	}

	public void setCusUserDataUrl(String cusUserDataUrl) {
		this.cusUserDataUrl = cusUserDataUrl;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] { //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusName") //
						.maxLength(100) //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusLoginUrl") //
						.maxLength(2000) //
						.isRequired() //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusBackendLoginUrl") //
						.maxLength(2000) //
						.isRequired() //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusUserDataUrl") //
						.maxLength(2000) //
						.isRequired() //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusStatus") //
						.isRequired() //
						.pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2007.getCode(), null) //
						.build() //
		});

	}

}
