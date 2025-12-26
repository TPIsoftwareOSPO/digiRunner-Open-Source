package tpi.dgrv4.dpaa.vo;

public class DPB0013App {

	/** ID(流水號) */
	private Long appId;

	/** 應用實例分類ID */
	private Long refAppCateId;

	/** 應用實例名稱 */
	private String name;

	/** 資料狀態 */
	private String dataStatus;

	/** 分類名稱 */
	private String appCateName;

	public DPB0013App() {}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public Long getRefAppCateId() {
		return refAppCateId;
	}

	public void setRefAppCateId(Long refAppCateId) {
		this.refAppCateId = refAppCateId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getAppCateName() {
		return appCateName;
	}

	public void setAppCateName(String appCateName) {
		this.appCateName = appCateName;
	}
	
}
