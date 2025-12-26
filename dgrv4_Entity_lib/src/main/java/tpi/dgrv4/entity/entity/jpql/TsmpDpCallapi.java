package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_callapi")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpCallapi {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "callapi_id")
	private Long callapiId;

	@Column(name = "req_url")
	private String reqUrl;
	
	@Column(name = "req_msg")
	private String reqMsg;
	
	@Column(name = "resp_msg")
	private String respMsg;
	
	@Column(name = "token_url")
	private String tokenUrl;
	
	@Column(name = "sign_code_url")
	private String signCodeUrl;
	
	@Column(name = "auth")
	private String auth;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();
	
	@Column(name = "create_user")
	private String createUser = "SYSTEM";


	/* constructors */

	public TsmpDpCallapi() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpCallapi [callapiId=" + callapiId + ", reqUrl=" + reqUrl + ", reqMsg=" + reqMsg + ", respMsg="
				+ respMsg + ", tokenUrl=" + tokenUrl + ", signCodeUrl=" + signCodeUrl + ", auth=" + auth
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + "]";
	}

	/* getters and setters */

	public Long getCallapiId() {
		return callapiId;
	}

	public void setCallapiId(Long callapiId) {
		this.callapiId = callapiId;
	}

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

	public String getReqMsg() {
		return reqMsg;
	}

	public void setReqMsg(String reqMsg) {
		this.reqMsg = reqMsg;
	}

	public String getRespMsg() {
		return respMsg;
	}

	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	public String getSignCodeUrl() {
		return signCodeUrl;
	}

	public void setSignCodeUrl(String signCodeUrl) {
		this.signCodeUrl = signCodeUrl;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
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
