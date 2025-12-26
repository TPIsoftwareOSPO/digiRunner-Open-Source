package tpi.dgrv4.dpaa.vo;

public class DPB0068ClientReg {

	/**  */
	private Long reqOrderd3Id;

	/** 用戶端帳號 */
	private String clientId;

	/** 用戶端代號 */
	private String clientName;

	/** 用戶郵件 */
	private String emails;

	/** 公開/私有-代碼 */
	private String publicFlag;

	/** 公開/私有-名稱 */
	private String publicFlagName;

	/** 申請文件檔名 */
	private String fileName;

	/** 申請文件檔名(含路徑) */
	private String filePath;

	public DPB0068ClientReg() {
	}

	public Long getReqOrderd3Id() {
		return reqOrderd3Id;
	}

	public void setReqOrderd3Id(Long reqOrderd3Id) {
		this.reqOrderd3Id = reqOrderd3Id;
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

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
