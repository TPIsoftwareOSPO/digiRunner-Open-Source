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
@Table(name = "tsmp_dp_req_orderd3")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpReqOrderd3 {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "req_orderd3_id")
	private Long reqOrderd3Id;

	@Column(name = "ref_req_orderm_id")
	private Long refReqOrdermId;

	@Column(name = "client_id")
	private String clientId;

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

	public TsmpDpReqOrderd3() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpReqOrderd3 [reqOrderd3Id=" + reqOrderd3Id + ", refReqOrdermId=" + refReqOrdermId + ", clientId="
				+ clientId + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	/* getters and setters */

	public Long getReqOrderd3Id() {
		return reqOrderd3Id;
	}

	public void setReqOrderd3Id(Long reqOrderd3Id) {
		this.reqOrderd3Id = reqOrderd3Id;
	}

	public Long getRefReqOrdermId() {
		return refReqOrdermId;
	}

	public void setRefReqOrdermId(Long refReqOrdermId) {
		this.refReqOrdermId = refReqOrdermId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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
