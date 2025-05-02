package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0126Req {
	
	/** 關鍵字搜尋*/
	private String keyword;

	/** 畫面已選擇的Index清單*/
	private List<String> roleIndexList;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getRoleIndexList() {
		return roleIndexList;
	}

	public void setRoleIndexList(List<String> roleIndexList) {
		this.roleIndexList = roleIndexList;
	}

	@Override
	public String toString() {
		return "EA0013Req [keyword=" + keyword + ", roleIndexList=" + roleIndexList + "]";
	}

}