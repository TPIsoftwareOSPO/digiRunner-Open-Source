package tpi.dgrv4.gateway.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthRevocationResp {
	/** 
	 * 自定義回應的 revoke success code，目前定義以下二種：
	 * token_revoke_success，指本次 token revoke 成功；
	 * token_already_revoked 指 token 已被廢止
	 */
	@JsonProperty("code")
	private String code;
	
	/** 對此 revoke success code 的進一步描述 */
	@JsonProperty("message")
	private String message;

	@Override
	public String toString() {
		return "OAuthRevocationResp [code=" + code + ", message=" + message + "]";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
