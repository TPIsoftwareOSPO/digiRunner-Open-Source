package tpi.dgrv4.dpaa.vo;

public class DPB0006Req {

	/** PK=做為分頁使用 */
	private String clientId;

	/** 模糊搜尋=每一個字串可以使用"空白鍵"隔開	*/
	private String keyword;

	/** 狀態=2：放行，3：退回，空值：全部 */
	private String regStatus;

	/** 起日=日期時間格式:YYYY/MM/DD */
	private String startDate;

	/** 迄日=日期時間格式:YYYY/MM/DD */
	private String endDate;

	public DPB0006Req() {}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public String getKeyword() {
		return keyword;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
}
