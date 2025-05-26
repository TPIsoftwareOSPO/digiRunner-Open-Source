package tpi.dgrv4.dpaa.vo;

public class DPB0103Items {

	/** 排程工作ID */
	private Long apptRjobDId;

	/** 排程工作版號 */
	private Long lv;

	/** 大類別 */
	private String refItemNo;

	/** 大類別名稱 */
	private String refItemName;

	/** 子項目 */
	private String refSubitemNo;

	/** 子項目名稱 */
	private String refSubitemName;

	/** 參數值 */
	private String inParams;

	/** 識別資料 */
	private String identifData;

	/** 執行順序 */
	private Integer sortBy;

	public Long getApptRjobDId() {
		return apptRjobDId;
	}

	public void setApptRjobDId(Long apptRjobDId) {
		this.apptRjobDId = apptRjobDId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}
	
	public String getRefItemNo() {
		return refItemNo;
	}

	public String getRefItemName() {
		return refItemName;
	}

	public void setRefItemName(String refItemName) {
		this.refItemName = refItemName;
	}
	
	public String getRefSubitemName() {
		return refSubitemName;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}
	
	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public void setRefSubitemName(String refSubitemName) {
		this.refSubitemName = refSubitemName;
	}

	public String getInParams() {
		return inParams;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
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

}
