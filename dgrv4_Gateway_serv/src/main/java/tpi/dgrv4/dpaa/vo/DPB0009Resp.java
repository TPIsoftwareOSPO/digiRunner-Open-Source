package tpi.dgrv4.dpaa.vo;

public class DPB0009Resp {

	/** PK */
	private Long appCateId;

	/** 分類名稱 */
	private String appCateName;

	/** 資料排序 */
	private Integer dataSort;

	/** 建立日期 */
	private String createDateTime;

	/** 更新日期 */
	private String updateDateTime;

	public DPB0009Resp() {}

	public Long getAppCateId() {
		return appCateId;
	}

	public void setAppCateId(Long appCateId) {
		this.appCateId = appCateId;
	}

	public String getAppCateName() {
		return appCateName;
	}

	public void setAppCateName(String appCateName) {
		this.appCateName = appCateName;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(String updateDateTime) {
		this.updateDateTime = updateDateTime;
	}
	
}
