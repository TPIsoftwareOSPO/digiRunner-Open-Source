package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0103Resp {

	private List<AA0103List> funcInfoList;

	public List<AA0103List> getFuncInfoList() {
		return funcInfoList;
	}

	public void setFuncInfoList(List<AA0103List> funcInfoList) {
		this.funcInfoList = funcInfoList;
	}

	@Override
	public String toString() {
		return "AA0103Resp [funcInfoList=" + funcInfoList + "]";
	}
	
	
}
