package tpi.dgrv4.dpaa.vo;

public class DPB0102Req {

	/** 週期排程UID, PK, 分頁用 */
	private String apptRjobId;

	/** 狀態代碼 */
	private String status;

	/** 關鍵字, 以空白分隔 */
	private String keyword;

	public String getApptRjobId() {
		return apptRjobId;
	}

	public void setApptRjobId(String apptRjobId) {
		this.apptRjobId = apptRjobId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
