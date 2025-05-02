package tpi.dgrv4.entity.entity.sql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.component.fuzzy.FuzzyField;

@Entity
@Table(name = "tsmp_dp_faq_question")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpFaqQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_id")
	private Long questionId;

	@Fuzzy
	@Column(name = "question_name")
	private String questionName;

	@Fuzzy
	@Column(name = "question_name_en")
	private String questionNameEn;

	@Column(name = "data_sort")
	private Integer dataSort;

	/** 資料狀態: 1=啟用，0=停用(預設啟用) */
	@Column(name = "data_status")
	private String dataStatus = "1";

	@Column(name = "create_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@FuzzyField
	@Column(name = "keyword_search")
	private String keywordSearch = "";

	/* constructors */

	public TsmpDpFaqQuestion() {
	}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpFaqQuestion [questionId=" + questionId + ", questionName=" + questionName + ", questionNameEn="
				+ questionNameEn + ", dataSort=" + dataSort + ", dataStatus=" + dataStatus + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + ", keywordSearch=" + keywordSearch + "]";
	}

	/* getters and setters */

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

	public String getQuestionNameEn() {
		return questionNameEn;
	}

	public void setQuestionNameEn(String questionNameEn) {
		this.questionNameEn = questionNameEn;
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

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}

}
