package tpi.dgrv4.entity.entity.autoInitSQL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_appt_rjob_d")
public class AutoInitSQLTsmpDpApptRjobD extends BasicFields {

	@Id
	@Column(name = "appt_rjob_d_id")
	private Long apptRjobDId;

	@Column(name = "ref_item_no")
	@Fuzzy
	private String refItemNo;

	@Column(name = "ref_subitem_no")
	@Fuzzy
	private String refSubitemNo;

	@Column(name = "in_params")
	private String inParams;

	@Column(name = "identif_data")
	private String identifData;
	
	@Column(name = "appt_rjob_id")
	@Fuzzy
	private String apptRjobId;

	@Column(name = "sort_by")
	private Integer sortBy = 0;
	
	/* constructors */

	public AutoInitSQLTsmpDpApptRjobD() {}

	@Override
	public String toString() {
		return "TsmpDpApptRjobD [apptRjobDId=" + apptRjobDId + ", apptRjobId=" + apptRjobId + ", refItemNo=" + refItemNo
				+ ", refSubitemNo=" + refSubitemNo + ", inParams=" + inParams + ", identifData=" + identifData
				+ ", sortBy=" + sortBy + ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()="
				+ getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()="
				+ getUpdateUser() + ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch()
				+ "]";
	}

	 
	/* getters and setters */

	public Long getApptRjobDId() {
		return apptRjobDId;
	}

	public void setApptRjobDId(Long apptRjobDId) {
		this.apptRjobDId = apptRjobDId;
	}

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

	public String getRefItemNo() {
		return refItemNo;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}

	public String getInParams() {
		return inParams;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}
	
	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

	public Integer getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}
}

