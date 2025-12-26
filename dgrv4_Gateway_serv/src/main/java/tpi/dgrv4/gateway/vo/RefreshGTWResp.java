package tpi.dgrv4.gateway.vo;

import java.util.List;

import tpi.dgrv4.dpaa.vo.AA0317Data;
import tpi.dgrv4.dpaa.vo.AA0317ReqItem;
import tpi.dgrv4.dpaa.vo.AA1120ExportData;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;

public class RefreshGTWResp {
	private AA1120ExportData clientData;
	private String orgId;
	private List<AA0317ReqItem> aa0317ReqItemList;
	private AA0317Data apiData;
	private String settingData;
	private List<TsmpTokenHistory> tokenData;
	private Long lastUpdateTimeClient;
	private Long lastUpdateTimeAPI;
	private Long lastUpdateTimeSetting;
	private Long landLastUpdateTimeToken;
	
	public AA1120ExportData getClientData() {
		return clientData;
	}
	public void setClientData(AA1120ExportData clientData) {
		this.clientData = clientData;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public List<AA0317ReqItem> getAa0317ReqItemList() {
		return aa0317ReqItemList;
	}
	public void setAa0317ReqItemList(List<AA0317ReqItem> aa0317ReqItemList) {
		this.aa0317ReqItemList = aa0317ReqItemList;
	}
	public AA0317Data getApiData() {
		return apiData;
	}
	public void setApiData(AA0317Data apiData) {
		this.apiData = apiData;
	}
	public String getSettingData() {
		return settingData;
	}
	public void setSettingData(String settingData) {
		this.settingData = settingData;
	}
	public List<TsmpTokenHistory> getTokenData() {
		return tokenData;
	}
	public void setTokenData(List<TsmpTokenHistory> tokenData) {
		this.tokenData = tokenData;
	}
	public Long getLastUpdateTimeClient() {
		return lastUpdateTimeClient;
	}
	public void setLastUpdateTimeClient(Long lastUpdateTimeClient) {
		this.lastUpdateTimeClient = lastUpdateTimeClient;
	}
	public Long getLastUpdateTimeAPI() {
		return lastUpdateTimeAPI;
	}
	public void setLastUpdateTimeAPI(Long lastUpdateTimeAPI) {
		this.lastUpdateTimeAPI = lastUpdateTimeAPI;
	}
	public Long getLastUpdateTimeSetting() {
		return lastUpdateTimeSetting;
	}
	public void setLastUpdateTimeSetting(Long lastUpdateTimeSetting) {
		this.lastUpdateTimeSetting = lastUpdateTimeSetting;
	}
	/**
	 * @return the landLastUpdateTimeToken
	 */
	public Long getLandLastUpdateTimeToken() {
		return landLastUpdateTimeToken;
	}
	/**
	 * @param landLastUpdateTimeToken the landLastUpdateTimeToken to set
	 */
	public void setLandLastUpdateTimeToken(Long landLastUpdateTimeToken) {
		this.landLastUpdateTimeToken = landLastUpdateTimeToken;
	}

}
