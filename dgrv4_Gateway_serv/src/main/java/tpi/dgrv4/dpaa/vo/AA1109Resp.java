package tpi.dgrv4.dpaa.vo;

public class AA1109Resp {
	
	/** 核身型式名稱 */
	private String groupAuthoritieName;
	
	/** 核身型式描述 */
	private String groupAuthoritieDesc;
	
	/** 核身型式代碼 */
	private String groupAuthoritieId;
	
	/** 核身型式等級 */
	private String groupAuthoritieLevel;

	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}

	public String getGroupAuthoritieName() {
		return groupAuthoritieName;
	}

	public void setGroupAuthoritieName(String groupAuthoritieName) {
		this.groupAuthoritieName = groupAuthoritieName;
	}

	public String getGroupAuthoritieDesc() {
		return groupAuthoritieDesc;
	}

	public void setGroupAuthoritieDesc(String groupAuthoritieDesc) {
		this.groupAuthoritieDesc = groupAuthoritieDesc;
	}

	public String getGroupAuthoritieLevel() {
		return groupAuthoritieLevel;
	}

	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}
	
	public void setGroupAuthoritieLevel(String groupAuthoritieLevel) {
		this.groupAuthoritieLevel = groupAuthoritieLevel;
	}

	
}
