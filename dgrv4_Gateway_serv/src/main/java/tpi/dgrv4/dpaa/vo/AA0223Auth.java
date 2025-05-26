package tpi.dgrv4.dpaa.vo;

public class AA0223Auth {

	/** 核身型式名稱 */
	private String groupAuthoritieName;
	
	/** 核身型式代碼 */
	private String groupAuthoritieId;

	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}

	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}

	public void setGroupAuthoritieName(String groupAuthoritieName) {
		this.groupAuthoritieName = groupAuthoritieName;
	}
	
	public String getGroupAuthoritieName() {
		return groupAuthoritieName;
	}

	@Override
	public String toString() {
		return "AA0223Auth [groupAuthoritieId=" + groupAuthoritieId + ", groupAuthoritieName=" + groupAuthoritieName
				+ "]";
	}
	

}
