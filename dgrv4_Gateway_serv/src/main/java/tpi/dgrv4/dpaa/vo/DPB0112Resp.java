package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0112Resp {

	private String oriRoleId;

	private String oriRoleName;

	private String oriRoleAlias;

	private String oriListType;

	private List<String> oriTxIdList;

	private String oriTxIdString;

	public String getOriRoleId() {
		return oriRoleId;
	}

	public void setOriRoleId(String oriRoleId) {
		this.oriRoleId = oriRoleId;
	}

	public String getOriRoleName() {
		return oriRoleName;
	}

	public void setOriRoleName(String oriRoleName) {
		this.oriRoleName = oriRoleName;
	}

	public String getOriRoleAlias() {
		return oriRoleAlias;
	}

	public void setOriRoleAlias(String oriRoleAlias) {
		this.oriRoleAlias = oriRoleAlias;
	}

	public String getOriListType() {
		return oriListType;
	}

	public void setOriListType(String oriListType) {
		this.oriListType = oriListType;
	}

	public List<String> getOriTxIdList() {
		return oriTxIdList;
	}

	public void setOriTxIdList(List<String> oriTxIdList) {
		this.oriTxIdList = oriTxIdList;
	}

	public String getOriTxIdString() {
		return oriTxIdString;
	}

	public void setOriTxIdString(String oriTxIdString) {
		this.oriTxIdString = oriTxIdString;
	}

}