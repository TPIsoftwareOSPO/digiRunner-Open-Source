package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "tsmp_req_res_log_history")
public class TsmpReqResLogHistory {
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
	
	@Column(name = "atype")
	private String atype;
	
	@Column(name = "module_version")
	private String moduleVersion;
	
	@Column(name = "node_alias")
	private String nodeAlias;
	
	@Column(name = "node_id")
	private String nodeId;
	
	@Column(name = "url")
	private String url;
	
	@Column(name = "cip")
	private String cip;
	
	@Column(name = "entry")
	private String entry;
	
	@Column(name = "tuser")
	private String tuser;
	
	@Column(name = "jti")
	private String jti;

	@Column(name = "rcode")
	private String rcode;

	@Column(name = "err_msg")
	private String errMsg;
	

	public TsmpReqResLogHistory() {}
	
	public TsmpReqResLogHistory(String id, Date rtime, String moduleName, String txid, String cid, String exeStatus,
			Integer elapse, Integer httpStatus) {
		this.id = id;
		this.rtime = rtime;
		this.moduleName = moduleName;
		this.txid = txid;
		this.cid = cid;
		this.exeStatus = exeStatus;
		this.elapse = elapse;
		this.httpStatus = httpStatus;
	}

	@Override
	public String toString() {
		return "TsmpReqResLogHistory [id=" + id + ", rtime=" + rtime + ", moduleName=" + moduleName + ", orgid=" + orgid
				+ ", txid=" + txid + ", cid=" + cid + ", exeStatus=" + exeStatus + ", elapse=" + elapse
				+ ", httpStatus=" + httpStatus + ", rtimeYearMonth=" + rtimeYearMonth + ", atype=" + atype
				+ ", moduleVersion=" + moduleVersion + ", nodeAlias=" + nodeAlias + ", nodeId=" + nodeId + ", url="
				+ url + ", cip=" + cip + ", entry=" + entry + ", tuser=" + tuser + ", jti=" + jti + ", rcode=" + rcode
				+ ", errMsg=" + errMsg + "]";
	}

	public String getAtype() {
		return atype;
	}

	public void setAtype(String atype) {
		this.atype = atype;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getNodeAlias() {
		return nodeAlias;
	}

	public void setNodeAlias(String nodeAlias) {
		this.nodeAlias = nodeAlias;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCip() {
		return cip;
	}

	public void setCip(String cip) {
		this.cip = cip;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getTuser() {
		return tuser;
	}

	public void setTuser(String tuser) {
		this.tuser = tuser;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getRcode() {
		return rcode;
	}

	public void setRcode(String rcode) {
		this.rcode = rcode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
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
