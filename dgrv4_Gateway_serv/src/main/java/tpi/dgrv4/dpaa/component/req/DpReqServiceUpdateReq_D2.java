package tpi.dgrv4.dpaa.component.req;

import java.util.List;
import java.util.Map;

public class DpReqServiceUpdateReq_D2 extends DpReqServiceUpdateReq {

	/** 開放狀態, 應套用到 D2 中的每個 API */
	private String publicFlag;

	/** 選定的API+綁定的主題 */
	private Map<String, List<DpReqServiceUpdateReq_D2D>> apiUidDatas;

	/** 一支API 對一個檔案, 不一定會有檔案, 取它的TSMP_DP_REQ_ORDERD2.id */

	/** Map<apiUid, fileName>, Query時原有舊資料 */
	private Map<String, String> oriApiMapFileName;

	/** Map<apiUid, (temp)fileName>, 更新時傳送的資料 */
	private Map<String, String> newApiMapFileName;

	public DpReqServiceUpdateReq_D2() {
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public Map<String, List<DpReqServiceUpdateReq_D2D>> getApiUidDatas() {
		return apiUidDatas;
	}

	public void setApiUidDatas(Map<String, List<DpReqServiceUpdateReq_D2D>> apiUidDatas) {
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

}
