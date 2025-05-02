package tpi.dgrv4.dpaa.vo;

public class AA0301Pair {

	/** 顯示文字	*/
	private String n;
	
	/** 值*/
	private String v;

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public void setN(String n) {
		this.n = n;
	}
	
	public String getN() {
		return n;
	}

	@Override
	public String toString() {
		return "AA0301Pair [v=" + v + ", n=" + n + "]";
	}
	
}
