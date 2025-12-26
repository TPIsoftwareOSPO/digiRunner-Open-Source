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
@Table(name = "dgr_gtw_idp_info_jdbc")
public class DgrGtwIdpInfoJdbc implements DgrSequenced {
	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "gtw_idp_info_jdbc_id")
	private Long gtwIdpInfoJdbcId;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "status")
	private String status;

	@Column(name = "remark")
	private String remark;

	@Column(name = "connection_name")
	private String connectionName;

	@Column(name = "sql_ptmt")
	private String sqlPtmt;

	@Column(name = "sql_params")
	private String sqlParams;

	@Column(name = "user_mima_alg")
	private String userMimaAlg;

	@Column(name = "user_mima_col_name")
	private String userMimaColName;

	@Column(name = "idt_sub")
	private String idtSub;

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

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@Override
	public Long getPrimaryKey() {
		return gtwIdpInfoJdbcId;
	}

	@Override
	public String toString() {
		return "DgrGtwIdpInfoJdbc [gtwIdpInfoJdbcId=" + gtwIdpInfoJdbcId + ", clientId=" + clientId + ", status="
				+ status + ", remark=" + remark + ", connectionName=" + connectionName + ", sqlPtmt=" + sqlPtmt
				+ ", sqlParams=" + sqlParams + ", userMimaAlg=" + userMimaAlg + ", userMimaColName=" + userMimaColName
				+ ", idtSub=" + idtSub + ", idtName=" + idtName + ", idtEmail=" + idtEmail + ", idtPicture="
				+ idtPicture + ", iconFile=" + iconFile + ", pageTitle=" + pageTitle + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + "]";
	}

	public Long getGtwIdpInfoJdbcId() {
		return gtwIdpInfoJdbcId;
	}

	public void setGtwIdpInfoJdbcId(Long gtwIdpInfoJdbcId) {
		this.gtwIdpInfoJdbcId = gtwIdpInfoJdbcId;
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

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getSqlPtmt() {
		return sqlPtmt;
	}

	public void setSqlPtmt(String sqlPtmt) {
		this.sqlPtmt = sqlPtmt;
	}

	public String getSqlParams() {
		return sqlParams;
	}

	public void setSqlParams(String sqlParams) {
		this.sqlParams = sqlParams;
	}

	public String getUserMimaAlg() {
		return userMimaAlg;
	}

	public void setUserMimaAlg(String userMimaAlg) {
		this.userMimaAlg = userMimaAlg;
	}

	public String getUserMimaColName() {
		return userMimaColName;
	}

	public void setUserMimaColName(String userMimaColName) {
		this.userMimaColName = userMimaColName;
	}

	public String getIdtSub() {
		return idtSub;
	}

	public void setIdtSub(String idtSub) {
		this.idtSub = idtSub;
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
}
