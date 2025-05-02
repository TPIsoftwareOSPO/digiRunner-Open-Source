package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_node_task_work")
public class TsmpnNodeTaskWork {

	@Id
	@Column(name = "id")
	private Long Id;

	@Column(name = "competitive_id")
	private String competitiveId;

	@Column(name = "competitive_time")
	private Date competitiveTime;
	
	@Column(name = "node_task_id")
	private Long nodeTaskId;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "success")
	private Boolean success;
	
	@Column(name = "node")
	private String node;
	
	@Column(name = "competitive_node")
	private String competitiveNode;

	@Column(name = "error_msg")
	private String errorMsg;

	@Override
	public String toString() {
		return "TsmpnNodeTaskWork [Id=" + Id + ", nodeTaskId=" + nodeTaskId + ", competitiveId=" + competitiveId
				+ ", competitiveTime=" + competitiveTime + ", competitiveNode=" + competitiveNode + ", updateTime="
				+ updateTime + ", success=" + success + ", errorMsg=" + errorMsg + ", node=" + node + "]\n";
	}

	public void setId(Long id) {
		Id = id;
	}

	public Long getNodeTaskId() {
		return nodeTaskId;
	}

	public void setNodeTaskId(Long nodeTaskId) {
		this.nodeTaskId = nodeTaskId;
	}

	public String getCompetitiveId() {
		return competitiveId;
	}
	
	public Long getId() {
		return Id;
	}

	public Date getCompetitiveTime() {
		return competitiveTime;
	}

	public void setCompetitiveTime(Date competitiveTime) {
		this.competitiveTime = competitiveTime;
	}
	
	public void setNode(String node) {
		this.node = node;
	}

	public String getCompetitiveNode() {
		return competitiveNode;
	}
	
	public void setCompetitiveId(String competitiveId) {
		this.competitiveId = competitiveId;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setCompetitiveNode(String competitiveNode) {
		this.competitiveNode = competitiveNode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

	public String getNode() {
		return node;
	}

	
}