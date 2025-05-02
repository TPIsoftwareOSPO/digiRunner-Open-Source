
package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_dc_node")
@IdClass(TsmpDcNodeId.class)
public class TsmpDcNode {
	@Id
	@Column(name = "node")
	private String node;

	@Id
	@Column(name = "dc_id")
	private Long dcId;

	@Column(name = "node_task_id")
	private Long nodeTaskId;

	@Override
	public String toString() {
		return "TsmpDcNode [node=" + node + ", dcId=" + dcId + ", nodeTaskId=" + nodeTaskId + "]\n";
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Long getDcId() {
		return dcId;
	}

	public void setDcId(Long dcId) {
		this.dcId = dcId;
	}

	public Long getNodeTaskId() {
		return nodeTaskId;
	}

	public void setNodeTaskId(Long nodeTaskId) {
		this.nodeTaskId = nodeTaskId;
	}

}