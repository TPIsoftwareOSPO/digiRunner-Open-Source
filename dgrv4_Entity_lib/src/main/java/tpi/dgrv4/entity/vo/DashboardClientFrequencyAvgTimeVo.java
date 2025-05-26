package tpi.dgrv4.entity.vo;

public class DashboardClientFrequencyAvgTimeVo {

	private String cid;
	private String moduleName;
	private String txid;
	private String exeStatus;
	private Long sumElapse;
	private Long frequency;
	
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public String getExeStatus() {
		return exeStatus;
	}
	public void setExeStatus(String exeStatus) {
		this.exeStatus = exeStatus;
	}
	public Long getSumElapse() {
		return sumElapse;
	}
	public void setSumElapse(Long sumElapse) {
		this.sumElapse = sumElapse;
	}
	public Long getFrequency() {
		return frequency;
	}
	public void setFrequency(Long frequency) {
		this.frequency = frequency;
	}
	
	
}
