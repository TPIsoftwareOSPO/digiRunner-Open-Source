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

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_req_orderm")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpReqOrderm extends BasicFields {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "req_orderm_id")
	private Long reqOrdermId;

	@Fuzzy
	@Column(name = "req_order_no")
	private String reqOrderNo;

	@Column(name = "req_type")
	private String reqType;

	@Column(name = "req_subtype")
	private String reqSubtype;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "org_id")
	private String orgId;

	@Fuzzy
	@Column(name = "req_desc")
	private String reqDesc;

	@Column(name = "req_user_id")
	private String reqUserId;

	@Column(name = "effective_date")
	private Date effectiveDate;

	/* constructors */

	public TsmpDpReqOrderm() {}

	/* methods */

	public void ensureReqOrderNo(String type, String dateString, Long seq) {
		if (
			!StringUtils.isEmpty(type) &&
			!StringUtils.isEmpty(dateString) &&
			seq != null
		) {
			final String seqString = String.format("%04d", seq);
			final String reqOrderNo = type.concat("-").concat(dateString).concat("-").concat(seqString);
			setReqOrderNo(reqOrderNo);
		}
	}

	@Override
	public String toString() {
		return "TsmpDpReqOrderm [reqOrdermId=" + reqOrdermId + ", reqOrderNo=" + reqOrderNo + ", reqType=" + reqType
				+ ", reqSubtype=" + reqSubtype + ", clientId=" + clientId + ", orgId=" + orgId + ", reqDesc=" + reqDesc
				+ ", reqUserId=" + reqUserId + ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()="
				+ getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()="
				+ getUpdateUser() + ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch()
				+ "]\n";
	}

	/* getters and setters */

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getReqOrderNo() {
		return reqOrderNo;
	}

	public void setReqOrderNo(String reqOrderNo) {
		this.reqOrderNo = reqOrderNo;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getReqDesc() {
		return reqDesc;
	}

	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	public String getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

}
