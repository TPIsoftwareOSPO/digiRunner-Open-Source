package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_site_module")
public class TsmpnSiteModule {

	@Id
	@Column(name = "site_id")
	private Long siteId;

	@Column(name = "module_id")
	private Long moduleId;

	@Column(name = "node_task_id")
	private Long nodeTaskId;

	/* constructors */

	public TsmpnSiteModule() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpnSiteModule [siteId=" + siteId + ", moduleId=" + moduleId + ", nodeTaskId=" + nodeTaskId + "]";
	}

	/* getters and setters */

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public Long getNodeTaskId() {
		return nodeTaskId;
	}

	public void setNodeTaskId(Long nodeTaskId) {
		this.nodeTaskId = nodeTaskId;
	}

}
