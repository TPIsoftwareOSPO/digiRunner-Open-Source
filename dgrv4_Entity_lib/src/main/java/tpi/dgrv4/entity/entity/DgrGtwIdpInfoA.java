package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Entity
@Table(name = "dgr_gtw_idp_info_a")
public class DgrGtwIdpInfoA implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "gtw_idp_info_a_id")
	private Long gtwIdpInfoAId;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "status")
	private String status;

	@Column(name = "remark")
	private String remark;

	@Column(name = "api_url")
	private String apiUrl;

	@Column(name = "api_method")
	private String apiMethod;

	@Column(name = "req_header")
	private String reqHeader;

	@Column(name = "req_body_type")
	private String reqBodyType;

	@Column(name = "req_body")
	private String reqBody;

	@Column(name = "suc_by_type")
	private String sucByType;

	@Column(name = "suc_by_field")
	private String sucByField;

	@Column(name = "suc_by_value")
	private String sucByValue;

	@Column(name = "idt_name")
	private String idtName;

	@Column(name = "idt_email")
	private String idtEmail;

	@Column(name = "idt_picture")
	private String idtPicture;
	
	@Column(name = "icon_file")
	private String iconFile;

	@Column(name = "page_title")
	private String pageTitle;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "idt_light_id")
	private String idtLightId;

	@Column(name = "idt_role_name")
	private String idtRoleName;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@Override
	public Long getPrimaryKey() {
		return gtwIdpInfoAId;
	}

	@Override
	public String toString() {
		return "DgrGtwIdpInfoA [gtwIdpInfoAId=" + gtwIdpInfoAId + ", clientId=" + clientId + ", status=" + status
				+ ", remark=" + remark + ", apiUrl=" + apiUrl + ", apiMethod=" + apiMethod + ", reqHeader=" + reqHeader
				+ ", reqBodyType=" + reqBodyType + ", reqBody=" + reqBody + ", sucByType=" + sucByType + ", sucByField="
				+ sucByField + ", sucByValue=" + sucByValue + ", idtName=" + idtName + ", idtEmail=" + idtEmail
				+ ", idtPicture=" + idtPicture + ", iconFile=" + iconFile + ", pageTitle=" + pageTitle
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + ", idtLightId=" + idtLightId
				+ ", idtRoleName" + idtRoleName + "]";
	}

	public Long getGtwIdpInfoAId() {
		return gtwIdpInfoAId;
	}

	public void setGtwIdpInfoAId(Long gtwIdpInfoAId) {
		this.gtwIdpInfoAId = gtwIdpInfoAId;
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

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getApiMethod() {
		return apiMethod;
	}

	public void setApiMethod(String apiMethod) {
		this.apiMethod = apiMethod;
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

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
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
}
