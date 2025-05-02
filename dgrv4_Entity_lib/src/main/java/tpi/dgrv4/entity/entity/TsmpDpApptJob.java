package tpi.dgrv4.entity.entity;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_appt_job")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpApptJob{

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "appt_job_id")
	private Long apptJobId;

	@Column(name = "ref_item_no")
	private String refItemNo;

	@Column(name = "ref_subitem_no")
	private String refSubitemNo;

	@Column(name = "status")
	private String status = TsmpDpApptJobStatus.WAIT.value();

	@Column(name = "in_params")
	private String inParams;

	@Column(name = "exec_result")
	private String execResult;

	@Column(name = "exec_owner")
	private String execOwner = "SYS";

	@Column(name = "stack_trace")
	private String stackTrace;

	@Column(name = "job_step")
	private String jobStep;

	@Column(name = "start_date_time")
	private Date startDateTime ;

	@Column(name = "from_job_id")
	private Long fromJobId;

	@Column(name = "period_uid")
	private String periodUid = UUID.randomUUID().toString();

	@Column(name = "period_items_id")
	private Long periodItemsId = 0L;

	@Column(name = "period_nexttime")
	private Long periodNexttime;
	
	@Column(name = "identif_data")
	private String identifData;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	/* constructors */

	public TsmpDpApptJob() {}

	@Override
	public String toString() {
		return "TsmpDpApptJob [apptJobId=" + apptJobId + ", refItemNo=" + refItemNo + ", refSubitemNo=" + refSubitemNo
				+ ", status=" + status + ", inParams=" + inParams + ", execResult=" + execResult + ", execOwner="
				+ execOwner + ", stackTrace=" + stackTrace + ", jobStep=" + jobStep + ", startDateTime=" + startDateTime
				+ ", fromJobId=" + fromJobId + ", periodUid=" + periodUid + ", periodItemsId=" + periodItemsId
				+ ", periodNexttime=" + periodNexttime + ", identifData=" + identifData + ", createDateTime="
				+ createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime + ", updateUser="
				+ updateUser + ", version=" + version + "]";
	}

	/* getters and setters */

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInParams() {
		return inParams;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public String getExecResult() {
		return execResult;
	}

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}

	public String getExecOwner() {
		return execOwner;
	}

	public void setExecOwner(String execOwner) {
		this.execOwner = execOwner;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getJobStep() {
		return jobStep;
	}

	public void setJobStep(String jobStep) {
		this.jobStep = jobStep;
	}

	public Date getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Long getFromJobId() {
		return fromJobId;
	}

	public void setFromJobId(Long fromJobId) {
		this.fromJobId = fromJobId;
	}

	public String getPeriodUid() {
		return periodUid;
	}

	public void setPeriodUid(String periodUid) {
		this.periodUid = periodUid;
	}

	public Long getPeriodItemsId() {
		return periodItemsId;
	}

	public void setPeriodItemsId(Long periodItemsId) {
		this.periodItemsId = periodItemsId;
	}

	public Long getPeriodNexttime() {
		return periodNexttime;
	}

	public void setPeriodNexttime(Long periodNexttime) {
		this.periodNexttime = periodNexttime;
	}

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
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
 
}
