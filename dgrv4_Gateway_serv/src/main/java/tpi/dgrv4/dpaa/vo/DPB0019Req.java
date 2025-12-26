package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0019Req {

	/** 檔名 */
	private String fileName;

	/** 主題名稱 */
	private String apiThemeName;

	/** 狀態 */
	private String dataStatus;

	/** 排序 */
	private String dataSort;

	/** 使用哪些API=apiUid.apiUid.....etc */
	private List<String> useApis;

	/** 圖示內容=base64 */
	private String iconFileContent;

	public DPB0019Req() {}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

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

	public String getDataSort() {
		return dataSort;
	}

	public void setDataSort(String dataSort) {
		this.dataSort = dataSort;
	}

	public void setUseApis(List<String> useApis) {
		this.useApis = useApis;
	}

	public List<String> getUseApis() {
		return useApis;
	}
	
	public String getIconFileContent() {
		return iconFileContent;
	}

	public void setIconFileContent(String iconFileContent) {
		this.iconFileContent = iconFileContent;
	}
	
}
