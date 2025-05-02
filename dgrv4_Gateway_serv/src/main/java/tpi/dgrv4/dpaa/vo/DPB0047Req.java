package tpi.dgrv4.dpaa.vo;

public class DPB0047Req {
	
	/** PK	做為分頁使用, 必需是 List 回傳的最後一筆 */
	public Long itemId;
	
	/** 模糊搜尋	每一個字串可以使用"空白鍵" 隔開 */
	public String keyword;
	
	/** 編碼過的 itemNo	請參考"BcryptParam設計"	throw編碼不正確*/
	public String encodeItemNo;
	
	/** 
	 * 是否取預設值(Y/N)	值為Y時,  TSMP_DP_ITEMS.IS_DEFAULT 程式需要 ignore case (""v"") , 
	 * 此時與keyword無關, callThisMethod(itemNo="MEMBER_REG_FLAG", isDefault=Y)
	 * 例如上圖中只會取出 itemId = 1 那一筆資料的 subitemNo=DISABLE"
	 */
	public String isDefault;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getEncodeItemNo() {
		return encodeItemNo;
	}

	public void setEncodeItemNo(String encodeItemNo) {
		this.encodeItemNo = encodeItemNo;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	
}
