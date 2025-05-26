package tpi.dgrv4.dpaa.vo;

public class DPB0101Items {

	/** 大類別 */
	private String refItemNo;

	/** 子項目 */
	private String refSubitemNo;

	/** 參數值 */
	private String inParams;

	/** 識別資料 */
	private String identifData;

	/** 執行順序, 預設為0 */
	private Integer sortBy = 0;

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}

	public String getInParams() {
		return inParams;
	}
	
	public String getRefItemNo() {
		return refItemNo;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public String getIdentifData() {
		return identifData;
	}

	public Integer getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}
	
	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

}