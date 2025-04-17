package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1212Resp {
	private List<AA1212RespItem> dataList;
	private List<AA1212LastLoginLog> lastLoginLogList;
	
	public List<AA1212RespItem> getDataList() {
		return dataList;
	}
	public void setDataList(List<AA1212RespItem> dataList) {
		this.dataList = dataList;
	}
	public List<AA1212LastLoginLog> getLastLoginLogList() {
		return lastLoginLogList;
	}
	public void setLastLoginLogList(List<AA1212LastLoginLog> lastLoginLogList) {
		this.lastLoginLogList = lastLoginLogList;
	}
	
}
