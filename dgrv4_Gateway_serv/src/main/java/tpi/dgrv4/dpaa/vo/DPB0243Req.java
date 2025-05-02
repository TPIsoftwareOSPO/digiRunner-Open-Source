package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class DPB0243Req extends ReqValidator {

	private String gtwIdpInfoCusId;
	private String clientId;
	private String status;
	private String cusLoginUrl;
	private String cusUserDataUrl;
	private String iconFile;
	private String pageTitle;

	public String getGtwIdpInfoCusId() {
		return gtwIdpInfoCusId;
	}

	public void setGtwIdpInfoCusId(String gtwIdpInfoCusId) {
		this.gtwIdpInfoCusId = gtwIdpInfoCusId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCusLoginUrl() {
		return cusLoginUrl;
	}

	public void setCusLoginUrl(String cusLoginUrl) {
		this.cusLoginUrl = cusLoginUrl;
	}

	public String getCusUserDataUrl() {
		return cusUserDataUrl;
	}

	public void setCusUserDataUrl(String cusUserDataUrl) {
		this.cusUserDataUrl = cusUserDataUrl;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] { //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("cusLoginUrl") //
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
						.field("iconFile") //
						.maxLength(4000) //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("pageTitle") //
						.maxLength(400) //
						.build(), //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("status") //
						.isRequired() //
						.pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2007.getCode(), null) //
						.build() //
		});

	}

}
