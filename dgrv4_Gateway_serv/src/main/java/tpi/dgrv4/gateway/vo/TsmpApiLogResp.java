package tpi.dgrv4.gateway.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TsmpApiLogResp {
	private String mtype;
	private String cid;
	private Long elapse;
	private String mbody;
	private String originServerResponseTime;
	private String id;
	private String moduleName;
	private String type;
	private String ts;
	private String node;
	private String url;
	private String cip;
	private Integer httpStatus;
	private Map<String, List<String>> headers;
	private String txid;
	private String aType;
	private String orgId;
	private String entry;
	private String jti;
	private Long createTimestamp;
	
	public String getMtype() {
		return mtype;
	}
	public void setMtype(String mtype) {
		this.mtype = mtype;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public Long getElapse() {
		return elapse;
	}
	public void setElapse(Long elapse) {
		this.elapse = elapse;
	}
	public String getMbody() {
		return mbody;
	}
	public void setMbody(String mbody) {
		this.mbody = mbody;
	}
	public String getOriginServerResponseTime() {
		return originServerResponseTime;
	}
	public void setOriginServerResponseTime(String originServerResponseTime) {
		this.originServerResponseTime = originServerResponseTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public Integer getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	public String getTxid() {
		return txid;
	}
	public void setTxid(String txid) {
		this.txid = txid;
	}
	public String getaType() {
		return aType;
	}
	public void setaType(String aType) {
		this.aType = aType;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getEntry() {
		return entry;
	}
	public void setEntry(String entry) {
		this.entry = entry;
	}
	public String getJti() {
		return jti;
	}
	public void setJti(String jti) {
		this.jti = jti;
	}
	public Long getCreateTimestamp() {
		return createTimestamp;
	}
	public void setCreateTimestamp(Long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}
	
	
}
