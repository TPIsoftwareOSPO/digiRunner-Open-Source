package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0068ApiOnOff {

	/** 開放狀態	參考 API_AUTHORITY 值 */
	private String publicFlag;

	/** 開放狀態名稱 */
	private String publicFlagName;

	/** API清單 */
	private List<DPB0068D2> apiOnOffList;

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public List<DPB0068D2> getApiOnOffList() {
		return apiOnOffList;
	}

	public void setApiOnOffList(List<DPB0068D2> apiOnOffList) {
		this.apiOnOffList = apiOnOffList;
	}

}
