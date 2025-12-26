package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0065ApiApplication {

	/** 用戶ID,欲申請使用API的用戶 */
	private String clientId;

	/** 申請的API */
	private List<String> apiUids;

	/** 暫存檔名,申請附件 */
	private String tmpFileName;

	public DPB0065ApiApplication() {
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<String> getApiUids() {
		return apiUids;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

	public String getTmpFileName() {
		return tmpFileName;
	}

	public void setTmpFileName(String tmpFileName) {
		this.tmpFileName = tmpFileName;
	}

}
