package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0187Req extends ReqValidator {
	private String id;
	private String clientId;
	private String status;
	private String remark;
	private String apiMethod;
	private String apiUrl;
	private String reqHeader;
	private String reqBodyType;
	private String reqBody;
	private String sucByType;
	private String sucByField;
	private String sucByValue;
	private String idtName;
	private String idtEmail;
	private String idtPicture;
	private String iconFile;
	private String pageTitle;
	private String idtLightId;
	private String idtRoleName;
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

	public String getApiMethod() {
		return apiMethod;
	}

	public void setApiMethod(String apiMethod) {
		this.apiMethod = apiMethod;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getReqHeader() {
		return reqHeader;
	}

	public void setReqHeader(String reqHeader) {
		this.reqHeader = reqHeader;
	}

	public String getReqBodyType() {
		return reqBodyType;
	}

	public void setReqBodyType(String reqBodyType) {
		this.reqBodyType = reqBodyType;
	}

	public String getReqBody() {
		return reqBody;
	}

	public void setReqBody(String reqBody) {
		this.reqBody = reqBody;
	}

	public String getSucByType() {
		return sucByType;
	}

	public void setSucByType(String sucByType) {
		this.sucByType = sucByType;
	}

	public String getSucByField() {
		return sucByField;
	}

	public void setSucByField(String sucByField) {
		this.sucByField = sucByField;
	}

	public String getSucByValue() {
		return sucByValue;
	}

	public void setSucByValue(String sucByValue) {
		this.sucByValue = sucByValue;
	}

	public String getIdtName() {
		return idtName;
	}

	public void setIdtName(String idtName) {
		this.idtName = idtName;
	}

	public String getIdtEmail() {
		return idtEmail;
	}

	public void setIdtEmail(String idtEmail) {
		this.idtEmail = idtEmail;
	}

	public String getIdtPicture() {
		return idtPicture;
	}

	public void setIdtPicture(String idtPicture) {
		this.idtPicture = idtPicture;
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

	public String getIdtLightId() {
		return idtLightId;
	}

	public void setIdtLightId(String idtLightId) {
		this.idtLightId = idtLightId;
	}

	public String getIdtRoleName() {
		return idtRoleName;
	}

	public void setIdtRoleName(String idtRoleName) {
		this.idtRoleName = idtRoleName;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("status")
				.isRequired()
				.pattern(RegexpConstant.Y_OR_N,TsmpDpAaRtnCode._2007.getCode(),null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiMethod")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("apiUrl")
				.isRequired()
				.pattern("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",TsmpDpAaRtnCode._2007.getCode(),null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("reqBodyType")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("sucByType")
				.isRequired()
				.pattern("^[H|R]$",TsmpDpAaRtnCode._2007.getCode(),null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("pageTitle")
				.build()
	});
	}
}
