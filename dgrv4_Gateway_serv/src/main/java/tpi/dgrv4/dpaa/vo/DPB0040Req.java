package tpi.dgrv4.dpaa.vo;

public class DPB0040Req {

	/** 模組名稱, 分頁使用 */
	private String moduleName;

	/** 模組版本, 分頁使用 */
	private String moduleVersion;

	private String keyword;

	public DPB0040Req() {}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
