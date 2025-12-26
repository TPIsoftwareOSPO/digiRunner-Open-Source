package tpi.dgrv4.gateway.vo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TsmpApiLogReq {
	private String mtype;
	private String cid;
	private Long elapse;
	private String originServerRequestTime;
	private String id;
	private String mbody;
	private String moduleName;
	private String type;
	private String ts;
	private String node;
	private String url;
	private String cip;
	private Integer httpStatus;
	private String txid;
	private String aType;
	private String orgId;
	private String entry;
	private String jti;
	private Long createTimestamp;
	private String httpMethod;
	private Map<String, List<String>> headers;
	
	@JsonIgnore
	private long startTime;
	@JsonIgnore
	private String esUrl;
	@JsonIgnore
	private String esIdPwd;
	@JsonIgnore
	private boolean isIgnore;
	@JsonIgnore
	private String dgrcUri;
	@JsonIgnore
	private String userName;
	
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
	public String getOriginServerRequestTime() {
		return originServerRequestTime;
	}
	public void setOriginServerRequestTime(String originServerRequestTime) {
		this.originServerRequestTime = originServerRequestTime;
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
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	public String getMbody() {
		return mbody;
	}
	public void setMbody(String mbody) {
		this.mbody = mbody;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public String getEsUrl() {
		return esUrl;
	}
	public void setEsUrl(String esUrl) {
		this.esUrl = esUrl;
	}
	public String getEsIdPwd() {
		return esIdPwd;
	}
	public void setEsIdPwd(String esIdPwd) {
		this.esIdPwd = esIdPwd;
	}
	public Long getCreateTimestamp() {
		return createTimestamp;
	}
	public void setCreateTimestamp(Long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	@JsonIgnore
	public boolean isIgnore() {
		return isIgnore;
	}
	@JsonIgnore
	public void setIgnore(boolean isIgnore) {
		this.isIgnore = isIgnore;
	}
	public String getDgrcUri() {
		return dgrcUri;
	}
	public void setDgrcUri(String dgrcUri) {
		this.dgrcUri = dgrcUri;
	}
	@JsonIgnore
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
