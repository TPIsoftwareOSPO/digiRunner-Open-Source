package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.entity.vo.AA0237ReqB;

public class AA0237Req {
	
	/** 虛擬群組ID*/
	private String vgroupId;

	/** 功能B的Request內容*/
	private AA0237ReqB reqB;

	public String getVgroupId() {
		return vgroupId;
	}

	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	public AA0237ReqB getReqB() {
		return reqB;
	}

	public void setReqB(AA0237ReqB reqB) {
		this.reqB = reqB;
	}

	@Override
	public String toString() {
		return "AA0237Req [vgroupId=" + vgroupId + ", reqB=" + reqB + "]";
	}
	
	
}
