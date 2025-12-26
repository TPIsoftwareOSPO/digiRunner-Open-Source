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

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.component.fuzzy.FuzzyField;

@Entity
@Table(name = "tsmp_dp_theme_category")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpThemeCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long apiThemeId;

	@Fuzzy
	@Column(name = "theme_name")
	private String apiThemeName;

	/** 資料狀態:1=啟用，0=停用(預設啟用) */
	@Column(name = "data_status")
	private String dataStatus = TsmpDpDataStatus.ON.value();

	@Column(name = "data_sort", nullable = true)
	private Integer dataSort;

	@Column(name = "org_id", nullable = true)
	private String orgId;

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "create_time")
	private Date createDateTime = DateTimeUtil.now();

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@FuzzyField
	@Column(name = "keyword_search")
	private String keywordSearch = "";

	/* constructors */

	public TsmpDpThemeCategory() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpThemeCategory [apiThemeId=" + apiThemeId + ", apiThemeName=" + apiThemeName + ", dataStatus="
				+ dataStatus + ", dataSort=" + dataSort + ", org_id=" + orgId + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + ", keywordSearch=" + keywordSearch + "]\n";
	}

	/* getters and setters */

	public Long getApiThemeId() {
		return apiThemeId;
	}

	public void setApiThemeId(Long apiThemeId) {
		this.apiThemeId = apiThemeId;
	}

	public String getApiThemeName() {
		return apiThemeName;
	}

	public void setApiThemeName(String apiThemeName) {
		this.apiThemeName = apiThemeName;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
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
	
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
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

	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}

}
