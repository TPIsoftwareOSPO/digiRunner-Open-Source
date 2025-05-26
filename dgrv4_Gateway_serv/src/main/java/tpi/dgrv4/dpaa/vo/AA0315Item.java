package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0315Item {
	/** API名稱 */
	private String summary;

	/** 來源URL的最尾路徑 用來當作 AA0316Item 的 apiId */
	private String rearPath;

	/** 來源URL */
	private String path;

	/** 完整的來源URL protocol + host + basePath + path */
	private String srcUrl;

	/** API說明 */
	private String apiDesc;

	/** Http Method ex: ["POST", "GET"] */
	private List<String> methods;

	/** Http Header */
	private List<String> headers;

	/** Http Parameter */
	private List<String> params;

	/** Http Consumes Content-type */
	private List<String> consumes;

	/** Http Produces Response */
	private List<String> produces;

	private String moduleName;
	
	@Override
	public String toString() {
		return "AA0315Item [summary=" + summary + ", rearPath=" + rearPath + ", path=" + path + ", srcUrl=" + srcUrl
				+ ", apiDesc=" + apiDesc + ", methods=" + methods + ", headers=" + headers + ", params=" + params
				+ ", consumes=" + consumes + ", produces=" + produces + ", moduleName=" + moduleName + "]";
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getRearPath() {
		return rearPath;
	}

	public void setRearPath(String rearPath) {
		this.rearPath = rearPath;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
}
