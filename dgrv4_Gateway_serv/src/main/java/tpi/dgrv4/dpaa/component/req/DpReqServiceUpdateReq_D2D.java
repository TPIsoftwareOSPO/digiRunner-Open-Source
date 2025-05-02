package tpi.dgrv4.dpaa.component.req;

public class DpReqServiceUpdateReq_D2D {

	private String apiUid;

	/** 主題ID */
	private Long refThemeId;

	public DpReqServiceUpdateReq_D2D() {
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
