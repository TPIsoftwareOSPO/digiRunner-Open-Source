package tpi.dgrv4.dpaa.vo;

public class DPB0106RespItem {

	/** Event PK */
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

	/** 截斷後的"訊息" */
	private String infoMsg;

	/** 完整的"訊息" */
	private String oriInfoMsg;

	/** "訊息"是否被截斷 */
	private Boolean isMsgTruncated;

	/** 建立日期 格式: yyyy/MM/dd HH:mm */
	private String createDateTime;

	/** 是否封存 Y / N */
	private String archiveFlag;

	/** 是否保留 Y / N */
	private String keepFlag;

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}
	
	public Long getEventId() {
		return eventId;
	}

	public String getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(String eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	
	public String getEventTypeName() {
		return eventTypeName;
	}

	public String getEventNameId() {
		return eventNameId;
	}

	public void setEventNameId(String eventNameId) {
		this.eventNameId = eventNameId;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public String getEventName() {
		return eventName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}
	
	public String getModuleVersion() {
		return moduleVersion;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public String getOriInfoMsg() {
		return oriInfoMsg;
	}

	public void setOriInfoMsg(String oriInfoMsg) {
		this.oriInfoMsg = oriInfoMsg;
	}


	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
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

	public Boolean getIsMsgTruncated() {
		return isMsgTruncated;
	}

	public void setIsMsgTruncated(Boolean isMsgTruncated) {
		this.isMsgTruncated = isMsgTruncated;
	}

	
}
