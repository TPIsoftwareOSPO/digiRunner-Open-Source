package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0208Req extends ReqValidator {
	/** digiRunner 的 client_id */
	private String clientId;

	/** X-Api-Key 別名 */
	private String apiKeyAlias;

	/** 生效日期,前端將 YYYY/MM/DD 轉成 long 格式的字串 */
	private String effectiveAt;

	/** 到期日期,前端將 YYYY/MM/DD 轉成 long 格式的字串 */
	private String expiredAt;

	/** 選擇的 Group Id */
	private List<String> groupIdList;
 
	@Override
	public String toString() {
		return "DPB0208Req [clientId=" + clientId + ", apiKeyAlias=" + apiKeyAlias + ", effectiveAt=" + effectiveAt
				+ ", expiredAt=" + expiredAt + ", groupIdList=" + groupIdList + "]";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getApiKeyAlias() {
		return apiKeyAlias;
	}

	public void setApiKeyAlias(String apiKeyAlias) {
		this.apiKeyAlias = apiKeyAlias;
	}

	public String getEffectiveAt() {
		return effectiveAt;
	}

	public void setEffectiveAt(String effectiveAt) {
		this.effectiveAt = effectiveAt;
	}

	public String getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(String expiredAt) {
		this.expiredAt = expiredAt;
	}

	public List<String> getGroupIdList() {
		return groupIdList;
	}

	public void setGroupIdList(List<String> groupIdList) {
		this.groupIdList = groupIdList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("clientId")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("apiKeyAlias")
					.isRequired()
					.maxLength(100)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector() //
					.buildString(locale)
					.field("expiredAt")
					.isRequired()
					.build()
				,		
			});
		}
}
