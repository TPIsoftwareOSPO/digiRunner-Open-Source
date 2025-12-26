package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewStatus;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_req_orders")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpReqOrders extends BasicFields {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "req_orders_id")
	private Long reqOrdersId;

	@Column(name = "req_orderm_id")
	private Long reqOrdermId;

	@Column(name = "layer")
	private Integer layer;

	@Fuzzy
	@Column(name = "req_comment")
	private String reqComment;

	@Column(name = "review_status")
	private String reviewStatus = TsmpDpReqReviewStatus.WAIT1.value();

	@Column(name = "status")
	private String status = TsmpDpDataStatus.ON.value();

	@Column(name = "proc_flag")
	private Integer procFlag;

	/* constructors */

	public TsmpDpReqOrders() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpReqOrders [reqOrdersId=" + reqOrdersId + ", reqOrdermId=" + reqOrdermId + ", layer="
				+ layer + ", reqComment=" + reqComment + ", reviewStatus=" + reviewStatus + ", status=" + status
				+ ", procFlag=" + procFlag + ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()=" + getCreateUser()
				+ ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()=" + getUpdateUser()
				+ ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch() + "]";
	}

	/* getters and setters */

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getProcFlag() {
		return procFlag;
	}

	public void setProcFlag(Integer procFlag) {
		this.procFlag = procFlag;
	}

}
