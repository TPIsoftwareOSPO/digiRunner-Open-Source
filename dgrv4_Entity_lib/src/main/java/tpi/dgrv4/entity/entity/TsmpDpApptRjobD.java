package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_appt_rjob_d")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpApptRjobD extends BasicFields {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "appt_rjob_d_id")
	private Long apptRjobDId;

	@Column(name = "appt_rjob_id")
	@Fuzzy
	private String apptRjobId;

	@Column(name = "ref_item_no")
	@Fuzzy
	private String refItemNo;

	@Column(name = "ref_subitem_no")
	@Fuzzy
	private String refSubitemNo;

	@Column(name = "identif_data")
	private String identifData;
	
	@Column(name = "in_params")
	private String inParams;

	@Column(name = "sort_by")
	private Integer sortBy = 0;
	

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

	public void setApptRjobDId(Long apptRjobDId) {
		this.apptRjobDId = apptRjobDId;
	}
	
	public Long getApptRjobDId() {
		return apptRjobDId;
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

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
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
