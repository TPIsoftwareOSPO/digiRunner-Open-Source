package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.common.utils.DateTimeUtil;
@Entity
@Table(name = "tsmp_events")
public class TsmpEvents {

	@Id
	@Column(name = "event_id")
	private Long eventId;

	@Column(name = "event_type_id")
	private String eventTypeId;

	@Column(name = "event_name_id")
	private String eventNameId;

	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "module_version")
	private String moduleVersion;

	@Column(name = "trace_id")
	private String traceId;

	@Column(name = "info_msg")
	private String infoMsg;

	@Column(name = "keep_flag")
	private String keepFlag = "N";

	@Column(name = "archive_flag")
	private String archiveFlag = "N";

	@Column(name = "node_alias")
	private String nodeAlias;

	@Column(name = "node_id")
	private String nodeId;

	@Column(name = "thread_name")
	private String threadName;

	// --- extends BasicFields
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Override
	public String toString() {
		return "TsmpEvents [event_id=" + eventId + ", event_type_id=" + eventTypeId + ", event_name_id=" + eventNameId
				+ ", module_name=" + moduleName + ", module_version=" + moduleVersion + ", trace_id=" + traceId
				+ ", info_msg=" + infoMsg + ", keep_flag=" + keepFlag + ", archive_flag=" + archiveFlag
				+ ", node_alias=" + nodeAlias + ", node_id=" + nodeId + ", thread_name=" + threadName
				+ ", create_date_time=" + createDateTime + ", create_user=" + createUser + "]\n";
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(String eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public String getEventNameId() {
		return eventNameId;
	}

	public void setEventNameId(String eventNameId) {
		this.eventNameId = eventNameId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public String getKeepFlag() {
		return keepFlag;
	}

	public void setKeepFlag(String keepFlag) {
		this.keepFlag = keepFlag;
	}

	public String getArchiveFlag() {
		return archiveFlag;
	}

	public void setArchiveFlag(String archiveFlag) {
		this.archiveFlag = archiveFlag;
	}

	public String getNodeAlias() {
		return nodeAlias;
	}

	public void setNodeAlias(String nodeAlias) {
		this.nodeAlias = nodeAlias;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

}
