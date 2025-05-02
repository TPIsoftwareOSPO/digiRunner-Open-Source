package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_chk_log")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpChkLog {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "chk_log_id")
	private Long chkLogId;

	@Column(name = "req_orders_id")
	private Long reqOrdersId;

	@Column(name = "req_orderm_id")
	private Long reqOrdermId;

	@Column(name = "layer")
	private Integer layer;

	@Column(name = "req_comment")
	private String reqComment;

	@Column(name = "review_status")
	private String reviewStatus;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	/* constructors */

	public TsmpDpChkLog() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpChkLog [chkLogId=" + chkLogId + ", reqOrdersId=" + reqOrdersId + ", reqOrdermId=" + reqOrdermId
				+ ", layer=" + layer + ", reqComment=" + reqComment + ", reviewStatus=" + reviewStatus
				+ ", createDateTime=" + createDateTime + ", createUser=" + createUser + "]\n";
	}

	/* getters and setters */

	public Long getChkLogId() {
		return chkLogId;
	}

	public void setChkLogId(Long chkLogId) {
		this.chkLogId = chkLogId;
	}

	public Long getReqOrdersId() {
		return reqOrdersId;
	}

	public void setReqOrdersId(Long reqOrdersId) {
		this.reqOrdersId = reqOrdersId;
	}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Integer getLayer() {
		return layer;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public String getReqComment() {
		return reqComment;
	}

	public void setReqComment(String reqComment) {
		this.reqComment = reqComment;
	}

	public String getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
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

}
