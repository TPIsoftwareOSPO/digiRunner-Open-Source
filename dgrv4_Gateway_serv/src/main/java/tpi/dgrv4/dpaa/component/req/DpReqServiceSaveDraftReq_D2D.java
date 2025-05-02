package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceSaveDraftReq_D2D {

	private String apiUid;

	/** 主題ID */
	private Long refThemeId;

	public DpReqServiceSaveDraftReq_D2D() {
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public Long getRefThemeId() {
		return refThemeId;
	}

	public void setRefThemeId(Long refThemeId) {
		this.refThemeId = refThemeId;
	}

}
