package tpi.dgrv4.dpaa.vo;

public class AA0302Controls {
	
	/** 是否可編輯API名稱 */
	private Boolean apiName = false;

	/** 是否可編輯協定 */
	private Boolean protocol = false;

	/** 是否可編輯來源URL */
	private Boolean srcUrl = false;
	
	/** 是否可編輯Http Method */
	private Boolean methodOfJson = false;
	
	/**是否可編輯資料格式  */
	private Boolean dataFormat = false;

	public Boolean getApiName() {
		return apiName;
	}

	public void setApiName(Boolean apiName) {
		this.apiName = apiName;
	}

	public Boolean getProtocol() {
		return protocol;
	}

	public void setProtocol(Boolean protocol) {
		this.protocol = protocol;
	}

	public Boolean getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(Boolean srcUrl) {
		this.srcUrl = srcUrl;
	}

	public Boolean getMethodOfJson() {
		return methodOfJson;
	}

	public void setMethodOfJson(Boolean methodOfJson) {
		this.methodOfJson = methodOfJson;
	}

	public Boolean getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(Boolean dataFormat) {
		this.dataFormat = dataFormat;
	}

	@Override
	public String toString() {
		return "AA0302Controls [apiName=" + apiName + ", protocol=" + protocol + ", srcUrl=" + srcUrl
				+ ", methodOfJson=" + methodOfJson + ", dataFormat=" + dataFormat + "]";
	}
	
	
}
