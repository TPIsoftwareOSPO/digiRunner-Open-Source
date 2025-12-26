package tpi.dgrv4.dpaa.vo;

public class DPB0061RespItem {

	/** 檔案流水號 */
	private Long fileId;

	/** 檔案名稱 */
	private String fileName;

	/** 檔案路徑 */
	private String filePath;

	/** 是否可預覽 */
	private String isPreviewable;

	/** 檔案內容文字 */
	private String fileContent;

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getIsPreviewable() {
		return isPreviewable;
	}

	public void setIsPreviewable(String isPreviewable) {
		this.isPreviewable = isPreviewable;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

}