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
@Table(name = "dgr_website_detail")
public class DgrWebsiteDetail implements Serializable, DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "dgr_website_detail_id")
	private Long dgrWebsiteDetailId;

	@Column(name = "dgr_website_id")
	private Long dgrWebsiteId;

	@Column(name = "probability")
	private Integer probability;

	@Column(name = "url")
	private String url;

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

	public Long getDgrWebsiteDetailId() {
		return dgrWebsiteDetailId;
	}

	public void setDgrWebsiteDetailId(Long dgrWebsiteDetailId) {
		this.dgrWebsiteDetailId = dgrWebsiteDetailId;
	}

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	public Integer getProbability() {
		return probability;
	}

	public void setProbability(Integer probability) {
		this.probability = probability;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	@Override
	public String toString() {
		return "DgrWebsiteDetail [dgrWebsiteDetailId=" + dgrWebsiteDetailId + ", dgrWebsiteId=" + dgrWebsiteId
				+ ", probability=" + probability + ", url=" + url + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + ", keywordSearch=" + keywordSearch + "]";
	}

	@Override
	public Long getPrimaryKey() {
		return this.dgrWebsiteDetailId;
	}
}
