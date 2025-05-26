package tpi.dgrv4.gateway.funcInterFace;

import tpi.dgrv4.gateway.service.CusIdPService.RedirectException;

@FunctionalInterface
public interface CusLoginFailedHandler {

	void handle(String customErrorMsg) throws RedirectException;

}
