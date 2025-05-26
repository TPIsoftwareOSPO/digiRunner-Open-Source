package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0066ApiApplication {

	/**
	 * <b>新的檔名</b>: 更新後的申請附件檔名。<br>
	 * 若有更新檔案, 則應帶入暫存檔名(ex: 123.wait.測試.txt);<br>
	 * 若檔案未異動, 則應帶入原始檔名(與oriFileName同值);<br>
	 * 未帶值表示要刪除檔案
	 */
	private String newFileName;

	/** 既有檔名 若原申請單已有附件, 則此欄應帶入附件檔名 */
	private String oriFileName;

	/** 新的API清單 */
	private List<String> apiUids;

	/** 新的用戶ID 欲申請使用API的用戶 */
	private String clientId;

	public DPB0066ApiApplication() {
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

	public String getOriFileName() {
		return oriFileName;
	}

	public void setOriFileName(String oriFileName) {
		this.oriFileName = oriFileName;
	}

	public List<String> getApiUids() {
		return apiUids;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}
