package tpi.dgrv4.entity.entity.jpql;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.utils.DateTimeUtil;

@Entity
@Table(name = "dgr_composer_flow")
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class DgrComposerFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "flow_id")
	private Long flowId;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "API_ID")
	private String apiId;

	@Column(name = "flow_data")
	private byte[] flowData;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Version
	@Column(name = "version")
	private Long version = 1L;
	
	@Transient
	private String flowDataString;

	public Long getFlowId() {
		return flowId;
	}

	public void setFlowId(Long flowId) {
		this.flowId = flowId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public byte[] getFlowData() {
		return flowData;
	}

	public void setFlowData(byte[] flowData) {
		this.flowData = flowData;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "DgrComposerFlow [flowId=" + flowId + ", moduleName=" + moduleName + ", apiId=" + apiId + ", flowData="
				+ flowData + ", createDateTime=" + createDateTime + ", updateDateTime=" + updateDateTime + ", version="
				+ version + "]";
	}
	
	@JsonIgnore
	public String getFlowDataAsString() {
		if (null == flowDataString) {
			flowDataString = new String(flowData, StandardCharsets.UTF_8);
		}

		return flowDataString;
	}

}
