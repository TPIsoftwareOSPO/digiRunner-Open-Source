package tpi.dgrv4.dpaa.vo;

public class DPB0111Item {

	private String roleId;

	private String roleName;

	private String roleAlias;

	private String txId;

	private Boolean isTxIdTruncated;

	private String oriTxId;

	private String listType;

	private String listTypeName;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleName() {
		return roleName;
	}
	
	public String getRoleAlias() {
		return roleAlias;
	}

	public void setRoleAlias(String roleAlias) {
		this.roleAlias = roleAlias;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public Boolean getIsTxIdTruncated() {
		return isTxIdTruncated;
	}

	public void setIsTxIdTruncated(Boolean isTxIdTruncated) {
		this.isTxIdTruncated = isTxIdTruncated;
	}

	public String getOriTxId() {
		return oriTxId;
	}

	public void setOriTxId(String oriTxId) {
		this.oriTxId = oriTxId;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getListTypeName() {
		return listTypeName;
	}

	public void setListTypeName(String listTypeName) {
		this.listTypeName = listTypeName;
	}

}