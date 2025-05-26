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
@Table(name = "dgr_ac_idp_info_cus")
public class DgrAcIdpInfoCus implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "ac_idp_info_cus_id")
	private Long acIdpInfoCusId;

	@Column(name = "ac_idp_info_cus_name")
	private String acIdpInfoCusName;

	@Column(name = "cus_status")
	private String cusStatus;

	@Column(name = "cus_login_url")
	private String cusLoginUrl;

	@Column(name = "cus_backend_login_url")
	private String cusBackendLoginUrl;

	@Column(name = "cus_user_data_url")
	private String cusUserDataUrl;

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
	private Long version = 1L;

	@Override
	public Long getPrimaryKey() {
		return acIdpInfoCusId;
	}

	@Override
	public String toString() {
		return "DgrAcIdpInfoCus [acIdpInfoCusId=" + acIdpInfoCusId + ", acIdpInfoCusName=" + acIdpInfoCusName
				+ ", cusStatus=" + cusStatus + ", cusLoginUrl=" + cusLoginUrl + ", cusBackendLoginUrl="
				+ cusBackendLoginUrl + ", cusUserDataUrl=" + cusUserDataUrl + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]";
	}

	// Getters and Setters

	public Long getAcIdpInfoCusId() {
		return acIdpInfoCusId;
	}

	public void setAcIdpInfoCusId(Long acIdpInfoCusId) {
		this.acIdpInfoCusId = acIdpInfoCusId;
	}

	public String getAcIdpInfoCusName() {
		return acIdpInfoCusName;
	}

	public void setAcIdpInfoCusName(String acIdpInfoCusName) {
		this.acIdpInfoCusName = acIdpInfoCusName;
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
}
