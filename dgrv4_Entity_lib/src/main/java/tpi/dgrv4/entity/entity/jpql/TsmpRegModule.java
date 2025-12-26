package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_reg_module")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpRegModule {
	
	@Id
	@Column(name = "reg_module_id")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	private Long regModuleId;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "module_version")
	private String moduleVersion;

	@Column(name = "module_src")
	private String moduleSrc;

	@Column(name = "latest")
	private String latest = "N";

	@Column(name = "upload_date_time")
	private Date uploadDateTime;

	@Column(name = "upload_user")
	private String uploadUser;
	
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
	public TsmpRegModule() {}

	/* methods */
	@Override
	public String toString() {
		return "TsmpRegModule [regModuleId=" + regModuleId + ", moduleName=" + moduleName + ", moduleVersion="
				+ moduleVersion + ", moduleSrc=" + moduleSrc + ", latest=" + latest + ", uploadDateTime="
				+ uploadDateTime + ", uploadUser=" + uploadUser + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]";
	}

	/* getters and setters */
	public Long getRegModuleId() {
		return regModuleId;
	}

	public void setRegModuleId(Long regModuleId) {
		this.regModuleId = regModuleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getModuleSrc() {
		return moduleSrc;
	}

	public void setModuleSrc(String moduleSrc) {
		this.moduleSrc = moduleSrc;
	}

	public String getLatest() {
		return latest;
	}

	public void setLatest(String latest) {
		this.latest = latest;
	}

	public Date getUploadDateTime() {
		return uploadDateTime;
	}

	public void setUploadDateTime(Date uploadDateTime) {
		this.uploadDateTime = uploadDateTime;
	}

	public String getUploadUser() {
		return uploadUser;
	}

	public void setUploadUser(String uploadUser) {
		this.uploadUser = uploadUser;
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
