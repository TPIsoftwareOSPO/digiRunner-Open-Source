package tpi.dgrv4.dpaa.component.req;

import java.util.List;

/**
 * 屬性名稱開頭刻意加一個'_'是為了與父類別屬性區隔
 * 
 * @author Mini
 *
 */
public class DpReqServiceSaveDraftReq_D5 extends DpReqServiceSaveDraftReq {

	/** 用戶ID 欲申請使用Open API Key的用戶 */
	private String _clientId;

	/** Open API Key ID */
	private Long openApiKeyId;

	/** Open API Key */
	private String openApiKey;

	/** Secret Key */
	private String secretKey;

	/** Open API Key 別名 */
	private String openApiKeyAlias;

	/** 使用次數上限 */
	private Integer timesThreshold;

	/** Open API Key 效期 */
	private String expiredAt;

	/** 申請的 API UUID */
	private List<String> apiUids;

	public String get_clientId() {
		return _clientId;
	}

	public void set_clientId(String _clientId) {
		this._clientId = _clientId;
	}

	public Long getOpenApiKeyId() {
		return openApiKeyId;
	}

	public void setOpenApiKeyId(Long openApiKeyId) {
		this.openApiKeyId = openApiKeyId;
	}

	public String getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(String openApiKey) {
		this.openApiKey = openApiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getOpenApiKeyAlias() {
		return openApiKeyAlias;
	}

	public void setOpenApiKeyAlias(String openApiKeyAlias) {
		this.openApiKeyAlias = openApiKeyAlias;
	}
 
	public Integer getTimesThreshold() {
		return timesThreshold;
	}

	public void setTimesThreshold(Integer timesThreshold) {
		this.timesThreshold = timesThreshold;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public List<String> getApiUids() {
		return apiUids;
	}

	public void setApiUids(List<String> apiUids) {
		this.apiUids = apiUids;
	}

}
