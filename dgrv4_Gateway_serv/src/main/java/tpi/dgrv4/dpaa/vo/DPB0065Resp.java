package tpi.dgrv4.dpaa.vo;

public class DPB0065Resp {

	private DPB0065RespApiApplication apiApplication;

	private DPB0065RespApiOnOff apiOnOff;

	private DPB0065RespClientReg clientReg;
	
	private DPB0065RespOpenApiKey openApiKey;

	public DPB0065Resp() {
	}

	public DPB0065RespApiApplication getApiApplication() {
		return apiApplication;
	}

	public void setApiApplication(DPB0065RespApiApplication apiApplication) {
		this.apiApplication = apiApplication;
	}

	public DPB0065RespApiOnOff getApiOnOff() {
		return apiOnOff;
	}

	public void setApiOnOff(DPB0065RespApiOnOff apiOnOff) {
		this.apiOnOff = apiOnOff;
	}

	public DPB0065RespClientReg getClientReg() {
		return clientReg;
	}

	public void setClientReg(DPB0065RespClientReg clientReg) {
		this.clientReg = clientReg;
	}

	public DPB0065RespOpenApiKey getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(DPB0065RespOpenApiKey openApiKey) {
		this.openApiKey = openApiKey;
	}

}
