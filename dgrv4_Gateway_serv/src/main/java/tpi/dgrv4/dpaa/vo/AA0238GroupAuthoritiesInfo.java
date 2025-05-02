package tpi.dgrv4.dpaa.vo;

public class AA0238GroupAuthoritiesInfo {

	/** 截斷授權核身名稱*/
	private String groupAuthoritieName;
	
	/** 授權核身名稱*/
	private String oriGroupAuthoritieName;
	
	/** 是否被截斷*/
	private boolean isTruncated;

	public String getGroupAuthoritieName() {
		return groupAuthoritieName;
	}

	public void setGroupAuthoritieName(String groupAuthoritieName) {
		this.groupAuthoritieName = groupAuthoritieName;
	}

	public String getOriGroupAuthoritieName() {
		return oriGroupAuthoritieName;
	}

	public void setOriGroupAuthoritieName(String oriGroupAuthoritieName) {
		this.oriGroupAuthoritieName = oriGroupAuthoritieName;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}
	
	
}
