package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0189Req {
	
	private String connName;
	private String strSql;
	private List<String> paramList;
	
	@Override
	public String toString() {
		return "DPB0189Req [connName=" + connName + ", strSql=" + strSql + ", paramList=" + paramList + "]";
	}
	public String getConnName() {
		return connName;
	}
	public void setConnName(String connName) {
		this.connName = connName;
	}
	public String getStrSql() {
		return strSql;
	}
	public void setStrSql(String strSql) {
		this.strSql = strSql;
	}
	public List<String> getParamList() {
		return paramList;
	}
	public void setParamList(List<String> paramList) {
		this.paramList = paramList;
	}
	
	
	
}
