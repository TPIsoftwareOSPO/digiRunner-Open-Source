package tpi.dgrv4.gateway.vo;

public class OCInMetrics {
	
	private Long startupTime;
	
	private Long upTime;
	
	private String cpu;
	
	private String mem;
	
	private String hUsed;

	private String hFree;
	
	private String hTotal;

	public Long getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Long startupTime) {
		this.startupTime = startupTime;
	}

	public Long getUpTime() {
		return upTime;
	}

	public void setUpTime(Long upTime) {
		this.upTime = upTime;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMem() {
		return mem;
	}

	public void setMem(String mem) {
		this.mem = mem;
	}

	public String gethUsed() {
		return hUsed;
	}

	public void sethUsed(String hUsed) {
		this.hUsed = hUsed;
	}

	public String gethFree() {
		return hFree;
	}

	public void sethFree(String hFree) {
		this.hFree = hFree;
	}

	public String gethTotal() {
		return hTotal;
	}

	public void sethTotal(String hTotal) {
		this.hTotal = hTotal;
	}
	
}
