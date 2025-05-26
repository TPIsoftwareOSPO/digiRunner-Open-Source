package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class AA0302Trunc {

	/** 顯示值*/
	private String v;
	
	/** 原始值是否被截斷*/
	private Boolean t;
	
	/** 原始值*/
	@JsonInclude(Include.NON_NULL)
	private String o;

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public Boolean getT() {
		return t;
	}

	public void setT(Boolean t) {
		this.t = t;
	}

	public String getO() {
		return o;
	}

	public void setO(String o) {
		this.o = o;
	}

	@Override
	public String toString() {
		return "AA0302Trunc [v=" + v + ", t=" + t + ", o=" + o + "]";
	}

}
