package tpi.dgrv4.entity.entity.jpql;

import java.sql.Blob;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@SqlResultSetMapping(
    name = "TsmpApiModuleMapping",
    classes = @ConstructorResult(
		targetClass = TsmpApiModule.class,
    	columns = {
			@ColumnResult(name = "src", type = String.class),
    		@ColumnResult(name = "id", type = Long.class),
            @ColumnResult(name = "module_name", type = String.class),
            @ColumnResult(name = "module_version", type = String.class),
            @ColumnResult(name = "active", type = Boolean.class)
    	}
    )
)
@Table(name = "tsmp_api_module")
public class TsmpApiModule {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "module_version")
	private String moduleVersion;

	@Column(name = "module_app_class")
	private String moduleAppClass;

	@Column(name = "module_bytes")
	private Blob moduleBytes;

	@Column(name = "module_md5")
	private String moduleMd5;

	@Column(name = "module_type")
	private String moduleType;

	@Column(name = "upload_time")
	private Date uploadTime;

	@Column(name = "uploader_name")
	private String uploaderName;

	@Column(name = "status_time")
	private Date statusTime;

	@Column(name = "status_user")
	private String statusUser;

	/** Module狀態: TRUE=Active, FALSE=Disable */
	@Column(name = "active")
	private Boolean active;

	/** Module啟動/停止的任務識別碼 */
	@Column(name = "node_task_id")
	private Integer nodeTaskId;

	/** 1: V2架構; 2: V3架構 (null也是V2) */
	@Column(name = "v2_flag")
	private Integer v2Flag;

	@Column(name = "org_id")
	private String orgId;

	/** 'M'=from TsmpApiModule, 'N'=from TsmpnApiModule(.Net) */
	@Transient
	private String src;

	/* constructors */

	public TsmpApiModule() {}

	public TsmpApiModule(Long id, String moduleName, String moduleVersion, Boolean active) {
		this.id = id;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.active = active;
	}
	
	public TsmpApiModule(Long id, String moduleName, String moduleVersion, Date uploadTime, Boolean active, Integer v2Flag) {
		this.id = id;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.uploadTime = uploadTime;
		this.active = active;
		this.v2Flag = v2Flag;
	}

	public TsmpApiModule(String src, Long id, String moduleName, String moduleVersion, Boolean active) {
		this.src = src;
		this.id = id;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.active = active;
	}

	// 包含所有欄位，除了 module_bytes 以外
	public TsmpApiModule(
		Long id, String moduleName, String moduleVersion, //
		String moduleAppClass, String moduleMd5, String moduleType,	//
		Date uploadTime, String uploaderName,	//
		Date statusTime, String statusUser,	//
		Boolean active, Integer nodeTaskId, Integer v2Flag,	String orgId
	) {
		this.id = id;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.moduleAppClass = moduleAppClass;
		this.moduleMd5 = moduleMd5;
		this.moduleType = moduleType;
		this.uploadTime = uploadTime;
		this.uploaderName = uploaderName;
		this.statusTime = statusTime;
		this.statusUser = statusUser;
		this.active = active;
		this.nodeTaskId = nodeTaskId;
		this.v2Flag = v2Flag;
		this.orgId = orgId;
	}

	/* methods */

	@Override
	public String toString() {
		return "TsmpApiModule [id=" + id + ", moduleName=" + moduleName + ", moduleVersion=" + moduleVersion
				+ ", moduleAppClass=" + moduleAppClass + ", moduleMd5=" + moduleMd5 + ", moduleType=" + moduleType
				+ ", uploadTime=" + uploadTime + ", uploaderName=" + uploaderName + ", statusTime=" + statusTime
				+ ", statusUser=" + statusUser + ", active=" + active + ", nodeTaskId=" + nodeTaskId
				+ ", v2Flag=" + v2Flag  + ", orgId=" + orgId + ", src=" + src + "]";
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

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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

	public String getModuleMd5() {
		return moduleMd5;
	}

	public void setModuleMd5(String moduleMd5) {
		this.moduleMd5 = moduleMd5;
	}

	public String getModuleType() {
		return moduleType;
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

	public Integer getNodeTaskId() {
		return nodeTaskId;
	}

	public void setNodeTaskId(Integer nodeTaskId) {
		this.nodeTaskId = nodeTaskId;
	}

	public Integer getV2Flag() {
		return v2Flag;
	}

	public void setV2Flag(Integer v2Flag) {
		this.v2Flag = v2Flag;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}
	
}
