package tpi.dgrv4.dpaa.vo;

public class DPB0076RespItem {
	
	/** ID(流水號) */
	private Long themeId;
	
	/** API主題名稱 */
	private String themeName;
	
	/** 組織單位ID */
	private String orgId;

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
}
