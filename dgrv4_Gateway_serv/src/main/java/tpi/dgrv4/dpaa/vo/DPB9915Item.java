package tpi.dgrv4.dpaa.vo;

public class DPB9915Item {
	private Long fileId;
	private DPB9915Trunc fileName;
	private String refFileCateCode;
	private Long refId;
	private String lastUpdDateTime;
	private String lastUpdUser;
	private String filePath;
	private Long version;
	
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public DPB9915Trunc getFileName() {
		return fileName;
	}
	public void setFileName(DPB9915Trunc fileName) {
		this.fileName = fileName;
	}
	public String getRefFileCateCode() {
		return refFileCateCode;
	}
	public void setRefFileCateCode(String refFileCateCode) {
		this.refFileCateCode = refFileCateCode;
	}
	public Long getRefId() {
		return refId;
	}
	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public String getLastUpdDateTime() {
		return lastUpdDateTime;
	}
	public void setLastUpdDateTime(String lastUpdDateTime) {
		this.lastUpdDateTime = lastUpdDateTime;
	}
	public String getLastUpdUser() {
		return lastUpdUser;
	}
	public void setLastUpdUser(String lastUpdUser) {
		this.lastUpdUser = lastUpdUser;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	
}
