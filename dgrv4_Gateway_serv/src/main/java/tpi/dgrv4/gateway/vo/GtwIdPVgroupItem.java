package tpi.dgrv4.gateway.vo;

import java.util.List;

public class GtwIdPVgroupItem {
	/** 虛擬群組ID */
	private String vgroupId;

	/** 虛擬群組名稱 */
	private String vgroupName;

	/** 虛擬群組別名 */
	private String vgroupAlias;

	/** 虛擬群組顯示名稱 */
	private String vgroupAliasShowUi;

	/** API資訊清單 */
	private List<GtwIdPVgroupApiItem> apiDataList;

	@Override
	public String toString() {
		return "GtwIdPVgroupItem [vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + ", vgroupAlias=" + vgroupAlias
				+ ", vgroupAliasShowUi=" + vgroupAliasShowUi + ", apiDataList=" + apiDataList + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}

	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}

	public String getVgroupAliasShowUi() {
		return vgroupAliasShowUi;
	}

	public void setVgroupAliasShowUi(String vgroupAliasShowUi) {
		this.vgroupAliasShowUi = vgroupAliasShowUi;
	}

	public List<GtwIdPVgroupApiItem> getApiDataList() {
		return apiDataList;
	}

	public void setApiDataList(List<GtwIdPVgroupApiItem> apiDataList) {
		this.apiDataList = apiDataList;
	}
}
