package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AA0318Req {

	/** 暫存檔名 */
	private String tempFileName;

	/** 此欄位不應被解析 */
	@JsonIgnore
	private String fileName;

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
