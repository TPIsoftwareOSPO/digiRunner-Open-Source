package tpi.dgrv4.gateway.vo;

import org.springframework.http.ResponseEntity;

public class ClientAssertionJwtData {
	private ResponseEntity<OAuthTokenErrorResp2> errRespEntity;
	private String clientId;

	public ResponseEntity<OAuthTokenErrorResp2> getErrRespEntity() {
		return errRespEntity;
	}

	public void setErrRespEntity(ResponseEntity<OAuthTokenErrorResp2> errRespEntity) {
		this.errRespEntity = errRespEntity;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
