package tpi.dgrv4.dpaa.vo;

public class AA1212NodeInfoResp {
	private Integer cpuCore;
	private String cpuUsage;
	private String memFree;
	private String memTotal;
	private String memMax;
	
	public Integer getCpuCore() {
		return cpuCore;
	}
	public void setCpuCore(Integer cpuCore) {
		this.cpuCore = cpuCore;
	}
	public String getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(String cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public String getMemFree() {
		return memFree;
	}
	public void setMemFree(String memFree) {
		this.memFree = memFree;
	}
	public String getMemTotal() {
		return memTotal;
	}
	public void setMemTotal(String memTotal) {
		this.memTotal = memTotal;
	}
	public String getMemMax() {
		return memMax;
	}
	public void setMemMax(String memMax) {
		this.memMax = memMax;
	}
	
	
}
