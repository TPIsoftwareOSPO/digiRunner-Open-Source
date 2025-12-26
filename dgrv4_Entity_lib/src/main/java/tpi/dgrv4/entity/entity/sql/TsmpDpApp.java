package tpi.dgrv4.entity.entity.sql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.component.fuzzy.FuzzyField;

@Entity
@Table(name = "tsmp_dp_app")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpApp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_id")
	private Long appId;

	@Column(name = "ref_app_cate_id")
	private Long refAppCateId;

	@Column(name = "name")
	private String name;

	@Fuzzy
	@Column(name = "intro")
	private String intro;

	@Fuzzy
	@Column(name = "author", nullable = true)
	private String author;

	/** 資料狀態: 1=啟用, 0=停用(預設啟用) */
	@Column(name = "data_status")
	private String dataStatus = "1";

	@Column(name = "create_time")
	private Date createDateTime = DateTimeUtil.now();
	
	@FuzzyField
	@Column(name = "keyword_search")
	private String keywordSearch = "";

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "org_id", nullable = true)
	private String orgId;

	@Version
	@Column(name = "version")
	private Long version = 1L;
	

	/* constructors */

	public TsmpDpApp() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpApp [appId=" + appId + ", refAppCateId=" + refAppCateId + ", name=" + name + ", intro=" + intro
				+ ", author=" + author + ", dataStatus=" + dataStatus + ", orgId=" + orgId + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
				+ ", version=" + version + ", keywordSearch=" + keywordSearch + "]";
	}

	/* getters and setters */

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public void setRefAppCateId(Long refAppCateId) {
		this.refAppCateId = refAppCateId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIntro() {
		return intro;
	}
	
	public Long getRefAppCateId() {
		return refAppCateId;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getAuthor() {
		return author;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}
	
	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
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
	
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}

}
