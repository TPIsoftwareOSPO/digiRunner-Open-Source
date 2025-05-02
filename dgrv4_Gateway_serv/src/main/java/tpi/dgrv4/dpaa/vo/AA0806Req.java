package tpi.dgrv4.dpaa.vo;

public class AA0806Req{

	/** 啟用心跳*/
	private String enableHeartbeat;
	
	/** 主機狀態*/
	private String hostStatus;
	
	/** PK*/
	private String lastReghostId;
	
	/** 模糊搜尋 - 關鍵字*/
	private String keyword;
	
	/** 是否分頁*/
	private String paging;
	
	public String getEnableHeartbeat() {
		return enableHeartbeat;
	}

	public void setEnableHeartbeat(String enableHeartbeat) {
		this.enableHeartbeat = enableHeartbeat;
	}

	public String getHostStatus() {
		return hostStatus;
	}

	public void setHostStatus(String hostStatus) {
		this.hostStatus = hostStatus;
	}

	public String getLastReghostId() {
		return lastReghostId;
	}

	public void setLastReghostId(String lastReghostId) {
		this.lastReghostId = lastReghostId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getPaging() {
		return paging;
	}

	public void setPaging(String paging) {
		this.paging = paging;
	}

	@Override
	public String toString() {
		return "AA0806Req [enableHeartbeat=" + enableHeartbeat + ", hostStatus=" + hostStatus + ", lastReghostId="
				+ lastReghostId + ", keyword=" + keyword + ", paging=" + paging + "]";
	}
	
}
