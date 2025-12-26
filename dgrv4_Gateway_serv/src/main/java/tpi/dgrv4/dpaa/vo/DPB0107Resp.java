package tpi.dgrv4.dpaa.vo;

public class DPB0107Resp {
	/** ID (流水號) */
	private Long eventId;

	/** 事件類型代碼 METHOD_INPUT / METHOD_CHECK / ... */
	private String eventTypeId;

	/** 事件類型中文 接收參數 / 參數檢查結果 / ... */
	private String eventTypeName;

	/** 事件名稱代碼 UPLD_MODULE / SCHED_RUN / ... */
	private String eventNameId;

	/** 事件名稱中文 上傳MODULE / 排程工作生效 / ... */
	private String eventName;

	/** 模組名稱 */
	private String moduleName;

	/** 模組版本 */
	private String moduleVersion;
	
	/** 追踪 Id*/
	private String traceId;

	/** 訊息 */
	private String infoMsg;

	/** 是否封存 Y / N */
	private String archiveFlag;

	/** 是否保留 Y / N */
	private String keepFlag;
	
	/** Node 別名*/
	private String nodeAlias;
	
	/** Node 代碼*/
	private String nodeId;
	
	/** 線程名稱*/
	private String threadName;	

	/** 建立日期 格式: yyyy/MM/dd HH:mm */
	private String createDateTime;
	
	/** 建立人員*/
	private String createUser;

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

	public String getEventTypeName() {
		return eventTypeName;
	}

	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}

	public String getEventNameId() {
		return eventNameId;
	}

	public void setEventNameId(String eventNameId) {
		this.eventNameId = eventNameId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	
	public String getTraceId() {
		return traceId;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public String getArchiveFlag() {
		return archiveFlag;
	}

	public void setArchiveFlag(String archiveFlag) {
		this.archiveFlag = archiveFlag;
	}

	public String getKeepFlag() {
		return keepFlag;
	}

	public void setKeepFlag(String keepFlag) {
		this.keepFlag = keepFlag;
	}

	public String getNodeAlias() {
		return nodeAlias;
	}

	public void setNodeAlias(String nodeAlias) {
		this.nodeAlias = nodeAlias;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getNodeId() {
		return nodeId;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	
}
