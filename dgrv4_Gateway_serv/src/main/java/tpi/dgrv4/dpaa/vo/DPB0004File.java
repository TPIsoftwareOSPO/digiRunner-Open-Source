package tpi.dgrv4.dpaa.vo;

public class DPB0004File {

	/** 檔案名稱 */
	private String fileName;

	/** 檔案路徑 */
	private String filePath;

	/** PK:下載檔案時使用 */
	private Long fileId;

	public DPB0004File() {}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	
}
