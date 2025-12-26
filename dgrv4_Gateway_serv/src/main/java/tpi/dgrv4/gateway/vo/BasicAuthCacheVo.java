package tpi.dgrv4.gateway.vo;

public class BasicAuthCacheVo {
	private Long dataTimestamp;// Cache 建立時間
	private String hash;// DB 中密碼的 BCrypt 值
	
	@Override
	public String toString() {
		return "BasicAuthCacheVo [dataTimestamp=" + dataTimestamp + ", hash=" + hash + "]";
	}

	public Long getDataTimestamp() {
		return dataTimestamp;
	}

	public void setDataTimestamp(Long dataTimestamp) {
		this.dataTimestamp = dataTimestamp;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
