package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_dc_module")
@IdClass(TsmpDcModuleId.class)
public class TsmpDcModule {

	/** 部署容器編號 */
	@Id
	@Column(name = "dc_id")
	private Long dcId;

	/** Module ID (表示此部署容器要載入啟用的Module) */
	@Id
	@Column(name = "module_id")
	private Long moduleId; 

	/**  */
	@Column(name = "node_task_id")
	private Integer nodeTaskId;

	/* constructors */

	public TsmpDcModule() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDcModule [dcId=" + dcId + ", moduleId=" + moduleId + ", nodeTaskId=" + nodeTaskId + "]";
	}

	/* getters and setters */

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

	public Integer getNodeTaskId() {
		return nodeTaskId;
	}

	public void setNodeTaskId(Integer nodeTaskId) {
		this.nodeTaskId = nodeTaskId;
	}
	
}
