package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0020Theme {

	private Long apiThemeId;

	/** API主題名稱 */
	private String apiThemeName;

	/** 資料狀態 */
	private String dataStatus;

	/** 資料排序 */
	private Integer dataSort;

	/**  */
	private List<DPB0020Api> orgApiList;

	public DPB0020Theme() {}

	public Long getApiThemeId() {
		return apiThemeId;
	}

	public void setApiThemeId(Long apiThemeId) {
		this.apiThemeId = apiThemeId;
	}

	public String getApiThemeName() {
		return apiThemeName;
	}
	
	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public Integer getDataSort() {
		return dataSort;
	}
	
	public void setApiThemeName(String apiThemeName) {
		this.apiThemeName = apiThemeName;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public List<DPB0020Api> getOrgApiList() {
		return orgApiList;
	}

	public void setOrgApiList(List<DPB0020Api> orgApiList) {
		this.orgApiList = orgApiList;
	}
	
}
