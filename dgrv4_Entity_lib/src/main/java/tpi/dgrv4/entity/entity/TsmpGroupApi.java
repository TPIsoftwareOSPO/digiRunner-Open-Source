package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_group_api")
@IdClass(TsmpGroupApiId.class)
public class TsmpGroupApi {

	@Id
	@Column(name = "group_id")
	private String groupId;

	@Id
	@Column(name = "api_key")
	private String apiKey;

	@Id
	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "module_version")
	private String moduleVer;

	@Column(name = "create_time")
	private Date createTime = DateTimeUtil.now();

	/* constructors */

	public TsmpGroupApi() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpGroupApi [groupId=" + groupId + ", apiKey=" + apiKey + ", moduleName=" + moduleName + ", moduleVer="
				+ moduleVer + ", createTime=" + createTime + "]";
	}

	/* getters and setters */

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

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

	public String getModuleVer() {
		return moduleVer;
	}

	public void setModuleVer(String moduleVer) {
		this.moduleVer = moduleVer;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
