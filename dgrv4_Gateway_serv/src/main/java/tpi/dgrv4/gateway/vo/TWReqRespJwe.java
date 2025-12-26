package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 加密後-API 訊息區塊（jwe-enveloped）
 * HTTP Request/Response
 * 
 * @author Mini
 */
public class TWReqRespJwe {
	
	//checkmarx,Excessive Data Exposure,所以更改命名
	/** 描述本文加密演算法資訊  須採 BASE64URL 編碼 */
	@JsonProperty("protected")
	private String encProtectedData;
	
	/** 描述本文加密金鑰資訊  本階段不使用，此欄位值放空字串"" */
	@JsonProperty("unprotected")
	private String unprotected = "";

	//checkmarx,Excessive Data Exposure,所以更改命名
	/** 描述【保護本文加密金鑰】之演算法及【本文加密金鑰】 */
	@JsonProperty("recipients")
	private List<TWReqRespJweRecipients> encTwReqRespJweRecipientsList;
	
	//checkmarx,Excessive Data Exposure,所以更改命名
	/** Initialization Vector  加密本文所採用演算法所需之初始參數，通常為亂數,須採 BASE64URL 編碼 */
	@JsonProperty("iv")
	private String encIv;
	
	//checkmarx,Excessive Data Exposure,所以更改命名
	/** 加密密文  須採 BASE64URL 編碼 */
	@JsonProperty("ciphertext")
	private String encCipherText;
	
	//checkmarx,Excessive Data Exposure,所以更改命名
	/** JWE Authentication Tag  須採 BASE64URL 編碼 */
	@JsonProperty("tag")
	private String encTag;


	@Override
	public String toString() {
		return "TWReqRespJwe [encProtectedData=" + encProtectedData + ", unprotected=" + unprotected
				+ ", encTwReqRespJweRecipientsList=" + encTwReqRespJweRecipientsList + ", encIv=" + encIv
				+ ", encCipherText=" + encCipherText + ", encTag=" + encTag + "]";
	}

	public String getEncProtectedData() {
		return encProtectedData;
	}

	public void setEncProtectedData(String encProtectedData) {
		this.encProtectedData = encProtectedData;
	}

	public List<TWReqRespJweRecipients> getEncTwReqRespJweRecipientsList() {
		return encTwReqRespJweRecipientsList;
	}

	public void setEncTwReqRespJweRecipientsList(List<TWReqRespJweRecipients> encTwReqRespJweRecipientsList) {
		this.encTwReqRespJweRecipientsList = encTwReqRespJweRecipientsList;
	}

	public String getUnprotected() {
		return unprotected;
	}

	public void setUnprotected(String unprotected) {
		this.unprotected = unprotected;
	}

	
	public String getEncIv() {
		return encIv;
	}

	public void setEncIv(String encIv) {
		this.encIv = encIv;
	}

	public String getEncCipherText() {
		return encCipherText;
	}

	public void setEncCipherText(String encCipherText) {
		this.encCipherText = encCipherText;
	}

	public String getEncTag() {
		return encTag;
	}

	public void setEncTag(String encTag) {
		this.encTag = encTag;
	}

	
}
