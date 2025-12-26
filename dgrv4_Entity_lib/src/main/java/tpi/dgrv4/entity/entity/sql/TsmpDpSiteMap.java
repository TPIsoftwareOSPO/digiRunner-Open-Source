package tpi.dgrv4.entity.entity.sql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_dp_site_map")
public class TsmpDpSiteMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "site_id")
	private Long siteId;

	@Column(name = "site_parent_id")
	private Long siteParentId;

	@Column(name = "site_desc")
	private String siteDesc;

	@Column(name = "data_sort")
	private Integer dataSort;

	@Column(name = "site_url")
	private String siteUrl;

	@Column(name = "create_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;
	
	@Column(name = "update_time")
	private Date updateDateTime;

	/* constructors */

	public TsmpDpSiteMap() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpSiteMap [siteId=" + siteId + ", siteParentId=" + siteParentId + ", siteDesc=" + siteDesc
				+ ", dataSort=" + dataSort + ", siteUrl=" + siteUrl + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + "]";
	}

	/* getters and setters */

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Long getSiteParentId() {
		return siteParentId;
	}

	public void setSiteParentId(Long siteParentId) {
		this.siteParentId = siteParentId;
	}

	public String getSiteDesc() {
		return siteDesc;
	}

	public void setSiteDesc(String siteDesc) {
		this.siteDesc = siteDesc;
	}
	
	public Long getSiteId() {
		return siteId;
	}


	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}
	
	public String getSiteUrl() {
		return siteUrl;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
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

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}
