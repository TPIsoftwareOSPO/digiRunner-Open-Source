
package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_node_task")
public class TsmpnNodeTask {
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "task_signature")
	private String taskSignature;

	@Column(name = "task_id")
	private String taskId;

	@Column(name = "coordination")
	private String coordination;

	@Column(name = "node")
	private String node;
	
	@Column(name = "execute_time")
	private Date executeTime;

	@Column(name = "notice_node")
	private String noticeNode;

	@Column(name = "notice_time")
	private Date noticeTime;
	
	@Column(name = "task_arg")
	private String taskArg;

	@Override
	public String toString() {
		return "TsmpnNodeTask [id=" + id + ", taskSignature=" + taskSignature + ", taskId=" + taskId + ", taskArg="
				+ taskArg + ", coordination=" + coordination + ", executeTime=" + executeTime + ", noticeNode="
				+ noticeNode + ", noticeTime=" + noticeTime + ", node=" + node + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTaskSignature(String taskSignature) {
		this.taskSignature = taskSignature;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskArg() {
		return taskArg;
	}

	public void setTaskArg(String taskArg) {
		this.taskArg = taskArg;
	}

	public void setNoticeTime(Date noticeTime) {
		this.noticeTime = noticeTime;
	}
	
	public String getCoordination() {
		return coordination;
	}

	public void setCoordination(String coordination) {
		this.coordination = coordination;
	}

	public Date getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(Date executeTime) {
		this.executeTime = executeTime;
	}

	public String getNoticeNode() {
		return noticeNode;
	}

	public void setNoticeNode(String noticeNode) {
		this.noticeNode = noticeNode;
	}

	public Date getNoticeTime() {
		return noticeTime;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
	
	public String getTaskSignature() {
		return taskSignature;
	}

}
