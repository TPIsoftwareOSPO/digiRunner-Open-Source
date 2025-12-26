package tpi.dgrv4.entity.entity;

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
@Table(name = "tsmp_open_apikey_map")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpOpenApiKeyMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "open_apikey_map_id")
	private Long openApiKeyMapId;
	
	@Column(name = "ref_open_apikey_id")
	private Long refOpenApiKeyId;
	
	@Column(name = "ref_api_uid")
	private String refApiUid;
	
	//
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
	public String toString() {
		return "TsmpOpenApiKeyMap [openApiKeyMapId=" + openApiKeyMapId + ", refOpenApiKeyId=" + refOpenApiKeyId
				+ ", refApiUid=" + refApiUid + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	
	public Long getOpenApiKeyMapId() {
		return openApiKeyMapId;
	}

	public void setOpenApiKeyMapId(Long openApiKeyMapId) {
		this.openApiKeyMapId = openApiKeyMapId;
	}

	public Long getRefOpenApiKeyId() {
		return refOpenApiKeyId;
	}

	public void setRefOpenApiKeyId(Long refOpenApiKeyId) {
		this.refOpenApiKeyId = refOpenApiKeyId;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
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
