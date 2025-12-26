package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "tsmp_dp_api_theme")
@IdClass(TsmpDpApiThemeId.class)
public class TsmpDpApiTheme {

	@Id
	@Column(name = "ref_api_theme_id")
	private Long refApiThemeId;

	@Id
	@Column(name = "ref_api_uid")
	private String refApiUid;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	/* constructors */

	public TsmpDpApiTheme() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpApiTheme [refApiThemeId=" + refApiThemeId + ", refApiUid=" + refApiUid + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + "]";
	}

	/* getters and setters */

	public Long getRefApiThemeId() {
		return refApiThemeId;
	}

	public void setRefApiThemeId(Long refApiThemeId) {
		this.refApiThemeId = refApiThemeId;
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

}
