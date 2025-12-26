package tpi.dgrv4.dpaa.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class DPB0061Resp {

	/** ID (流水號) */
	private Long apptJobId;

	/** 大分類 */
	private String refItemNo;

	private String itemName;

	/** 子項目 */
	private String refSubitemNo;

	private String subItemName;

	/** 狀態 */
	private String status;

	/** input 參數 */
	private String inParams;

	/** 執行結果 */
	private String execResult;

	/** 工作執行者 */
	private String execOwner;

	/** Exception Msg */
	private String stackTrace;

	/** 進度 */
	private String jobStep;

	/** 工作預約時間 */
	private String startDateTime;

	/** 重複工作ID */
	private Long fromJobId;

	/** 創建資訊 */
	private String createDateTime;

	/**  */
	private String createUser;

	/** 更新資訊 */
	private String updateDateTime;

	/**  */
	private String updateUser;

	private Long lv;

	/** 是否可執行Y/N, 需要計算出來, 利用startDateTime與now()來計算 */
	private String canExec;
	
	/** 識別資料 */
	private String identifData;

	/** 相關檔案清單 */
	@JsonInclude(Include.NON_NULL)
	private List<DPB0061RespItem> fileList;

	public DPB0061Resp() {}

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

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}

	public String getSubItemName() {
		return subItemName;
	}

	public void setSubItemName(String subItemName) {
		this.subItemName = subItemName;
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

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}

	public String getExecResult() {
		return execResult;
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

	public void setJobStep(String jobStep) {
		this.jobStep = jobStep;
	}
	
	public String getJobStep() {
		return jobStep;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Long getFromJobId() {
		return fromJobId;
	}

	public void setFromJobId(Long fromJobId) {
		this.fromJobId = fromJobId;
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

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getCanExec() {
		return canExec;
	}

	public void setCanExec(String canExec) {
		this.canExec = canExec;
	}

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

	public List<DPB0061RespItem> getFileList() {
		return fileList;
	}

	public void setFileList(List<DPB0061RespItem> fileList) {
		this.fileList = fileList;
	}
	
}
