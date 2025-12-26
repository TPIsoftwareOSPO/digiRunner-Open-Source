package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_client_host")
public class TsmpClientHost {

	@Id
	@Column(name = "host_seq")
	private Long hostSeq;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "host_name")
	private String hostName;

	@Column(name = "host_ip")
	private String hostIp;

	@Column(name = "create_time")
	private Date createTime;

	/* constructors */
	public TsmpClientHost() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpClientHost [hostSeq=" + hostSeq + ", clientId=" + clientId + ", hostName=" + hostName + ", hostIp="
				+ hostIp + ", createTime=" + createTime + "]";
	}

	/* getters and setters */
	public Long getHostSeq() {
		return hostSeq;
	}

	public void setHostSeq(Long hostSeq) {
		this.hostSeq = hostSeq;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
