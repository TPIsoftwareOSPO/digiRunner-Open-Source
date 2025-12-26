package tpi.dgrv4.dpaa.vo;

public class DPB9916Resp {

	private Long fileId;
	private String fileName;
	private String refFileCateCode;
	private Long refId;
	private String filePath;
	private String isBlob;
	private String createDateTime;
	private String createUser;
	private String updateUser;
	private String updateDateTime;
	private String blobData;
	private Long version;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getIsBlob() {
		return isBlob;
	}

	public void setIsBlob(String isBlob) {
		this.isBlob = isBlob;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
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

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getBlobData() {
		return blobData;
	}

	public void setBlobData(String blobData) {
		this.blobData = blobData;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}
