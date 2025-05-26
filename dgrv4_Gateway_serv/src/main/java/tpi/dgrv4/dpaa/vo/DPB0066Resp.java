package tpi.dgrv4.dpaa.vo;

public class DPB0066Resp {

	private DPB0066RespApiApplication apiApplication;

	private DPB0066RespApiOnOff apiOnOff;

	private DPB0066RespClientReg clientReg;
	
	private DPB0066RespOpenApiKey openApiKey;

	public DPB0066Resp() {}

	public DPB0066RespApiApplication getApiApplication() {
		return apiApplication;
	}

	public void setApiApplication(DPB0066RespApiApplication apiApplication) {
		this.apiApplication = apiApplication;
	}

	public DPB0066RespApiOnOff getApiOnOff() {
		return apiOnOff;
	}

	public void setApiOnOff(DPB0066RespApiOnOff apiOnOff) {
		this.apiOnOff = apiOnOff;
	}

	public DPB0066RespClientReg getClientReg() {
		return clientReg;
	}

	public void setClientReg(DPB0066RespClientReg clientReg) {
		this.clientReg = clientReg;
	}

	public DPB0066RespOpenApiKey getOpenApiKey() {
		return openApiKey;
	}

	public void setOpenApiKey(DPB0066RespOpenApiKey openApiKey) {
		this.openApiKey = openApiKey;
	}
	
}
