package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class TsmpInvokeCommLoopStatus {
	private Long apptJobId;
	private String status;
	private String statusName;
	private String stackTrace;
	
	public Long getApptJobId() {
		return apptJobId;
	}
	public void setApptJobId(Long apptJobId) {
		this.apptJobId = apptJobId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
