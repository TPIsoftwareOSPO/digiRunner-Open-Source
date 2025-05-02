package tpi.dgrv4.dpaa.vo;

public class DPB0062Req {

	/** 大項目:
	 * 從
	 * where TSMP_DP_ITEMS.ITEM_ITEM_NO= 'SCHED_CATE1' 
	 * 中的選單挑一個 SUBITEM_NO，以 LOV 型式供 User 選擇
	 * */
	private String refItemNo;

	/** 小項目:
	 * 依上項條件 TSMP_DP_ITEMS.ITEM_SUBITEM_NO + PARAM1 欄位值決定是否還有值。<br>
	 * Y: 表示還有子分類<br>
	 * N: 表示沒有子分類<br>
	 * 若是 Y 則是以它的值再次 where TSMP_DP_ITEMS.ITEM_ITEM_NO= ?
	 * 中的選單挑一個 SUBITEM_NO
	 * */
	private String refSubitemNo;

	/** input 參數	Caller 決定, 以 base64 編碼後才寫入。 */
	private String inParams;

	/** 開始時間	YYYY/MM/DD hh:mm		TSMP_DP_APPT_JOB */
	private String startDateTime;

	/** 識別資料	以 Base64 編碼後傳入後端 */
	private String identifData;

	public DPB0062Req() {}

	public String getRefItemNo() {
		return refItemNo;
	}

	public void setRefItemNo(String refItemNo) {
		this.refItemNo = refItemNo;
	}

	public void setRefSubitemNo(String refSubitemNo) {
		this.refSubitemNo = refSubitemNo;
	}
	
	public String getRefSubitemNo() {
		return refSubitemNo;
	}

	public String getInParams() {
		return inParams;
	}

	public void setInParams(String inParams) {
		this.inParams = inParams;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
	}

}
