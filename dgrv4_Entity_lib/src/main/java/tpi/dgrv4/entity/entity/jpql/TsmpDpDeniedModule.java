package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "TSMP_DP_DENIED_MODULE")
public class TsmpDpDeniedModule {

	@Id
	@Column(name = "REF_MODULE_NAME")
	private String refModuleName;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";
	

	/* constructors */
	public TsmpDpDeniedModule() {}


	public String getRefModuleName() {
		return refModuleName;
	}


	public void setRefModuleName(String refModuleName) {
		this.refModuleName = refModuleName;
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


	@Override
	public String toString() {
		return "TsmpDpDeniedModule [refModuleName=" + refModuleName + ", createDateTime=" + createDateTime
				+ ", createUser=" + createUser + "]";
	}

}
