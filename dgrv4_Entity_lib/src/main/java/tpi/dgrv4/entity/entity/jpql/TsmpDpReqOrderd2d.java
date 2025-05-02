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
@Table(name = "tsmp_dp_req_orderd2d")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
@IdClass(TsmpDpReqOrderd2dId.class)
public class TsmpDpReqOrderd2d {
	
	@Id
	@Column(name = "req_orderd2_id")
	private Long reqOrderd2Id;

	@Id
	@Column(name = "api_uid")
	private String apiUid;

	@Id
	@Column(name = "ref_theme_id")
	private Long refThemeId;

	@Column(name = "req_orderd2d_id")
	private Long reqOrderd2dId;

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

	public TsmpDpReqOrderd2d() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpReqOrderd2d [reqOrderd2Id=" + reqOrderd2Id + ", apiUid=" + apiUid + ", refThemeId=" + refThemeId
				+ ", reqOrderd2dId=" + reqOrderd2dId + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + "]\n";
	}

	/* getters and setters */

	public Long getReqOrderd2Id() {
		return reqOrderd2Id;
	}

	public void setReqOrderd2Id(Long reqOrderd2Id) {
		this.reqOrderd2Id = reqOrderd2Id;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public Long getRefThemeId() {
		return refThemeId;
	}

	public void setRefThemeId(Long refThemeId) {
		this.refThemeId = refThemeId;
	}

	public Long getReqOrderd2dId() {
		return reqOrderd2dId;
	}

	public void setReqOrderd2dId(Long reqOrderd2dId) {
		this.reqOrderd2dId = reqOrderd2dId;
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
