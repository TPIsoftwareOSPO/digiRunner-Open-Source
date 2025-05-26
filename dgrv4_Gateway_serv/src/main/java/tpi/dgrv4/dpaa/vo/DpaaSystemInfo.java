package tpi.dgrv4.dpaa.vo;

public class DpaaSystemInfo {

	private Float cpu;

	private Integer mem;

	private Integer hmax;

	private Integer hfree;
	
	private Integer htotal;

	private Integer hused;

	private Long dused;
	
	private Long dtotal;

	private String dfs;

	/** Used%, ex: 0.5 = 50% */
	private Float dusage;

	private Long davail;

	private double metaspacePercent;

	public void setCpu(Float cpu) {
		this.cpu = cpu;
	}

	public Integer getMem() {
		return mem;
	}

	public void setMem(Integer mem) {
		this.mem = mem;
	}

	public Integer getHtotal() {
		return htotal;
	}
	
	public Float getCpu() {
		return cpu;
	}

	public void setHtotal(Integer htotal) {
		this.htotal = htotal;
	}

	public Integer getHmax() {
		return hmax;
	}

	public void setHmax(Integer hmax) {
		this.hmax = hmax;
	}

	public void setHfree(Integer hfree) {
		this.hfree = hfree;
	}

	public void setHused(Integer hused) {
		this.hused = hused;
	}
	
	public Integer getHused() {
		return hused;
	}

	public Long getDtotal() {
		return dtotal;
	}

	public void setDtotal(Long dtotal) {
		this.dtotal = dtotal;
	}

	public Integer getHfree() {
		return hfree;
	}
	
	public Long getDused() {
		return dused;
	}

	public void setDused(Long dused) {
		this.dused = dused;
	}

	public void setDfs(String dfs) {
		this.dfs = dfs;
	}

	public Float getDusage() {
		return dusage;
	}

	public void setDusage(Float dusage) {
		this.dusage = dusage;
	}

	public String getDfs() {
		return dfs;
	}
	
	public Long getDavail() {
		return davail;
	}

	public void setDavail(Long davail) {
		this.davail = davail;
	}

	public void setMetaspacePercent(double monitorMetaspace) {
		this.metaspacePercent = monitorMetaspace;
	}

	public String getMetaspacePercent() {
		return String.format("%.2f%%", this.metaspacePercent);
	}

}