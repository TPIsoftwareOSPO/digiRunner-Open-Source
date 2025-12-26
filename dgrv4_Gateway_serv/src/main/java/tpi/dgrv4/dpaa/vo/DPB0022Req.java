package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0022Req {

	/** PK */
	private Long apiThemeId;

	/** 主題名稱 */
	private String apiThemeName;

	/** 狀態 */
	private String dataStatus;

	/** 排序 */
	private String dataSort;

	/** 使用哪些API=apiUid.apiUid.....etc */
	private List<String> useApis;

	/** 存在DB中的資料, 只允許remove, 不可以做資料修改 */
	private List<String> orgApiList;

	/** 檔名:取代時使用 */
	private String fileName;

	/** 圖示內容=base64, 取代時使用 */
	private String iconFileContent;

	/** db中上傳圖檔icon:舊資料, fileName */
	private String orgIcon;

	public DPB0022Req() {}

	public Long getApiThemeId() {
		return apiThemeId;
	}

	public void setApiThemeId(Long apiThemeId) {
		this.apiThemeId = apiThemeId;
	}

	public void setApiThemeName(String apiThemeName) {
		this.apiThemeName = apiThemeName;
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

	public String getDataSort() {
		return dataSort;
	}

	public void setDataSort(String dataSort) {
		this.dataSort = dataSort;
	}

	public List<String> getUseApis() {
		return useApis;
	}

	public void setUseApis(List<String> useApis) {
		this.useApis = useApis;
	}

	public List<String> getOrgApiList() {
		return orgApiList;
	}

	public void setOrgApiList(List<String> orgApiList) {
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

	public String getOrgIcon() {
		return orgIcon;
	}

	public void setOrgIcon(String orgIcon) {
		this.orgIcon = orgIcon;
	}
	
}
