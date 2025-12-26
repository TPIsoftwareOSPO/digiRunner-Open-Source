package tpi.dgrv4.common.utils.autoInitSQL.vo;

import java.util.Date;

import tpi.dgrv4.common.utils.DateTimeUtil;

public class BasicFieldsVo {

	private Date createDateTime = DateTimeUtil.now();

	private String createUser = "SYSTEM";

	private Date updateDateTime;

	private String updateUser;

	private Long version = 1L;

	private String keywordSearch = "";

	/* getters and setters */

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getKeywordSearch() {
		return keywordSearch;
	}

	public void setKeywordSearch(String keywordSearch) {
		this.keywordSearch = keywordSearch;
	}

}