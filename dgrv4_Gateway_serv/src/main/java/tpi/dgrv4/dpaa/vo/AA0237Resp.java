package tpi.dgrv4.dpaa.vo;

public class AA0237Resp {
	
	private AA0237RespA respA;

	private AA0237RespB1 respB1;

	private AA0237RespB2 respB2;

	public AA0237RespA getRespA() {
		return respA;
	}

	public void setRespA(AA0237RespA respA) {
		this.respA = respA;
	}

	public AA0237RespB1 getRespB1() {
		return respB1;
	}

	public void setRespB1(AA0237RespB1 respB1) {
		this.respB1 = respB1;
	}

	public AA0237RespB2 getRespB2() {
		return respB2;
	}

	public void setRespB2(AA0237RespB2 respB2) {
		this.respB2 = respB2;
	}

	@Override
	public String toString() {
		return "AA0237Resp [respA=" + respA + ", respB1=" + respB1 + ", respB2=" + respB2 + "]";
	}

	
}
