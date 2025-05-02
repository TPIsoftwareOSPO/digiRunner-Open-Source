package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0114Resp {

	private String oriRoleId;

	private String oriListType;

	private List<String> oriTxIdList;

	public String getOriRoleId() {
		return oriRoleId;
	}

	public void setOriRoleId(String oriRoleId) {
		this.oriRoleId = oriRoleId;
	}

	public void setOriListType(String oriListType) {
		this.oriListType = oriListType;
	}
	
	public String getOriListType() {
		return oriListType;
	}

	public List<String> getOriTxIdList() {
		return oriTxIdList;
	}

	public void setOriTxIdList(List<String> oriTxIdList) {
		this.oriTxIdList = oriTxIdList;
	}

}