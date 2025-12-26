package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0085Req {

	private String clientId;

	/** 若沒有資料可以不用提供 */
	private Map<Long, Long> lockVersions;

	/** 將檔案轉 base64上傳 */
	private String fileContent;
	
	/** 檔案名稱 */
	private String fileName;
	
	/** 憑證類型	使用BcryptParam,ITEM_NO='CERT_TYPE',JWE 使用 TSMP_CLIENT_CERT,TLS 使用 TSMP_CLIENT_CERT2 */
	private String encodeCertType;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Map<Long, Long> getLockVersions() {
		return lockVersions;
	}

	public void setLockVersions(Map<Long, Long> lockVersions) {
		this.lockVersions = lockVersions;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getEncodeCertType() {
		return encodeCertType;
	}

	public void setEncodeCertType(String encodeCertType) {
		this.encodeCertType = encodeCertType;
	}
}
