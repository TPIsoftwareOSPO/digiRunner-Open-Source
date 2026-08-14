package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

/**
 * DPB0341：測試後端代取 token 的結果（診斷用，含完整 token，前端不應保存）。
 */
public class DPB0341Resp {

	private boolean success;          // 代取是否成功
	private String message;           // 成功 "OK"，失敗則為原因
	private String tokenEndpoint;     // 實際使用的 token endpoint（discovery 解析後）
	private boolean tokenObtained;    // 是否取得 token
	private String token;             // 代取取得的完整 token（診斷用）
	private Long expiresInSec;        // token 有效秒數

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}

	public boolean isTokenObtained() {
		return tokenObtained;
	}

	public void setTokenObtained(boolean tokenObtained) {
		this.tokenObtained = tokenObtained;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getExpiresInSec() {
		return expiresInSec;
	}

	public void setExpiresInSec(Long expiresInSec) {
		this.expiresInSec = expiresInSec;
	}

	@Override
	public String toString() {
		return "DPB0341Resp [success=" + success + ", message=" + message + ", tokenEndpoint=" + tokenEndpoint
				+ ", tokenObtained=" + tokenObtained + ", token=" + token
				+ ", expiresInSec=" + expiresInSec + "]";
	}
}
