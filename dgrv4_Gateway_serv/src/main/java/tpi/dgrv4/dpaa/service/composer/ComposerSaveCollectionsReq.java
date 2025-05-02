package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerSaveCollectionsReq {

	/** 新增/更新, "C"=新增; "U"=更新 */
	private String act;

	/** collection名稱, ex: "applications" */
	private String collectionName;

	/** 
	 * 文件內容<br>
	 * ex: ["{ \"_id\" : { \"$oid\" : \"5c65225acccc70785ce0d552\"} , \"wires\" : [ ]}", "{ \"_id\" : { \"$oid\" : \"5c65225acccc70785ce0d552\"} , \"wires\" : [ ]}"]
	 */
	private List<String> documents;

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public List<String> getDocuments() {
		return documents;
	}

	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

}