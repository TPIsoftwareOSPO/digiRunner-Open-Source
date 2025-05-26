package tpi.dgrv4.dpaa.vo;

public class DPB0029Resp {

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
	private String orgFileName;

	/** 檔案ID */
	private Long orgFileId;

	/** 狀態	1：啟用，0：停用 （預設啟用） */
	private String dataStatus;

	public DPB0029Resp() {}

	public Long getQuestionId() {
		return questionId;
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
	
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public void setAnswerId(Long answerId) {
		this.answerId = answerId;
	}

	public String getAnswerName() {
		return answerName;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getOrgFileName() {
		return orgFileName;
	}

	public void setOrgFileName(String orgFileName) {
		this.orgFileName = orgFileName;
	}
	
	public void setAnswerName(String answerName) {
		this.answerName = answerName;
	}

	public Long getOrgFileId() {
		return orgFileId;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
	public void setOrgFileId(Long orgFileId) {
		this.orgFileId = orgFileId;
	}

}
