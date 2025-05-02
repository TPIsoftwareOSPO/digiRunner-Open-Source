package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0207RespItem {
	/** 轉換後的ID, YYYYMMDD格式 EX: 20230425_b52f599d */
	private String id;

	/** ID, long格式 EX: 2315699193398647197 */
	private String longId;

	/** digiRunner 的 client_id */
	private String clientId;

	/** X-Api-Key 別名 */
	private String apiKeyAlias;

	/** X-Api-Key 經過遮罩的值 */
	private String apiKeyMask;

	/** 生效日期,long格式的字串 */
	private String effectiveAt;

	/** 到期日期,long格式的字串 */
	private String expiredAt;

	/** 建立日期,long格式的字串 */
	private String createDateTime;

	/** 建立人員 */
	private String createUser;

	/** 群組資料清單 */
	private List<DPB0207GroupItem> groupDataList;

	@Override
	public String toString() {
		return "DPB0207RespItem [id=" + id + ", longId=" + longId + ", clientId=" + clientId + ", apiKeyAlias="
				+ apiKeyAlias + ", apiKeyMask=" + apiKeyMask + ", effectiveAt=" + effectiveAt + ", expiredAt="
				+ expiredAt + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", groupDataList="
				+ groupDataList + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getApiKeyAlias() {
		return apiKeyAlias;
	}

	public void setApiKeyAlias(String apiKeyAlias) {
		this.apiKeyAlias = apiKeyAlias;
	}

	public String getApiKeyMask() {
		return apiKeyMask;
	}

	public void setApiKeyMask(String apiKeyMask) {
		this.apiKeyMask = apiKeyMask;
	}

	public String getEffectiveAt() {
		return effectiveAt;
	}

	public void setEffectiveAt(String effectiveAt) {
		this.effectiveAt = effectiveAt;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public List<DPB0207GroupItem> getGroupDataList() {
		return groupDataList;
	}

	public void setGroupDataList(List<DPB0207GroupItem> groupDataList) {
		this.groupDataList = groupDataList;
	}
}
