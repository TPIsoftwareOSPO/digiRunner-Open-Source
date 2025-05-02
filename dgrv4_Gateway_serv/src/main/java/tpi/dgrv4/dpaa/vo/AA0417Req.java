package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0417Req {

	/** 節點名稱 分頁用, 當頁最後一筆資料的 節點名稱 */
	private String node;

	/** 關鍵字 以空格分隔, 搜尋欄位: TSMP_NODE.node */
	private String keyword;

	/** 欲排除的節點名稱 若有傳值, 則查詢時應排除含有這些 節點名稱 的資料 */
	private List<String> excludeNode;

	@Override
	public String toString() {
		return "AA0417Req [node=" + node + ", keyword=" + keyword + ", excludeNode=" + excludeNode + "]";
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getExcludeNode() {
		return excludeNode;
	}

	public void setExcludeNode(List<String> excludeNode) {
		this.excludeNode = excludeNode;
	}

}
