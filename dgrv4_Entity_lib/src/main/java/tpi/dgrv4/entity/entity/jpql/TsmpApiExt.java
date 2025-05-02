package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_api_ext")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
@IdClass(TsmpApiExtId.class)
public class TsmpApiExt {
	
	@Id
	@Column(name = "api_key")
	private String apiKey;

	@Id
	@Column(name = "module_name")
	private String moduleName;
	
	@Column(name = "dp_status")
	private String dpStatus;

	@Column(name = "dp_stu_date_time")
	private Date dpStuDateTime;

	@Column(name = "ref_orderm_id")
	private Long refOrdermId;

	@Column(name = "api_ext_id")
	private Long apiExtId;

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

	/* constructors */

	public TsmpApiExt() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpApiExt [apiKey=" + apiKey + ", moduleName=" + moduleName + ", dpStatus=" + dpStatus
				+ ", dpStuDateTime=" + dpStuDateTime + ", refOrdermId=" + refOrdermId + ", apiExtId=" + apiExtId
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]\n";
	}

	/* getters and setters */

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getDpStatus() {
		return dpStatus;
	}

	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}

	public Date getDpStuDateTime() {
		return dpStuDateTime;
	}

	public void setDpStuDateTime(Date dpStuDateTime) {
		this.dpStuDateTime = dpStuDateTime;
	}

	public Long getRefOrdermId() {
		return refOrdermId;
	}

	public void setRefOrdermId(Long refOrdermId) {
		this.refOrdermId = refOrdermId;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
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
