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
@Table(name = "dp_api_version")
public class DpApiVersion implements Serializable, DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "dp_api_version_id")
	private Long dpApiVersionId;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "dp_api_version")
	private String dpApiVersion;

	@Column(name = "start_of_life")
	private Long startOfLife;

	@Column(name = "end_of_life")
	private Long endOfLife;

	@Column(name = "remark")
	private String remark;

	@Column(name = "time_zone")
	private String timeZone;

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

	public Long getDpApiVersionId() {
		return dpApiVersionId;
	}

	public void setDpApiVersionId(Long dpApiVersionId) {
		this.dpApiVersionId = dpApiVersionId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getDpApiVersion() {
		return dpApiVersion;
	}

	public void setDpApiVersion(String dpApiVersion) {
		this.dpApiVersion = dpApiVersion;
	}

	public Long getStartOfLife() {
		return startOfLife;
	}

	public void setStartOfLife(Long startOfLife) {
		this.startOfLife = startOfLife;
	}

	public Long getEndOfLife() {
		return endOfLife;
	}

	public void setEndOfLife(Long endOfLife) {
		this.endOfLife = endOfLife;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
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

	@Override
	public String toString() {
		return "DpApiVersion [dpApiVersionId=" + dpApiVersionId + ", moduleName=" + moduleName + ", apiKey=" + apiKey
				+ ", dpApiVersion=" + dpApiVersion + ", startOfLife=" + startOfLife + ", endOfLife=" + endOfLife
				+ ", remark=" + remark + ", timeZone=" + timeZone + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]";
	}

	@Override
	public Long getPrimaryKey() {
		return this.dpApiVersionId;
	}
}
