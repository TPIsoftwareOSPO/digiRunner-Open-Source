package tpi.dgrv4.dpaa.component.req;

import java.util.List;
import java.util.Map;

public class DpReqServiceSaveDraftReq_D2 extends DpReqServiceSaveDraftReq {

	/** 開放狀態, 應套用到 D2 中的每個 API */
	private String publicFlag;

	/** 選定的API+綁定的主題 */
	private Map<String, List<DpReqServiceSaveDraftReq_D2D>> apiUidDatas;

	/** 暫存檔名:一支API對一個檔案, 不一定會有檔案, 取它的TSMP_DP_REQ_ORDERD2.id */
	/** Map<apiUid, tempFileName> */
	private Map<String, String> apiMapFileName;

	public DpReqServiceSaveDraftReq_D2() {
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public Map<String, List<DpReqServiceSaveDraftReq_D2D>> getApiUidDatas() {
		return apiUidDatas;
	}

	public void setApiUidDatas(Map<String, List<DpReqServiceSaveDraftReq_D2D>> apiUidDatas) {
		this.apiUidDatas = apiUidDatas;
	}

	public Map<String, String> getApiMapFileName() {
		return apiMapFileName;
	}

	public void setApiMapFileName(Map<String, String> apiMapFileName) {
		this.apiMapFileName = apiMapFileName;
	}

}
