package tpi.dgrv4.dpaa.vo;

public class AA1116Req {

	/** 安全等級ID 分頁用, Pk */
	private String securityLevelId;

	/** 關鍵字 以空格分隔, 搜尋欄位: 安全等級ID、安全等級名稱、安全等級描述 */
	private String keyword;

	@Override
	public String toString() {
		return "AA1116Req [securityLevelId=" + securityLevelId + ", keyword=" + keyword + "]";
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
