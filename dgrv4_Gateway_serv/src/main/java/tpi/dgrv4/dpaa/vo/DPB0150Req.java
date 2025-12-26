package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class DPB0150Req extends ReqValidator{
	private String idpType;
	
	private String clientId;
	
	private String clientMima;
	
	private String clientName;
	
	private String clientStatus;
	
	private String idpWellKnownUrl;
	
	private String callbackUrl;
	
	private String authUrl;
	
	private String accessTokenUrl;
	
	private String scope;

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientMima() {
		return clientMima;
	}

	public void setClientMima(String clientMima) {
		this.clientMima = clientMima;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientStatus() {
		return clientStatus;
	}

	public void setClientStatus(String clientStatus) {
		this.clientStatus = clientStatus;
	}

	public String getIdpWellKnownUrl() {
		return idpWellKnownUrl;
	}

	public void setIdpWellKnownUrl(String idpWellKnownUrl) {
		this.idpWellKnownUrl = idpWellKnownUrl;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}


	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		String URIRegex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("idpType")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientId")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientMima")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientName")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientStatus")
					.isRequired()
					.pattern(RegexpConstant.Y_OR_N,TsmpDpAaRtnCode._2007.getCode(),null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("idpWellKnownUrl")
					.isRequired()
					.pattern(URIRegex, TsmpDpAaRtnCode._1405.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("callbackUrl")
					.isRequired()
					.pattern(URIRegex, TsmpDpAaRtnCode._1405.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("authUrl")
					.pattern(URIRegex, TsmpDpAaRtnCode._1405.getCode(), null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("accessTokenUrl")
					.pattern(URIRegex, TsmpDpAaRtnCode._1405.getCode(), null)
					.build(),
		
			});
	}
}
