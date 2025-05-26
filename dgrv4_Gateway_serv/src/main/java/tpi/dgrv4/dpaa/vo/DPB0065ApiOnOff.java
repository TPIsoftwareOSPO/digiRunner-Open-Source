package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0065ApiOnOff {

	/** 選定的API+綁定的主題 */
	private Map<String, List<DPB0065ApiBindingData>> apiUidDatas;

	/** 暫存檔名:一支API對一個檔案, 不一定會有檔案, 取它的TSMP_DP_REQ_ORDERD2.id */
	/** Map<apiUid, tempFileName> */
	private Map<String, String> apiMapFileName;

	/** 開放狀態	publicFlag, 使用 BcryptParam 加密, ITEM_NO = 'API_AUTHORITY' */
	private String encPublicFlag;

	public DPB0065ApiOnOff() {}

	public Map<String, List<DPB0065ApiBindingData>> getApiUidDatas() {
		return apiUidDatas;
	}

	public void setApiUidDatas(Map<String, List<DPB0065ApiBindingData>> apiUidDatas) {
		this.apiUidDatas = apiUidDatas;
	}

	public Map<String, String> getApiMapFileName() {
		return apiMapFileName;
	}

	public void setApiMapFileName(Map<String, String> apiMapFileName) {
		this.apiMapFileName = apiMapFileName;
	}

	public String getEncPublicFlag() {
		return encPublicFlag;
	}

	public void setEncPublicFlag(String encPublicFlag) {
		this.encPublicFlag = encPublicFlag;
	}
	
}
