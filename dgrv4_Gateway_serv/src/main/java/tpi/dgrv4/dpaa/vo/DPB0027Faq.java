package tpi.dgrv4.dpaa.vo;

public class DPB0027Faq {

	/** ID (流水號) */
	private Long questionId;

	/** 問題題目	 */
	private String questionName;

	/** 資料排序 */
	private Integer dataSort;

	/** 資料狀態 */
	private String dataStatus;

	/** ID (流水號) */
	private Long answerId;

	/** 問題題目答案	只要顯示前30個字元+ "..." */
	private String answerName;

	public DPB0027Faq() {}

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

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
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
	
}
