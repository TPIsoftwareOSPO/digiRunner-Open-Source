package tpi.dgrv4.dpaa.vo;

public class DashboardAideVo {
	private String moudleName;
	private String apiKey;
	private long total;
	private long success;
	private long fail;
	private long elapse;
	
	
	public String getMoudleName() {
		return moudleName;
	}
	public void setMoudleName(String moudleName) {
		this.moudleName = moudleName;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public long getSuccess() {
		return success;
	}
	public void setSuccess(long success) {
		this.success = success;
	}
	public long getFail() {
		return fail;
	}
	public void setFail(long fail) {
		this.fail = fail;
	}
	public long getElapse() {
		return elapse;
	}
	public void setElapse(long elapse) {
		this.elapse = elapse;
	}
	
	
}
