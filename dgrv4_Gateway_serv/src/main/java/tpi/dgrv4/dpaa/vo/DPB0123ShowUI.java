package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DPB0123ShowUI {

	private DPB0123CgRespBody cgRespBody;
	
	@Override
	public String toString() {
		return "DPB0123ShowUI [cgRespBody=" + cgRespBody + "]";
	}

	public DPB0123CgRespBody getCgRespBody() {
		return cgRespBody;
	}

	public void setCgRespBody(DPB0123CgRespBody cgRespBody) {
		this.cgRespBody = cgRespBody;
	}

}