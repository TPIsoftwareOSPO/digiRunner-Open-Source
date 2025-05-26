package tpi.dgrv4.dpaa.vo;

public class DPB0041Req {

	/** 存檔清單	每個 moduleName 以 "," 區分 */
	private String moduleNames;

	public DPB0041Req() {}

	public String getModuleNames() {
		return moduleNames;
	}

	public void setModuleNames(String moduleNames) {
		this.moduleNames = moduleNames;
	}
	
}
