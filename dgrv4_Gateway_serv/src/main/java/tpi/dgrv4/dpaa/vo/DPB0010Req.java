package tpi.dgrv4.dpaa.vo;

public class DPB0010Req {

	/** PK */
	private Long appCateId;

	/** 分類名稱 */
	private String appCateName;

	public DPB0010Req() {}

	public Long getAppCateId() {
		return appCateId;
	}

	public void setAppCateId(Long appCateId) {
		this.appCateId = appCateId;
	}

	public String getAppCateName() {
		return appCateName;
	}

	public void setAppCateName(String appCateName) {
		this.appCateName = appCateName;
	}
	
}
