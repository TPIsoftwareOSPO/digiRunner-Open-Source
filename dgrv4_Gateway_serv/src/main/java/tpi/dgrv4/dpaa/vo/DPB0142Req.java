package tpi.dgrv4.dpaa.vo;

public class DPB0142Req {

	/** 主類別, 由 authType 決定是否必填。authType = ""Composer"" 時，此欄位填入 [模組名稱] */
	private String resource;

	/** 次類別, 由 authType 決定是否必填。authType = ""Composer"" 時，此欄位填入 [API ID] */
	private String subclass;

	public DPB0142Req() {
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getSubclass() {
		return subclass;
	}

	public void setSubclass(String subclass) {
		this.subclass = subclass;
	}

}