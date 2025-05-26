package tpi.dgrv4.dpaa.vo;

public class DPB0208Resp {
	/** X-Api-Key 經過遮罩的值 */
	private String apiKeyMask;

	@Override
	public String toString() {
		return "DPB0208Resp [apiKeyMask=" + apiKeyMask + "]";
	}

	public String getApiKeyMask() {
		return apiKeyMask;
	}

	public void setApiKeyMask(String apiKeyMask) {
		this.apiKeyMask = apiKeyMask;
	}
}
