package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class DgrWebsiteDetailVo {

	private Long dgrWebsiteDetailId;
	private Long dgrWebsiteId;
	private Integer probability;
	private String url;

	@Override
	public String toString() {
		return "DgrWebsiteDetail [dgrWebsiteDetailId=" + dgrWebsiteDetailId + ", dgrWebsiteId=" + dgrWebsiteId + ", probability= " + probability +", url=" + url + "]\n";
	}

	public Long getDgrWebsiteDetailId() {
		return dgrWebsiteDetailId;
	}

	public void setDgrWebsiteDetailId(Long dgrWebsiteDetailId) {
		this.dgrWebsiteDetailId = dgrWebsiteDetailId;
	}

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	public Integer getProbability() {
		return probability;
	}

	public void setProbability(Integer probability) {
		this.probability = probability;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}
