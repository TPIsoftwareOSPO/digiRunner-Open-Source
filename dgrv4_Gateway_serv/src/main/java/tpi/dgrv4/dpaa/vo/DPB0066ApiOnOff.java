package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0066ApiOnOff {

	/** 選定的API+綁定的主題 */
	private Map<String, List<DPB0066ApiBindingData>> apiUidDatas;
	
	/** Query時原有舊資料, Map<apiUid, fileName> */
	private Map<String, String> oriApiMapFileName;

	/** 更新時傳送的資料, 一支API對一個檔案, 不一定會有檔案, 取它的TSMP_DP_REQ_ORDERD2.id */
	private Map<String, String> newApiMapFileName;

	/** 開放狀態	publicFlag, 使用 BcryptParam 加密, ITEM_NO = 'API_AUTHORITY' */
	private String encPublicFlag;

	public DPB0066ApiOnOff() {}

	public Map<String, List<DPB0066ApiBindingData>> getApiUidDatas() {
		return apiUidDatas;
	}

	public void setApiUidDatas(Map<String, List<DPB0066ApiBindingData>> apiUidDatas) {
		this.apiUidDatas = apiUidDatas;
	}

	public Map<String, String> getOriApiMapFileName() {
		return oriApiMapFileName;
	}

	public void setOriApiMapFileName(Map<String, String> oriApiMapFileName) {
		this.oriApiMapFileName = oriApiMapFileName;
	}

	public Map<String, String> getNewApiMapFileName() {
		return newApiMapFileName;
	}

	public void setNewApiMapFileName(Map<String, String> newApiMapFileName) {
		this.newApiMapFileName = newApiMapFileName;
	}

	public String getEncPublicFlag() {
		return encPublicFlag;
	}

	public void setEncPublicFlag(String encPublicFlag) {
		this.encPublicFlag = encPublicFlag;
	}
	
}
