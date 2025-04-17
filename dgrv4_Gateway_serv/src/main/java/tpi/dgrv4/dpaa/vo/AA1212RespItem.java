package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1212RespItem {
	private String nodeName;
	private String keeperServer = "N";
	private String ip;
	private String updateTime;
	private String totalRequest;
	private AA1212SuccessResp success;
	private AA1212FailResp fail;
	private AA1212BadAttemptResp badAttempt;
	private List<AA1212BadAttemptItemResp> badAttemptList;
	private String reqTps;
	private String respTps;
	private List<AA1212RankedResp> inclundeFailFastList;
	private List<AA1212RankedResp> inclundeFailSlowList;
	private List<AA1212RankedResp> exclundeFailFastList;
	private List<AA1212RankedResp> exclundeFailSlowList;
	private AA1212DbResp db;
	private AA1212CacheResp cache;
	private AA1212QueueResp queue;
	private AA1212NodeInfoResp nodeInfo;
	private AA1212ApiThreadStatusResp apiThreadStatus;
	
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getKeeperServer() {
		return keeperServer;
	}
	public void setKeeperServer(String keeperServer) {
		this.keeperServer = keeperServer;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getTotalRequest() {
		return totalRequest;
	}
	public void setTotalRequest(String totalRequest) {
		this.totalRequest = totalRequest;
	}
	public AA1212SuccessResp getSuccess() {
		return success;
	}
	public void setSuccess(AA1212SuccessResp success) {
		this.success = success;
	}
	public AA1212FailResp getFail() {
		return fail;
	}
	public void setFail(AA1212FailResp fail) {
		this.fail = fail;
	}
	public AA1212BadAttemptResp getBadAttempt() {
		return badAttempt;
	}
	public void setBadAttempt(AA1212BadAttemptResp badAttempt) {
		this.badAttempt = badAttempt;
	}
	public List<AA1212BadAttemptItemResp> getBadAttemptList() {
		return badAttemptList;
	}
	public void setBadAttemptList(List<AA1212BadAttemptItemResp> badAttemptList) {
		this.badAttemptList = badAttemptList;
	}
	public String getReqTps() {
		return reqTps;
	}
	public void setReqTps(String reqTps) {
		this.reqTps = reqTps;
	}
	public String getRespTps() {
		return respTps;
	}
	public void setRespTps(String respTps) {
		this.respTps = respTps;
	}
	public List<AA1212RankedResp> getInclundeFailFastList() {
		return inclundeFailFastList;
	}
	public void setInclundeFailFastList(List<AA1212RankedResp> inclundeFailFastList) {
		this.inclundeFailFastList = inclundeFailFastList;
	}
	public List<AA1212RankedResp> getInclundeFailSlowList() {
		return inclundeFailSlowList;
	}
	public void setInclundeFailSlowList(List<AA1212RankedResp> inclundeFailSlowList) {
		this.inclundeFailSlowList = inclundeFailSlowList;
	}
	public List<AA1212RankedResp> getExclundeFailFastList() {
		return exclundeFailFastList;
	}
	public void setExclundeFailFastList(List<AA1212RankedResp> exclundeFailFastList) {
		this.exclundeFailFastList = exclundeFailFastList;
	}
	public List<AA1212RankedResp> getExclundeFailSlowList() {
		return exclundeFailSlowList;
	}
	public void setExclundeFailSlowList(List<AA1212RankedResp> exclundeFailSlowList) {
		this.exclundeFailSlowList = exclundeFailSlowList;
	}
	public AA1212DbResp getDb() {
		return db;
	}
	public void setDb(AA1212DbResp db) {
		this.db = db;
	}
	public AA1212CacheResp getCache() {
		return cache;
	}
	public void setCache(AA1212CacheResp cache) {
		this.cache = cache;
	}
	public AA1212QueueResp getQueue() {
		return queue;
	}
	public void setQueue(AA1212QueueResp queue) {
		this.queue = queue;
	}
	public AA1212NodeInfoResp getNodeInfo() {
		return nodeInfo;
	}
	public void setNodeInfo(AA1212NodeInfoResp nodeInfo) {
		this.nodeInfo = nodeInfo;
	}
	public AA1212ApiThreadStatusResp getApiThreadStatus() {
		return apiThreadStatus;
	}
	public void setApiThreadStatus(AA1212ApiThreadStatusResp apiThreadStatus) {
		this.apiThreadStatus = apiThreadStatus;
	}
	
	
}
