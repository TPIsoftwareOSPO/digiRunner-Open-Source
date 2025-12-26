package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiRegId;

public class AA0317ReqItem {

	private String apiKey;

	private String moduleName;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public TsmpApiId toTsmpApiId() {
		return new TsmpApiId(this.apiKey, this.moduleName);
	}

	public TsmpApiRegId toTsmpApiRegId() {
		return new TsmpApiRegId(this.apiKey, this.moduleName);
	}

}