package tpi.dgrv4.gateway.vo;

import java.util.List;

public class GtwIdPVgroupResp {
	/** Client ID */
	private String clientId;

	/** Access Token授權期限 */
	private Long accessTokenValidity;

	/** Refresh Token授權期限 */
	private Long refreshTokenValidity;

	/** 虛擬群組資訊清單 */
	private List<GtwIdPVgroupItem> vgroupDataList;

	@Override
	public String toString() {
		return "GtwIdPVgroupResp [clientId=" + clientId + ", accessTokenValidity=" + accessTokenValidity
				+ ", refreshTokenValidity=" + refreshTokenValidity + ", vgroupDataList=" + vgroupDataList + "]\n";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Long getRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(Long refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public List<GtwIdPVgroupItem> getVgroupDataList() {
		return vgroupDataList;
	}

	public void setVgroupDataList(List<GtwIdPVgroupItem> vgroupDataList) {
		this.vgroupDataList = vgroupDataList;
	}
}
