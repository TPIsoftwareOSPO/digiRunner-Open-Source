package tpi.dgrv4.entity.entity;

import java.io.Serializable;
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
@Table(name = "dp_app")
public class DpApp implements Serializable, DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "dp_application_id")
	private Long dpApplicationId;

	@Column(name = "application_name")
	private String applicationName;

	@Column(name = "application_desc")
	private String applicationDesc;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "dp_user_name")
	private String dpUserName;

	@Column(name = "id_token_jwtstr")
	private String idTokenJwtstr;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Integer version = 1;

	@Column(name = "keyword_search")
	private String keywordSearch;

	@Column(name = "iss")
	private String iss;

	@Override
	public String toString() {
		return "DpApp [dpApplicationId=" + dpApplicationId + ", applicationName=" + applicationName
				+ ", applicationDesc=" + applicationDesc + ", clientId=" + clientId
				+ ", dpUserName=" + dpUserName + ", idTokenJwtstr=" + idTokenJwtstr + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + ", keywordSearch=" + keywordSearch + ", iss=" + iss + "]";
	}

	public Long getDpApplicationId() {
		return dpApplicationId;
	}

	public void setDpApplicationId(Long dpApplicationId) {
		this.dpApplicationId = dpApplicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationDesc() {
		return applicationDesc;
	}

	public void setApplicationDesc(String applicationDesc) {
		this.applicationDesc = applicationDesc;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getDpUserName() {
		return dpUserName;
	}

	public void setDpUserName(String dpUserName) {
		this.dpUserName = dpUserName;
	}

	public String getIdTokenJwtstr() {
		return idTokenJwtstr;
	}

	public void setIdTokenJwtstr(String idTokenJwtstr) {
		this.idTokenJwtstr = idTokenJwtstr;
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	@Override
	public Long getPrimaryKey() {
		return this.dpApplicationId;
	}
}
