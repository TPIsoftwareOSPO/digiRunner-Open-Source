package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA0317Data {

	/** 註冊API清單, 或有匯出[API來源]為註冊(R)的API時，此欄位才會有值 */
	private List<AA0317Module> R;

	/** 組合API清單, 或有匯出[API來源]為組合(C)的API時，此欄位才會有值 */
	private List<AA0317Module> C;

	public List<AA0317Module> getR() {
		return R;
	}

	public void setR(List<AA0317Module> r) {
		R = r;
	}

	public List<AA0317Module> getC() {
		return C;
	}

	public void setC(List<AA0317Module> c) {
		C = c;
	}

}