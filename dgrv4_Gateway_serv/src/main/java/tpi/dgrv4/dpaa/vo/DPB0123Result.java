package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
 
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DPB0123Result {

	// 顯示排程狀態
	private TsmpInvokeCommLoopStatus commLoopStatus;
	
	// 直接調用
	private DPB0123ShowUI showUI;

	public TsmpInvokeCommLoopStatus getCommLoopStatus() {
		return commLoopStatus;
	}

	public void setCommLoopStatus(TsmpInvokeCommLoopStatus commLoopStatus) {
		this.commLoopStatus = commLoopStatus;
	}

	public DPB0123ShowUI getShowUI() {
		return showUI;
	}

	public void setShowUI(DPB0123ShowUI showUI) {
		this.showUI = showUI;
	}
}