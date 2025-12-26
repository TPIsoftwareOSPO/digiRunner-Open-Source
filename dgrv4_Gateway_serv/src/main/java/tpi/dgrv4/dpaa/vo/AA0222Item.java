package tpi.dgrv4.dpaa.vo;

public class AA0222Item {
	
	/** 虛擬群組ID */
	private String vgroupId;
	
	/** 虛擬群組代碼 */
	private String vgroupName;
	
	/** 虛擬群組名稱 */
	private String vgroupAlias;
	
	/** 是否被截斷*/
	private Boolean isDescTruncated;
	
	/** 被截斷後的虛擬群組描述 */
	private String vgroupDesc;
	
	/** 完整的虛擬群組描述 */
	private String oriVgroupDesc;
	
	/** 是否被截斷*/
	private Boolean isTruncated;
	
	/** 被截斷後的核身授權種類 */
	private String vgroupAuthorities;

	/** 完整的核身授權種類 */
	private String oriVgroupAuthorities;

	/** 安全等級名稱 */
	private String securityLevelName;
	
	/** 建立時間 */
	private String createTime;

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}
	
	public String getVgroupName() {
		return vgroupName;
	}

	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}

	public Boolean getIsDescTruncated() {
		return isDescTruncated;
	}

	public void setIsDescTruncated(Boolean isDescTruncated) {
		this.isDescTruncated = isDescTruncated;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
	}

	public String getOriVgroupDesc() {
		return oriVgroupDesc;
	}

	public void setOriVgroupDesc(String oriVgroupDesc) {
		this.oriVgroupDesc = oriVgroupDesc;
	}

	public Boolean getIsTruncated() {
		return isTruncated;
	}

	public void setIsTruncated(Boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public String getVgroupAuthorities() {
		return vgroupAuthorities;
	}

	public void setVgroupAuthorities(String vgroupAuthorities) {
		this.vgroupAuthorities = vgroupAuthorities;
	}

	public String getOriVgroupAuthorities() {
		return oriVgroupAuthorities;
	}

	public void setOriVgroupAuthorities(String oriVgroupAuthorities) {
		this.oriVgroupAuthorities = oriVgroupAuthorities;
	}

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "AA0222Item [vgroupId=" + vgroupId + ", vgroupName=" + vgroupName + ", vgroupAlias=" + vgroupAlias
				+ ", isDescTruncated=" + isDescTruncated + ", vgroupDesc=" + vgroupDesc + ", oriVgroupDesc="
				+ oriVgroupDesc + ", isTruncated=" + isTruncated + ", vgroupAuthorities=" + vgroupAuthorities
				+ ", oriVgroupAuthorities=" + oriVgroupAuthorities + ", securityLevelName=" + securityLevelName
				+ ", createTime=" + createTime + "]";
	}
	
	
}
