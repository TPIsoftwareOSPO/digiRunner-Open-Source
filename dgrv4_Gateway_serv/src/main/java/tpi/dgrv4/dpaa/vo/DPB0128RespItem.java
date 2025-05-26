package tpi.dgrv4.dpaa.vo;

public class DPB0128RespItem {
	private Long auditLongId;
	private String txnUid;
	private String entityName;
	private String cud;
	private String cudName;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String stackTrace;
	private String oldRowContent;
	private byte[] oldRowBlob;
	private String newRowContent;
	private byte[] newRowBlob;

	public Long getAuditLongId() {
		return auditLongId;
	}

	public void setAuditLongId(Long auditLongId) {
		this.auditLongId = auditLongId;
	}

	public void setTxnUid(String txnUid) {
		this.txnUid = txnUid;
	}
	
	public String getTxnUid() {
		return txnUid;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public void setCud(String cud) {
		this.cud = cud;
	}
	
	public String getCud() {
		return cud;
	}

	public String getCudName() {
		return cudName;
	}

	public void setCudName(String cudName) {
		this.cudName = cudName;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}
	
	public String getParam2() {
		return param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}
	
	public String getParam4() {
		return param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getOldRowContent() {
		return oldRowContent;
	}

	public void setOldRowContent(String oldRowContent) {
		this.oldRowContent = oldRowContent;
	}

	public byte[] getOldRowBlob() {
		return oldRowBlob;
	}

	public void setOldRowBlob(byte[] oldRowBlob) {
		this.oldRowBlob = oldRowBlob;
	}

	public String getNewRowContent() {
		return newRowContent;
	}

	public void setNewRowContent(String newRowContent) {
		this.newRowContent = newRowContent;
	}

	public byte[] getNewRowBlob() {
		return newRowBlob;
	}

	public void setNewRowBlob(byte[] newRowBlob) {
		this.newRowBlob = newRowBlob;
	}

}
