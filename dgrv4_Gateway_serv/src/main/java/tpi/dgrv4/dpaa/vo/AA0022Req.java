package tpi.dgrv4.dpaa.vo;
	
public class AA0022Req {

	/** PK 做為分頁使用, 必需是 List 回傳的最後一筆*/
	private String roleId;
	
	/** 模糊搜尋 每一個字串可以使用"空白鍵" 隔開*/
	private String keyword;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	
}
