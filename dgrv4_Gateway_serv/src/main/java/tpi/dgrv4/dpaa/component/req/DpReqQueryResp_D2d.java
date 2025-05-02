package tpi.dgrv4.dpaa.component.req;

public class DpReqQueryResp_D2d {

	/** from tsmp_dp_req_orderd2d.req_orderd2d_id */
	private Long reqOrderd2dId;

	/** from tsmp_dp_req_orderd2d.ref_theme_id */
	private Long refThemeId;

	/** from tsmp_dp_theme_category.api_theme_name */
	private String apiThemeName;

	public DpReqQueryResp_D2d() {
	}

	@Override
	public String toString() {
		return "\n\t\tDpReqQueryD2dResp [\n\t\t\treqOrderd2dId=" + reqOrderd2dId + ",\n\t\t\trefThemeId=" + refThemeId + ",\n\t\t\tapiThemeName="
				+ apiThemeName + "\n\t\t]";
	}

	public Long getReqOrderd2dId() {
		return reqOrderd2dId;
	}

	public void setReqOrderd2dId(Long reqOrderd2dId) {
		this.reqOrderd2dId = reqOrderd2dId;
	}

	public Long getRefThemeId() {
		return refThemeId;
	}

	public void setRefThemeId(Long refThemeId) {
		this.refThemeId = refThemeId;
	}

	public String getApiThemeName() {
		return apiThemeName;
	}

	public void setApiThemeName(String apiThemeName) {
		this.apiThemeName = apiThemeName;
	}

}
