package tpi.dgrv4.dpaa.vo;

public class DPB0105Items {

	/** 排程工作ID */
	private Long apptRjobDId;

	/** 大類別 */
	private String refItemNo;

	/** 子項目 */
	private String refSubitemNo;

	/** 參數值 */
	private String inParams;

	/** 識別資料 */
	private String identifData;

	/** 排程工作版號 */
	private Long lv;
	
	/** 執行順序 */
	private Integer sortBy;
	
	public void setApptRjobDId(Long apptRjobDId) {
		this.apptRjobDId = apptRjobDId;
	}

	public Long getApptRjobDId() {
		return apptRjobDId;
	}
	
	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getRefItemNo() {
		return refItemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}
	
	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}

	public String getRefSubitemNo() {
		return refSubitemNo;
	}	

	public String getInParams() {
		return inParams;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}
	
	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

	public Integer getSortBy() {
		return sortBy;
	}
	
	public Long getLv() {
		return lv;
	}

}
