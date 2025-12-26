package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0315Resp {

	/** 協定方式 ex: "http://", "https://" */
	private String protocol;

	/** Host IP:Port */
	private String host;

	/** 來源URL的基本路徑 */
	private String basePath;
	
	/**
	 * 模式
	 */
	private Integer type;

	/**
	 * 模組建立來源 TSMP_REG_MODULE.module_src, 依據上傳的檔案內容及格式，判斷：1=WSDL, 2=OAS2.0, 3=OAS3.0
	 */
	private String moduleSrc;

	/** 模組名稱 */
	private String moduleName;

	/** 模組版本 */
	private String moduleVersion;

	/** API清單 */
	private List<AA0315Item> openApiList;
 
	@Override
	public String toString() {
		return "AA0315Resp [protocol=" + protocol + ", host=" + host + ", basePath=" + basePath + ", moduleSrc="
				+ moduleSrc + ", moduleName=" + moduleName + ", moduleVersion=" + moduleVersion + ", openApiList="
				+ openApiList + "]";
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getModuleSrc() {
		return moduleSrc;
	}

	public void setModuleSrc(String moduleSrc) {
		this.moduleSrc = moduleSrc;
	}

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

	public List<AA0315Item> getOpenApiList() {
		return openApiList;
	}

	public void setOpenApiList(List<AA0315Item> openApiList) {
		this.openApiList = openApiList;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
}
