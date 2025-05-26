package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class AA0319Trunc {

	/** 顯示值 */
	private String val;

	/** 原始值 */
	@JsonInclude(Include.NON_NULL)
	private String ori;
	
	/** 原始值是否被截斷 */
	private Boolean t;

	public void setVal(String val) {
		this.val = val;
	}

	public Boolean getT() {
		return t;
	}
	
	public String getVal() {
		return val;
	}

	public void setT(Boolean t) {
		this.t = t;
	}

	public String getOri() {
		return ori;
	}

	public void setOri(String ori) {
		this.ori = ori;
	}

}