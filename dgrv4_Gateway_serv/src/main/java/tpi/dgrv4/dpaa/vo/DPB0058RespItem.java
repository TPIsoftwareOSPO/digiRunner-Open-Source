package tpi.dgrv4.dpaa.vo;

public class DPB0058RespItem {

	/** 工作ID */
	private Long apptJobId;

	/** 大分類 */
	private String refItemNo;

	/** 子項目 */
	private String refSubitemNo;
	
	/** 識別資料 */
	private String identifData;

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

	/** 是否可執行Y/N, 需要計算出來, 利用startDateTime與now()來計算 */
	private String canExec;

	public DPB0058RespItem() {}

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
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

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	public String getStartDateTime() {
		return startDateTime;
	}

	public String getJobStep() {
		return jobStep;
	}

	public void setJobStep(String jobStep) {
		this.jobStep = jobStep;
	}

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}
	
	public String getExecResult() {
		return execResult;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}
	
	public String getCreateDateTime() {
		return createDateTime;
	}
	
	public void setCanExec(String canExec) {
		this.canExec = canExec;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}
	
	public String getCanExec() {
		return canExec;
	}

	public String getUpdateUser() {
		return updateUser;
	}
	
	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	
}
