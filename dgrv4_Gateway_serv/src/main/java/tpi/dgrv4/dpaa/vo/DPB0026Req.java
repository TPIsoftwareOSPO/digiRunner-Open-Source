package tpi.dgrv4.dpaa.vo;

public class DPB0026Req {

	/** 問題題目 */
	private String questionName;

	/** 資料排序 */
	private Integer dataSort;

	/** 資料狀態	1：啟用，0：停用 （預設啟用） */
	private String dataStatus;

	/** 問題題目答案 */
	private String answerName;

	/** 檔案名稱 */
	private String fileName;

	/** 檔案內容	javascript 先轉base64 */
	private String fileContent;

	public DPB0026Req() {}

	public String getQuestionName() {
		return questionName;
	}

	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public Integer getDataSort() {
		return dataSort;
	}
	
	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getAnswerName() {
		return answerName;
	}

	public void setAnswerName(String answerName) {
		this.answerName = answerName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	
}
