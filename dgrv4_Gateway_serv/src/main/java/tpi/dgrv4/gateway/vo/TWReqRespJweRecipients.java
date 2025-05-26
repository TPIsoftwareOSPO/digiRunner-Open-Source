package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TWReqRespJweRecipients {
	
	/** JWE 支援【保護本文加密金鑰】之金鑰演算法 */
	@JsonProperty("header")
	private Object header;

	/** 【本文加密金鑰】，須採 BASE64URL 編碼 */
	@JsonProperty("encrypted_key")
	private String encryptedKey;

	@Override
	public String toString() {
		return "TWReqRespJweRecipients [header=" + header + ", encryptedKey=" + encryptedKey + "]";
	}

	public Object getHeader() {
		return header;
	}

	public void setHeader(Object header) {
		this.header = header;
	}

	public String getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}
}
