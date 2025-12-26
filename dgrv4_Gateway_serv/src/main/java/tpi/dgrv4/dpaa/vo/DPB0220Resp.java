package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0220Resp {

	private List<DPB0220RespItem> infoList;

	public DPB0220Resp() {

	}

	public DPB0220Resp(List<DPB0220RespItem> infoList) {
		this.infoList = infoList;
	}

	public List<DPB0220RespItem> getInfoList() {
		return infoList;
	}

	public void setInfoList(List<DPB0220RespItem> infoList) {
		this.infoList = infoList;
	}

}
