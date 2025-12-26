package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_dp_req_orderd5d")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
@IdClass(TsmpDpReqOrderd5dId.class)
public class TsmpDpReqOrderd5d {
	
	@Id
	@Column(name = "ref_req_orderd5_id")
	private Long refReqOrderd5Id;
	
	@Id
	@Column(name = "ref_api_uid")
	private String refApiUid;
	
	@Column(name = "req_orderd5d_id")
	private Long reqOrderd5dId;

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
		return "TsmpDpReqOrderd5d [refReqOrderd5Id=" + refReqOrderd5Id + ", refApiUid=" + refApiUid + ", reqOrderd5dId="
				+ reqOrderd5dId + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	public Long getRefReqOrderd5Id() {
		return refReqOrderd5Id;
	}

	public void setRefReqOrderd5Id(Long refReqOrderd5Id) {
		this.refReqOrderd5Id = refReqOrderd5Id;
	}

	public String getRefApiUid() {
		return refApiUid;
	}

	public void setRefApiUid(String refApiUid) {
		this.refApiUid = refApiUid;
	}

	public Long getReqOrderd5dId() {
		return reqOrderd5dId;
	}

	public void setReqOrderd5dId(Long reqOrderd5dId) {
		this.reqOrderd5dId = reqOrderd5dId;
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
