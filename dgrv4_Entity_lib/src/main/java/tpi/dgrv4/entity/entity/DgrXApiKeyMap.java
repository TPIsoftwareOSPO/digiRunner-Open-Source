package tpi.dgrv4.entity.entity;

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
@Table(name = "DGR_X_API_KEY_MAP")
public class DgrXApiKeyMap implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "api_key_map_id")
	private Long apiKeyMapId;

	@Column(name = "ref_api_key_id")
	private Long refApiKeyId;

	@Column(name = "group_id")
	private String groupId;

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

	@Override
	public Long getPrimaryKey() {
		return apiKeyMapId;
	}

	@Override
	public String toString() {
		return "DgrXApiKeyMap [apiKeyMapId=" + apiKeyMapId + ", refApiKeyId=" + refApiKeyId + ", groupId=" + groupId
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getApiKeyMapId() {
		return apiKeyMapId;
	}

	public void setApiKeyMapId(Long apiKeyMapId) {
		this.apiKeyMapId = apiKeyMapId;
	}

	public Long getRefApiKeyId() {
		return refApiKeyId;
	}

	public void setRefApiKeyId(Long refApiKeyId) {
		this.refApiKeyId = refApiKeyId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
