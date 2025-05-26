package tpi.dgrv4.dpaa.vo;

public class DPB0028Req {

	/** ID (流水號) */
	private Long questionId;

	/** 問題題目 */
	private String questionName;

	/** ID (流水號) */
	private Long answerId;

	/** 問題題目答案 */
	private String answerName;

	/** 資料排序 */
	private Integer dataSort;

	/** 檔案名稱 */
	private String fileName;

	/** 檔案內容	javascript 先轉base64 */
	private String fileContent;

	/** 檔案名稱 */
	private String orgFileName;

	/** 檔案ID	若為空值表示要刪除, 可使用questionId */
	private Long orgFileId;

	/** 狀態	1：啟用，0：停用 （預設啟用） */
	private String dataStatus;

	public DPB0028Req() {}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public String getQuestionName() {
		return questionName;
	}

	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}

	public Long getAnswerId() {
		return answerId;
	}

	public void setAnswerId(Long answerId) {
		this.answerId = answerId;
	}

	public String getAnswerName() {
		return answerName;
	}

	public void setAnswerName(String answerName) {
		this.answerName = answerName;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
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

	public String getOrgFileName() {
		return orgFileName;
	}

	public void setOrgFileName(String orgFileName) {
		this.orgFileName = orgFileName;
	}

	public Long getOrgFileId() {
		return orgFileId;
	}

	public void setOrgFileId(Long orgFileId) {
		this.orgFileId = orgFileId;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
}
