package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class DPB0123Resp {

	// Mock調用
	private Long apptJobId;

	// 直接調用
	private DPB0123ShowUI showUI;

	@Override
	public String toString() {
		return "DPB0123Resp [apptJobId=" + apptJobId + ", showUI=" + showUI + ", commLoopStatus=" + commLoopStatus
				+ ", result=" + result + "]";
	}

	// 預約工作狀態
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private TsmpInvokeCommLoopStatus commLoopStatus;

	private DPB0123Result result;	

	public Long getApptJobId() {
		return apptJobId;
	}

	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}

	public DPB0123ShowUI getShowUI() {
		return showUI;
	}

	public void setShowUI(DPB0123ShowUI showUI) {
		this.showUI = showUI;
	}

	public TsmpInvokeCommLoopStatus getCommLoopStatus() {
		return commLoopStatus;
	}

	public void setCommLoopStatus(TsmpInvokeCommLoopStatus commLoopStatus) {
		this.commLoopStatus = commLoopStatus;
	}

	public DPB0123Result getResult() {
		return result;
	}

	public void setResult(DPB0123Result result) {
		this.result = result;
	}

}
