package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0095Req extends ReqValidator{
	
	/** PK 做為分頁使用, 必需是 List 回傳的最後一筆 */
	private String clientId;

	/**
	 * 模糊搜尋	以空格分隔, 搜尋欄位: 
	 * 用戶端帳號(TSMP_CLIENT.client_id),
	 * 用戶端代號(TSMP_CLIENT.client_name), 
	 * 用戶端名稱(TSMP_CLIENT.client_alias)
	 */
	private String keyword;

	/** 會員資格狀態 固定帶入"2"-放行 */
	private String regStatus;

	@Override
	public String toString() {
		return "DPB0095Req [clientId=" + clientId + ", keyword=" + keyword + ", regStatus=" + regStatus + "]";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("regStatus")
				.isRequired()
				.build()
		});
	}
}
