package tpi.dgrv4.dpaa.vo;

public class AA0411Resp {

	/** 部署容器編號 */
	private Long dcId;

	@Override
	public String toString() {
		return "AA0411Resp [dcId=" + dcId + "]";
	}

	public Long getDcId() {
		return dcId;
	}

	public void setDcId(Long dcId) {
		this.dcId = dcId;
	}

}
