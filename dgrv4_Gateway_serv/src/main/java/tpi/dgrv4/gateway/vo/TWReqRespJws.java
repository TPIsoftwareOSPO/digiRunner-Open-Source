package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 簽章後-API 訊息區塊 
 * HTTP Request/Response
 * 
 * @author Mini
 */
public class TWReqRespJws {
	
	/** 被簽章簽認的訊息,須採 BASE64URL 編碼 */
	@JsonProperty("payload")
	private String payload;

	/** 描述訊息簽章演算法資訊及訊息簽章 */
	@JsonProperty("signatures")
	private List<TWReqRespJwsSignatures> twReqRespJwsSignaturesList;

	@Override
	public String toString() {
		return "TWReqRespJws [payload=" + payload + ", twReqRespJwsSignaturesList=" + twReqRespJwsSignaturesList + "]";
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public List<TWReqRespJwsSignatures> getTwReqRespJwsSignaturesList() {
		return twReqRespJwsSignaturesList;
	}

	public void setTwReqRespJwsSignaturesList(List<TWReqRespJwsSignatures> twReqRespJwsSignaturesList) {
		this.twReqRespJwsSignaturesList = twReqRespJwsSignaturesList;
	}
}


