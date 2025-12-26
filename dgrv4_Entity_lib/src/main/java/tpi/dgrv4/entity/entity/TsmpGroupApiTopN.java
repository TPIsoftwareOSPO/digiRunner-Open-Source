package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_group_api")
@IdClass(TsmpGroupApiId.class)
public class TsmpGroupApiTopN {

	@Id
	@Column(name = "group_id")
	private String groupId;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "count_size")
	private Long countSize;

	public TsmpGroupApiTopN() {
	}

	public TsmpGroupApiTopN(String moduleName, String apiKey, Long countSize) {
		this.moduleName = moduleName;
		this.apiKey = apiKey;
		this.countSize = countSize;
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

	public Long getCountSize() {
		return countSize;
	}

	public void setCountSize(Long countSize) {
		this.countSize = countSize;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
}
