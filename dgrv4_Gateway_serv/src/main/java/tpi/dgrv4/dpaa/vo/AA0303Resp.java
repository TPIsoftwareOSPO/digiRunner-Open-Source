package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0303Resp {

	private List<AA0303Item> apiList;

	/** 警示訊息 */
	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<AA0303Item> getApiList() {
		return apiList;
	}

	public void setApiList(List<AA0303Item> apiList) {
		this.apiList = apiList;
	}

}