package tpi.dgrv4.dpaa.vo;

public class DPB0040TsmpModule {
	
	/** 模組名稱 */
	private String moduleName;
	/** 模組版本 */
	private String moduleVersion;
	/** 開放=0/拒絕=1 */
	private String deniedFlag;
	
	private long id;
	
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
	public String getDeniedFlag() {
		return deniedFlag;
	}
	public void setDeniedFlag(String deniedFlag) {
		this.deniedFlag = deniedFlag;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	
	
}
