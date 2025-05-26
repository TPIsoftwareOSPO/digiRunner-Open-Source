package tpi.dgrv4.dpaa.vo;

public class DPB0082Req {
	
	/** base64 FILE 內容 */
	String attachFile;
	
	/** Base64.encode(檔名) */
	String fileName;

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
	
}
