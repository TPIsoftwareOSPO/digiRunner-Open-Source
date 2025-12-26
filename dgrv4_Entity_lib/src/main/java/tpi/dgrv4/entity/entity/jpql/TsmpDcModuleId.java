package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TsmpDcModuleId implements Serializable {

	/** 部署容器編號 */
	private Long dcId;

	/** Module ID (表示此部署容器要載入啟用的Module) */
	private Long moduleId; 

	public TsmpDcModuleId() {}

	public TsmpDcModuleId(Long dcId, Long moduleId) {
		super();
		this.dcId = dcId;
		this.moduleId = moduleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dcId == null) ? 0 : dcId.hashCode());
		result = prime * result + ((moduleId == null) ? 0 : moduleId.hashCode());
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
		TsmpDcModuleId other = (TsmpDcModuleId) obj;
		if (dcId == null) {
			if (other.dcId != null)
				return false;
		} else if (!dcId.equals(other.dcId))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		return true;
	}

	public Long getDcId() {
		return dcId;
	}

	public void setDcId(Long dcId) {
		this.dcId = dcId;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

}
