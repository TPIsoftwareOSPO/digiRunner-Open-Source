package tpi.dgrv4.dpaa.vo;

public class AA0317RegModule {

	/** ID(流水號), TSMP_REG_MODULE.reg_module_id */
	private Long regModuleId;

	/** 模組名稱, TSMP_REG_MODULE.module_name */
	private String moduleName;

	/** 模組版本, TSMP_REG_MODULE.module_version */
	private String moduleVersion;

	/** 建立來源, TSMP_REG_MODULE.module_src */
	private String moduleSrc;

	public Long getRegModuleId() {
		return regModuleId;
	}

	public void setRegModuleId(Long regModuleId) {
		this.regModuleId = regModuleId;
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

	public String getModuleSrc() {
		return moduleSrc;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleSrc(String moduleSrc) {
		this.moduleSrc = moduleSrc;
	}

}