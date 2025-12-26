package tpi.dgrv4.entity.entity.jpql;

import java.sql.Blob;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_api_module")
public class TsmpnApiModule {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "module_app_class")
	private String moduleAppClass;

	@Column(name = "module_bytes")
	private Blob moduleBytes;

	@Column(name = "module_md5")
	private String moduleMd5;

	@Column(name = "module_type")
	private String moduleType;
	
	@Column(name = "module_version")
	private String moduleVersion;

	@Column(name = "upload_time")
	private Date uploadTime;

	@Column(name = "status_time")
	private Date statusTime;

	@Column(name = "status_user")
	private String statusUser;

	/** Module狀態: TRUE=Active, FALSE=Disable */
	@Column(name = "active")
	private Boolean active;

	@Column(name = "target_version")
	private String targetVersion;
	
	@Column(name = "uploader_name")
	private String uploaderName;

	@Column(name = "org_id")
	private String orgId;

	/* constructors */

	public TsmpnApiModule() {}
	
	public TsmpnApiModule(Long id, String moduleName, String moduleVersion, Boolean active) {
		this.id = id;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.active = active;
	}

	/* methods */

	@Override
	public String toString() {
		return "TsmpnApiModule [id=" + id + ", moduleName=" + moduleName + ", moduleVersion=" + moduleVersion
				+ ", moduleAppClass=" + moduleAppClass + ", moduleBytes=" + moduleBytes + ", moduleMd5=" + moduleMd5
				+ ", moduleType=" + moduleType + ", uploadTime=" + uploadTime + ", uploaderName=" + uploaderName
				+ ", statusTime=" + statusTime + ", statusUser=" + statusUser + ", active=" + active + ", targetVersion="
				+ targetVersion + ", orgId=" + orgId + "]";
	}

	/* getters and setters */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getModuleAppClass() {
		return moduleAppClass;
	}

	public void setModuleAppClass(String moduleAppClass) {
		this.moduleAppClass = moduleAppClass;
	}

	public Blob getModuleBytes() {
		return moduleBytes;
	}

	public void setModuleBytes(Blob moduleBytes) {
		this.moduleBytes = moduleBytes;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleMd5() {
		return moduleMd5;
	}

	public void setModuleMd5(String moduleMd5) {
		this.moduleMd5 = moduleMd5;
	}

	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	public String getUploaderName() {
		return uploaderName;
	}

	public void setUploaderName(String uploaderName) {
		this.uploaderName = uploaderName;
	}

	public Date getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(Date statusTime) {
		this.statusTime = statusTime;
	}
	
	public String getModuleType() {
		return moduleType;
	}


	public String getStatusUser() {
		return statusUser;
	}

	public void setStatusUser(String statusUser) {
		this.statusUser = statusUser;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getTargetVersion() {
		return targetVersion;
	}
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}

	public String getOrgId() {
		return orgId;
	}
	
}
