package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_node")
public class TsmpNode {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "node")
	private String node;

	@Override
	public String toString() {
		return "TsmpNode [id=" + id + ", startTime=" + startTime + ", updateTime=" + updateTime + ", node=" + node
				+ "]\n";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

}
