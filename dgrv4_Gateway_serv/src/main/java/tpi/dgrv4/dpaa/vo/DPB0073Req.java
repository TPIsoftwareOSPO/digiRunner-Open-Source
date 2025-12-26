package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0073Req {
	
	/** 
	 * 權限 "參考 TSMP_DP_ITEMS.ITEM_NO = 'API_AUTHORITY' (API露出權限),
	 * 需使用 BcryptParam 設計" 
	 */
	private String encodePublicFlag;
	
	/** API清單 */
	private List<DPB0073ApiList> apiPKs;

	public String getEncodePublicFlag() {
		return encodePublicFlag;
	}

	public void setEncodePublicFlag(String encodePublicFlag) {
		this.encodePublicFlag = encodePublicFlag;
	}

	public List<DPB0073ApiList> getApiPKs() {
		return apiPKs;
	}

	public void setApiPKs(List<DPB0073ApiList> apiPKs) {
		this.apiPKs = apiPKs;
	}
}
