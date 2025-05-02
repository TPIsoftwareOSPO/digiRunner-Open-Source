package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0021Resp {

	/** API主題名稱 */
	private String apiThemeName;

	/** 資料狀態 */
	private String dataStatus;

	/** 資料排序 */
	private Integer dataSort;

	/** 存在DB中的資料 */
	private List<DPB0021Api> orgApiList;

	/** 檔名 */
	private String fileName;

	/** 圖示內容:base64 */
	private String iconFileContent;

	public DPB0021Resp() {}

	public String getApiThemeName() {
		return apiThemeName;
	}

	public void setApiThemeName(String apiThemeName) {
		this.apiThemeName = apiThemeName;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	
	public String getDataStatus() {
		return dataStatus;
	}

	public Integer getDataSort() {
		return dataSort;
	}

	public void setDataSort(Integer dataSort) {
		this.dataSort = dataSort;
	}

	public List<DPB0021Api> getOrgApiList() {
		return orgApiList;
	}

	public void setOrgApiList(List<DPB0021Api> orgApiList) {
		this.orgApiList = orgApiList;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIconFileContent() {
		return iconFileContent;
	}

	public void setIconFileContent(String iconFileContent) {
		this.iconFileContent = iconFileContent;
	}
	
}
