package tpi.dgrv4.dpaa.vo;

public class DPB0065ClientReg {

	/** 用戶ID */
	private String clientId;

	/** 用戶名稱 */
	private String clientName;

	/** 用戶電子郵件, 可設定多組email, 用逗號分隔 */
	private String emails;

	/** 密碼, 由 Javascript.Base64encode 後傳來 */
	private String clientBlock;

	/** 開放權限, 使用Bcrypt設計, itemNo = 'API_AUTHORITY' */
	private String encPublicFlag;

	/** 暫存檔名, 申請附件 */
	private String tmpFileName;

	public DPB0065ClientReg() {
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public void setClientBlock(String clientBlock) {
		this.clientBlock = clientBlock;
	}

	public String getClientBlock() {
		return clientBlock;
	}
	
	public String getEncPublicFlag() {
		return encPublicFlag;
	}

	public void setEncPublicFlag(String encPublicFlag) {
		this.encPublicFlag = encPublicFlag;
	}

	public String getTmpFileName() {
		return tmpFileName;
	}

	public void setTmpFileName(String tmpFileName) {
		this.tmpFileName = tmpFileName;
	}

}
