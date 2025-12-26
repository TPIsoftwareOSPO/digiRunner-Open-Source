package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DGR_DASHBOARD_ES_LOG	")
public class DgrDashboardEsLog {
	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "rtime")
	private Date rtime;
	
	@Column(name = "module_name")
	private String moduleName;
	
	@Column(name = "orgid")
	private String orgid;
	
	@Column(name = "txid")
	private String txid;
	
	@Column(name = "cid")
	private String cid;
	
	@Column(name = "exe_status")
	private String exeStatus;

	@Column(name = "elapse")
	private Integer elapse;

	@Column(name = "http_status")
	private Integer httpStatus;

	@Column(name = "rtime_year_month")
	private String rtimeYearMonth;
	
	public DgrDashboardEsLog() {}

	@Override
	public String toString() {
		return "DgrDashboardEsLog [id=" + id + ", rtime=" + rtime + ", moduleName=" + moduleName + ", orgid=" + orgid
				+ ", txid=" + txid + ", cid=" + cid + ", exeStatus=" + exeStatus + ", elapse=" + elapse
				+ ", httpStatus=" + httpStatus + ", rtimeYearMonth=" + rtimeYearMonth + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getRtime() {
		return rtime;
	}

	public void setRtime(Date rtime) {
		this.rtime = rtime;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getOrgid() {
		return orgid;
	}

	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getExeStatus() {
		return exeStatus;
	}

	public void setExeStatus(String exeStatus) {
		this.exeStatus = exeStatus;
	}

	public Integer getElapse() {
		return elapse;
	}

	public void setElapse(Integer elapse) {
		this.elapse = elapse;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getRtimeYearMonth() {
		return rtimeYearMonth;
	}

	public void setRtimeYearMonth(String rtimeYearMonth) {
		this.rtimeYearMonth = rtimeYearMonth;
	}
	
}
