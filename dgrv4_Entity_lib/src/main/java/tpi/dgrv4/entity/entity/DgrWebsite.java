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
@Table(name = "dgr_website")
public class DgrWebsite implements Serializable, DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "dgr_website_id")
	private Long dgrWebsiteId;

	@Column(name = "website_name")
	private String websiteName;

	@Column(name = "website_status")
	private String websiteStatus = "Y";

	@Column(name = "remark")
	private String remark;
	
	@Column(name = "auth")
	private String auth = "N";
	
	@Column(name = "sql_injection")
	private String sqlInjection = "N";
	
	@Column(name = "traffic")
	private String traffic = "N";
	
	@Column(name = "xss")
	private String xss = "N";
	
	@Column(name = "xxe")
	private String xxe = "N";
	
	@Column(name = "tps")
	private Integer tps = 0;
	
	@Column(name = "ignore_api")
	private String ignoreApi;
	
	@Column(name = "show_log")
	private String showLog = "N";

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

	@Override
	public String toString() {
		return "DgrWebsite [dgrWebsiteId=" + dgrWebsiteId + ", websiteName=" + websiteName + ", websiteStatus="
				+ websiteStatus + ", remark=" + remark + ", auth=" + auth + ", sqlInjection=" + sqlInjection
				+ ", traffic=" + traffic + ", xss=" + xss + ", xxe=" + xxe + ", tps=" + tps + ", ignoreApi=" + ignoreApi
				+ ", showLog=" + showLog + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version
				+ ", keywordSearch=" + keywordSearch + "]";
	}

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public String getWebsiteStatus() {
		return websiteStatus;
	}

	public void setWebsiteStatus(String websiteStatus) {
		this.websiteStatus = websiteStatus;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getSqlInjection() {
		return sqlInjection;
	}

	public void setSqlInjection(String sqlInjection) {
		this.sqlInjection = sqlInjection;
	}

	public String getTraffic() {
		return traffic;
	}

	public void setTraffic(String traffic) {
		this.traffic = traffic;
	}

	public String getXss() {
		return xss;
	}

	public void setXss(String xss) {
		this.xss = xss;
	}

	public String getXxe() {
		return xxe;
	}

	public void setXxe(String xxe) {
		this.xxe = xxe;
	}

	public Integer getTps() {
		return tps;
	}

	public void setTps(Integer tps) {
		this.tps = tps;
	}

	public String getIgnoreApi() {
		return ignoreApi;
	}

	public void setIgnoreApi(String ignoreApi) {
		this.ignoreApi = ignoreApi;
	}

	
	public String getShowLog() {
		return showLog;
	}

	public void setShowLog(String showLog) {
		this.showLog = showLog;
	}

	@Override
	public Long getPrimaryKey() {
		return this.dgrWebsiteId;
	}
}
