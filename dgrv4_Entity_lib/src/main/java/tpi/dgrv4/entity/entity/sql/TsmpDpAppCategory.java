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
@Table(name = "tsmp_dp_app_category")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpAppCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_cate_id")
	private Long appCateId;

	@Fuzzy
	@Column(name = "app_cate_name")
	private String appCateName;

	@Column(name = "data_sort", nullable = true)
	private Integer dataSort;

	@Column(name = "create_time")
	private Date createDateTime = DateTimeUtil.now();

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

	@FuzzyField
	@Column(name = "keyword_search")
	private String keywordSearch = "";

	/* constructors */

	public TsmpDpAppCategory() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpAppCategory [appCateId=" + appCateId + ", appCateName=" + appCateName + ", dataSort=" + dataSort
				+ ", orgId=" + orgId + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + ", keywordSearch="
				+ keywordSearch + "]";
	}

	/* getters and setters */

	public Long getAppCateId() {
		return appCateId;
	}

	public void setAppCateId(Long appCateId) {
		this.appCateId = appCateId;
	}

	public String getAppCateName() {
		return appCateName;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	public void setAppCateName(String appCateName) {
		this.appCateName = appCateName;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
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
	
	public String getCreateUser() {
		return createUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}
	
	public void setVersion(Long version) {
		this.version = version;
	}

}
