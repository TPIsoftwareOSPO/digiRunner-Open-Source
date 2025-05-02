package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0240Resp {

	private List<DPB0240RespItem> infoList;

	public DPB0240Resp() {

	}

	public DPB0240Resp(List<DPB0240RespItem> infoList) {
		this.infoList = infoList;
	}

	public List<DPB0240RespItem> getInfoList() {
		return infoList;
	}

	public void setInfoList(List<DPB0240RespItem> infoList) {
		this.infoList = infoList;
	}

}
