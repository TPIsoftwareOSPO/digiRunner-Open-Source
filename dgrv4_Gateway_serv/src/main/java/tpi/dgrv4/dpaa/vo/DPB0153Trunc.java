package tpi.dgrv4.dpaa.vo;

public class DPB0153Trunc {
	private String val; // 顯示值
	private boolean t; // 原始值是否被截斷
	private String ori; // 原始值

    public DPB0153Trunc() {}
	
    public DPB0153Trunc(String ori) {
       
        if (ori.length() > 20) {
        	this.ori = ori;
            this.val = ori.substring(0, 20);
            this.t = true;
        } else {
            this.val = ori;
            this.t = false;
        }
    }

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public boolean isT() {
		return t;
	}

	public void setT(boolean t) {
		this.t = t;
	}

	public String getOri() {
		return ori;
	}

	public void setOri(String ori) {
		this.ori = ori;
	}

}
