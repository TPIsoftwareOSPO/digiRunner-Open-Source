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
@Table(name = "tsmp_dp_faq_answer")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpFaqAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "answer_id")
	private Long answerId;

	@Fuzzy
	@Column(name = "answer_name")
	private String answerName;

	@Fuzzy
	@Column(name = "answer_name_en")
	private String answerNameEn;

	@Column(name = "ref_question_id")
	private Long refQuestionId;

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

	public TsmpDpFaqAnswer() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpFaqAnswer [answerId=" + answerId + ", answerName=" + answerName + ", answerNameEn=" + answerNameEn
				+ ", refQuestionId=" + refQuestionId + ", createDateTime=" + createDateTime + ", createUser="
				+ createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
				+ version + ", keywordSearch=" + keywordSearch + "]";
	}

	/* getters and setters */

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

	public String getAnswerNameEn() {
		return answerNameEn;
	}

	public void setAnswerNameEn(String answerNameEn) {
		this.answerNameEn = answerNameEn;
	}

	public Long getRefQuestionId() {
		return refQuestionId;
	}

	public void setRefQuestionId(Long refQuestionId) {
		this.refQuestionId = refQuestionId;
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
