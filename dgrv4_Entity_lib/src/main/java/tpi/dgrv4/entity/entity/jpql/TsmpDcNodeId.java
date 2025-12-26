package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")

public class TsmpDcNodeId implements Serializable {

	/** TSMP Node Alias TSMP_NODE.NODE(Properties中的"tsmp.core.node.alias") */
	private String node;

	/** TSMP_DC.DC_ID */
	private Long dcId;

	public TsmpDcNodeId() {}

	public TsmpDcNodeId(String node, Long dcId) {
		super();
		this.node = node;
		this.dcId = dcId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dcId == null) ? 0 : dcId.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpDcNodeId other = (TsmpDcNodeId) obj;
		if (dcId == null) {
			if (other.dcId != null)
				return false;
		} else if (!dcId.equals(other.dcId))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
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
}
