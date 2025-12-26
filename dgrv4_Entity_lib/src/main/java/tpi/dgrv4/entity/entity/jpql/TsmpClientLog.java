package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_client_log")
public class TsmpClientLog {

	@Id
	@Column(name = "log_seq")
	private String logSeq;

	@Column(name = "is_login")
	private Integer isLogin;

	@Column(name = "agent")
	private String agent;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "event_msg")
	private String eventMsg;

	@Column(name = "event_time")
	private Date eventTime;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "client_ip")
	private String clientIp;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "txsn")
	private String txsn;

	@Column(name = "create_time")
	private Date createTime;

	/* constructors */

	public TsmpClientLog() {
	}

	public String getLogSeq() {
		return logSeq;
	}

	public void setLogSeq(String logSeq) {
		this.logSeq = logSeq;
	}

	public Integer getIsLogin() {
		return isLogin;
	}

	public void setIsLogin(Integer isLogin) {
		this.isLogin = isLogin;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventMsg() {
		return eventMsg;
	}

	public void setEventMsg(String eventMsg) {
		this.eventMsg = eventMsg;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTxsn() {
		return txsn;
	}

	public void setTxsn(String txsn) {
		this.txsn = txsn;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "TsmpClientLog [logSeq=" + logSeq + ", isLogin=" + isLogin + ", agent=" + agent + ", eventType="
				+ eventType + ", eventMsg=" + eventMsg + ", eventTime=" + eventTime + ", clientId=" + clientId
				+ ", clientIp=" + clientIp + ", userName=" + userName + ", txsn=" + txsn + ", createTime=" + createTime
				+ "]";
	}

}
