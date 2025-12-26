package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_heartbeat")
public class TsmpHeartbeat {

	@Id
	@Column(name = "node_id")
	private String nodeId;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "node_info")
	private String nodeInfo;

	@Override
	public String toString() {
		return "TsmpHeartbeat [nodeId=" + nodeId + ", startTime=" + startTime + ", updateTime=" + updateTime
				+ ", nodeInfo=" + nodeInfo + "]";
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(String nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

}
