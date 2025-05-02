package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0222Req {
	
	/** 虛擬群組ID*/
	private String vgroupId;

	private String keyword;

	/** 授權核身種類ID*/
	private List<String> vgroupAuthoritiesIds;

	/** 安全等級ID*/
	private String securityLevelId;

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<String> getVgroupAuthoritiesIds() {
		return vgroupAuthoritiesIds;
	}

	public void setVgroupAuthoritiesIds(List<String> vgroupAuthoritiesIds) {
		this.vgroupAuthoritiesIds = vgroupAuthoritiesIds;
	}

	public String getSecurityLevelId() {
		return securityLevelId;
	}

	public void setSecurityLevelId(String securityLevelId) {
		this.securityLevelId = securityLevelId;
	}

	@Override
	public String toString() {
		return "AA0202Req [vgroupId=" + vgroupId + ", keyword=" + keyword + ", vgroupAuthoritiesIds="
				+ vgroupAuthoritiesIds + ", securityLevelId=" + securityLevelId + "]";
	}


}
