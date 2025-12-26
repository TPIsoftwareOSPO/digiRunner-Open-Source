package tpi.dgrv4.dpaa.vo;

public class DPB0080Req {

	private String clientId;

	/**  */
	private String fileCateCode;

	/**  */
	private Long refId;

	/** tsmp-v3 暫不支援Multipart 格式
	private List<MultipartFile> attachFile;
	*/

	private String attachFile;

	private String fileName;

	public DPB0080Req() {}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getFileCateCode() {
		return fileCateCode;
	}

	public void setFileCateCode(String fileCateCode) {
		this.fileCateCode = fileCateCode;
	}

	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public String getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(String attachFile) {
		this.attachFile = attachFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** tsmp-v3 暫不支援Multipart 格式
	public List<MultipartFile> getAttachFile() {
		return attachFile;
	}

	public void setAttachFile(List<MultipartFile> attachFile) {
		this.attachFile = attachFile;
	}
	*/

}
