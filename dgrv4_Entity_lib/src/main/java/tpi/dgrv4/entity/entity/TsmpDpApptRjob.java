package tpi.dgrv4.entity.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_appt_rjob")
@EntityListeners(FuzzyEntityListener.class)
public class TsmpDpApptRjob extends BasicFields {

	/** UUID */
	@Id
	@Column(name = "appt_rjob_id")
	@Fuzzy
	private String apptRjobId = UUID.randomUUID().toString();

	@Column(name = "rjob_name")
	@Fuzzy
	private String rjobName;
	
	@Column(name = "cron_expression")
	private String cronExpression;

	@Column(name = "cron_json")
	private String cronJson;

	@Column(name = "cron_desc")
	private String cronDesc;

	@Column(name = "next_date_time")
	private Long nextDateTime;

	@Column(name = "last_date_time")
	private Long lastDateTime;

	@Column(name = "eff_date_time")
	private Long effDateTime;

	@Column(name = "inv_date_time")
	private Long invDateTime;

	@Column(name = "remark")
	@Fuzzy
	private String remark;

	@Column(name = "status")
	private String status = "1";

	/* constructors */

	public TsmpDpApptRjob() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpApptRjob [apptRjobId=" + apptRjobId + ", rjobName=" + rjobName + ", cronExpression="
				+ cronExpression + ", cronJson=" + cronJson + ", cronDesc=" + cronDesc + ", nextDateTime="
				+ nextDateTime + ", lastDateTime=" + lastDateTime + ", effDateTime=" + effDateTime + ", invDateTime="
				+ invDateTime + ", remark=" + remark + ", status=" + status + ", getCreateDateTime()="
				+ getCreateDateTime() + ", getCreateUser()=" + getCreateUser() + ", getUpdateDateTime()="
				+ getUpdateDateTime() + ", getUpdateUser()=" + getUpdateUser() + ", getVersion()=" + getVersion()
				+ ", getKeywordSearch()=" + getKeywordSearch() + "]";
	}

	/* getters and setters */

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

	public String getRjobName() {
		return rjobName;
	}

	public void setRjobName(String rjobName) {
		this.rjobName = rjobName;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronJson() {
		return cronJson;
	}

	public void setCronJson(String cronJson) {
		this.cronJson = cronJson;
	}

	public String getCronDesc() {
		return cronDesc;
	}

	public void setCronDesc(String cronDesc) {
		this.cronDesc = cronDesc;
	}

	public Long getNextDateTime() {
		return nextDateTime;
	}

	public void setNextDateTime(Long nextDateTime) {
		this.nextDateTime = nextDateTime;
	}

	public Long getLastDateTime() {
		return lastDateTime;
	}

	public void setLastDateTime(Long lastDateTime) {
		this.lastDateTime = lastDateTime;
	}

	public Long getEffDateTime() {
		return effDateTime;
	}

	public void setEffDateTime(Long effDateTime) {
		this.effDateTime = effDateTime;
	}

	public Long getInvDateTime() {
		return invDateTime;
	}

	public void setInvDateTime(Long invDateTime) {
		this.invDateTime = invDateTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
