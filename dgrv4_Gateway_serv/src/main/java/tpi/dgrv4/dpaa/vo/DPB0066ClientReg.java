package tpi.dgrv4.dpaa.vo;

public class DPB0066ClientReg {

	/** 新的用戶ID */
	private String clientId;

	/** 新的用戶名稱 */
	private String clientName;

	/** 新的用戶電子郵件 可設定多組email，用逗號分隔 */
	private String emails;

	/** 新的密碼 由 Javascript.Base64encode 後傳來 */
	private String clientBlock;

	/** 開放權限 使用Bcrypt設計, itemNo = 'API_AUTHORITY' */
	private String encPublicFlag;

	/**
	 * <b>新的檔名</b>: 更新後的申請附件檔名。<br>
	 * 若有更新檔案, 則應帶入暫存檔名(ex: 123.wait.測試.txt);<br>
	 * 若檔案未異動, 則應帶入原始檔名(與oriFileName同值);<br>
	 * 未帶值表示要刪除檔案
	 */
	private String newFileName;

	/** 既有檔名 若原申請單已有附件, 則此欄應帶入附件檔名 */
	private String oriFileName;

	public DPB0066ClientReg() {
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getEmails() {
		return emails;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getClientBlock() {
		return clientBlock;
	}
	
	public String getEncPublicFlag() {
		return encPublicFlag;
	}

	public void setClientBlock(String clientBlock) {
		this.clientBlock = clientBlock;
	}

	public void setEncPublicFlag(String encPublicFlag) {
		this.encPublicFlag = encPublicFlag;
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

}
