package tpi.dgrv4.gateway.vo;

import java.util.Map;

public class TsmpMonitorLog {

	private String type;

	private Float cpu;

	private Integer mem;

	private Integer htotal;

	private Integer hmax;

	private Integer hfree;

	private Integer hused;

	private Long dtotal;

	private Long dused;

	private String dfs;

	/** Used%, ex: 0.5 = 50% */
	private Float dusage;

	private Long davail;

	private String ts;

	private String node;
	
	
	private Long createTimestamp;
	
	private Long mainJobSize;
	
	private Long deferrableJobSize;
	
	private Long refreshJobSize;

	/** ES */
	private Map<String, Object> es;
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Float getCpu() {
		return cpu;
	}

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

	public void setHtotal(Integer htotal) {
		this.htotal = htotal;
	}

	public Integer getHmax() {
		return hmax;
	}

	public void setHmax(Integer hmax) {
		this.hmax = hmax;
	}

	public Integer getHfree() {
		return hfree;
	}

	public void setHfree(Integer hfree) {
		this.hfree = hfree;
	}

	public Integer getHused() {
		return hused;
	}

	public void setHused(Integer hused) {
		this.hused = hused;
	}

	public Long getDtotal() {
		return dtotal;
	}

	public void setDtotal(Long dtotal) {
		this.dtotal = dtotal;
	}

	public Long getDused() {
		return dused;
	}

	public void setDused(Long dused) {
		this.dused = dused;
	}

	public String getDfs() {
		return dfs;
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

	public Long getDavail() {
		return davail;
	}

	public void setDavail(Long davail) {
		this.davail = davail;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Long getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public Long getMainJobSize() {
		return mainJobSize;
	}

	public void setMainJobSize(Long mainJobSize) {
		this.mainJobSize = mainJobSize;
	}

	public Long getDeferrableJobSize() {
		return deferrableJobSize;
	}

	public void setDeferrableJobSize(Long deferrableJobSize) {
		this.deferrableJobSize = deferrableJobSize;
	}

	public Long getRefreshJobSize() {
		return refreshJobSize;
	}

	public void setRefreshJobSize(Long refreshJobSize) {
		this.refreshJobSize = refreshJobSize;
	}

	public Map<String, Object> getEs() {
		return es;
	}

	public void setEs(Map<String, Object> es) {
		this.es = es;
	}

}