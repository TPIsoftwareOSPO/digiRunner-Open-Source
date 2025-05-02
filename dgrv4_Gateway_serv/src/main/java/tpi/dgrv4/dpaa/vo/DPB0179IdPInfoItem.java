package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0179IdPInfoItem {
	private String id;
	private String longId;
	private String iconFile;
	private String status;
	private String pageTitle;
	private Integer ldapTimeout;;
	private List<DPB0179LdapDataItem> ldapDataList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public Integer getLdapTimeout() {
		return ldapTimeout;
	}

	public void setLdapTimeout(Integer ldapTimeout) {
		this.ldapTimeout = ldapTimeout;
	}

	public List<DPB0179LdapDataItem> getLdapDataList() {
		return ldapDataList;
	}

	public void setLdapDataList(List<DPB0179LdapDataItem> ldapDataList) {
		this.ldapDataList = ldapDataList;
	}
}