package tpi.dgrv4.dpaa.vo;

public class DPB0072Req {

	/** 做為分頁使用 */
	private String apiKey;

	/** 做為分頁使用 */
	private String moduleName;

	/** 模糊搜尋 */
	private String keyword;

	/** 開始日期	日期時間格式: YYYY/MM/DD */
	private String startDate;

	/** 結束日期	日期時間格式: YYYY/MM/DD */
	private String endDate;
	
	/** 組織原則	ex:0 / 1 , 使用BcryptParam設計, itemNo="ORG_FLAG", default 為0 */
	private String orgFlagEncode;

	public DPB0072Req() {}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public void setOrgFlagEncode(String orgFlagEncode) {
		this.orgFlagEncode = orgFlagEncode;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

	public String getOrgFlagEncode() {
		return orgFlagEncode;
	}

	
}
