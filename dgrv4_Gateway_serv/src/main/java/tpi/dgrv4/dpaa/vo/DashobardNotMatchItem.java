package tpi.dgrv4.dpaa.vo;

public class DashobardNotMatchItem {
	public DashobardNotMatchItem() {
	}

	public DashobardNotMatchItem(String id, long createTimestamp) {
		this.id = id;
		this.createTimestamp = createTimestamp;
	}

	private Long createTimestamp;
	private String id;

	public Long getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Long createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
