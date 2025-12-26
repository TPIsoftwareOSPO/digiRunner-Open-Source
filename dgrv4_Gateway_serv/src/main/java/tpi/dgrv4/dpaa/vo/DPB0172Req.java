package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class DPB0172Req extends ReqValidator {
	private String id;

	private String clientId;

	private String idpType;

	private String status;

	private String remark;

	private String idpClientId;

	private String idpClientMima;

	private String idpClientName;

	private String wellKnownUrl;

	private String callbackUrl;

	private String authUrl;

	private String accessTokenUrl;

	private String scope;

	@Override
	public String toString() {
		return "DPB0172Req [id=" + id + ", clientId=" + clientId + ", idpType=" + idpType + ", status=" + status
				+ ", remark=" + remark + ", idpClientId=" + idpClientId + ", idpClientMima=" + idpClientMima
				+ ", idpClientName=" + idpClientName + ", wellKnownUrl=" + wellKnownUrl + ", callbackUrl=" + callbackUrl
				+ ", authUrl=" + authUrl + ", accessTokenUrl=" + accessTokenUrl + ", scope=" + scope + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIdpClientId() {
		return idpClientId;
	}

	public void setIdpClientId(String idpClientId) {
		this.idpClientId = idpClientId;
	}

	public String getIdpClientMima() {
		return idpClientMima;
	}

	public void setIdpClientMima(String idpClientMima) {
		this.idpClientMima = idpClientMima;
	}

	public String getIdpClientName() {
		return idpClientName;
	}

	public void setIdpClientName(String idpClientName) {
		this.idpClientName = idpClientName;
	}

	public String getWellKnownUrl() {
		return wellKnownUrl;
	}

	public void setWellKnownUrl(String wellKnownUrl) {
		this.wellKnownUrl = wellKnownUrl;
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

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
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
					.field("status")
					.isRequired()
					.pattern(RegexpConstant.Y_OR_N,TsmpDpAaRtnCode._2007.getCode(),null)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("idpClientId")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("idpClientMima")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("idpClientName")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("wellKnownUrl")
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
