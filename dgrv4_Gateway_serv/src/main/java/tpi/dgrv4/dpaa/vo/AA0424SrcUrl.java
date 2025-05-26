package tpi.dgrv4.dpaa.vo;

public class AA0424SrcUrl {
	private Integer percentage;
	private String srcUrl;

	public AA0424SrcUrl() {

	}

	public AA0424SrcUrl(AA0424SrcUrl o) {
		this.percentage = o.getPercentage();
		this.srcUrl = o.getSrcUrl();
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

}
