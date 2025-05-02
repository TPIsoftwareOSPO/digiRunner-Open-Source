package tpi.dgrv4.gateway.vo;

public class GtwIdPVgroupApiItem {
	/** 群組代碼 */
	private String groupId;

	/** API代碼 */
	private String apiKey;

	/** API名稱 */
	private String apiName;

	/** API顯示名稱 */
	private String apiNameShowUi;

	/** API說明 */
	private String apiDesc;

	@Override
	public String toString() {
		return "GtwIdPVgroupApiItem [groupId=" + groupId + ", apiKey=" + apiKey + ", apiName=" + apiName
				+ ", apiNameShowUi=" + apiNameShowUi + ", apiDesc=" + apiDesc + "]\n";
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiNameShowUi() {
		return apiNameShowUi;
	}

	public void setApiNameShowUi(String apiNameShowUi) {
		this.apiNameShowUi = apiNameShowUi;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}
}
