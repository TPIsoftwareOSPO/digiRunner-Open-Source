package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_req_log")
public class TsmpReqLog {

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "rtime")
	private Date rtime;
	
	@Column(name = "atype")
	private String atype;
	
	@Column(name = "module_name")
	private String moduleName;
	
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
	
	@Column(name = "orgid")
	private String orgid;
	
	@Column(name = "txid")
	private String txid;
	
	@Column(name = "entry")
	private String entry;
	
	@Column(name = "cid")
	private String cid;
	
	@Column(name = "tuser")
	private String tuser;
	
	@Column(name = "jti")
	private String jti;
	
	/* constructors */

	public TsmpReqLog() {}

	/* methods */
	@Override
	public String toString() {
		return "TsmpReqLog [id=" + id + ", rtime=" + rtime + ", atype=" + atype + ", moduleName=" + moduleName
				+ ", moduleVersion=" + moduleVersion + ", nodeAlias=" + nodeAlias + ", nodeId=" + nodeId + ", url="
				+ url + ", cip=" + cip + ", orgid=" + orgid + ", txid=" + txid + ", entry=" + entry + ", cid=" + cid
				+ ", tuser=" + tuser + ", jti=" + jti + "]";
	}

	/* getters and setters */
	
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

	public String getAtype() {
		return atype;
	}

	public void setAtype(String atype) {
		this.atype = atype;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
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
	
}
