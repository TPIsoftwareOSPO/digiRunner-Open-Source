package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class DPB0066RespApiOnOff {

	/**  */
	private Long reqOrdermId;

	/**  */
	private Long lv;

	/** 更新後的檔案名稱	Map<apiUid, fileName> */
	private Map<String, String> apiFileName;

	/**  */
	private List<Long> reqOrdersIds;

	/** Map<Long, List<reqOrderd2dId>> */
	private Map<Long, List<Long>> reqOrderd2Ids;

	public DPB0066RespApiOnOff() {}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public Map<String, String> getApiFileName() {
		return apiFileName;
	}

	public void setApiFileName(Map<String, String> apiFileName) {
		this.apiFileName = apiFileName;
	}

	public List<Long> getReqOrdersIds() {
		return reqOrdersIds;
	}

	public void setReqOrdersIds(List<Long> reqOrdersIds) {
		this.reqOrdersIds = reqOrdersIds;
	}

	public Map<Long, List<Long>> getReqOrderd2Ids() {
		return reqOrderd2Ids;
	}

	public void setReqOrderd2Ids(Map<Long, List<Long>> reqOrderd2Ids) {
		this.reqOrderd2Ids = reqOrderd2Ids;
	}

}
