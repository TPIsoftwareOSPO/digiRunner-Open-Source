package tpi.dgrv4.dpaa.vo;

public class AA0316Func {

	/** Token Payload */
	private Boolean tokenPayload;

	public int convert() {
		int funFlag = 0;
		if (this.tokenPayload) {
			funFlag++;
		}
		return funFlag;
	}

	public Boolean getTokenPayload() {
		return tokenPayload;
	}

	public void setTokenPayload(Boolean tokenPayload) {
		this.tokenPayload = tokenPayload;
	}
	
}