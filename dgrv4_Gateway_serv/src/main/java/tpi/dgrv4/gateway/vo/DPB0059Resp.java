package tpi.dgrv4.gateway.vo;

public class DPB0059Resp implements java.io.Serializable{

	/** 若是[重做]以新的 job id 為回傳對象 */
	private Long apptJobId;

	/** 大分類 */
	private String refItemNo;

	/** 子項目 */
	private String refSubitemNo;

	/** 狀態 */
	private String status;

	/** 工作預約時間 */
	private String startDateTime;

	/** 進度 */
	private String jobStep;

	/** 執行結果 */
	private String execResult;

	/** 創建資訊 */
	private String createDateTime;

	/**  */
	private String createUser;

	/** 更新資訊 */
	private String updateDateTime;

	/**  */
	private String updateUser;

	public DPB0059Resp() {}

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public String getRefItemNo() {
		return refItemNo;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}
	
	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
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

	public String getStartDateTime() {
		return startDateTime;
	}

	public String getJobStep() {
		return jobStep;
	}
	
	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public void setJobStep(String jobStep) {
		this.jobStep = jobStep;
	}	

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}
	
	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}
	
	public String getExecResult() {
		return execResult;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
}
