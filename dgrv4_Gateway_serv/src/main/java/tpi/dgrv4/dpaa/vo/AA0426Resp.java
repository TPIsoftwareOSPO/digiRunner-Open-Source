package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0426Resp {
	private List<AA0426RespItem> errList;
	private String errMsg;

	public List<AA0426RespItem> getErrList() {
		return errList;
	}

	public void setErrList(List<AA0426RespItem> errList) {
		this.errList = errList;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
}
