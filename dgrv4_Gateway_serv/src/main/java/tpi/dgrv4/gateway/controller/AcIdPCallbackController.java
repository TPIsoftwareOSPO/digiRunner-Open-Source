package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.service.AcIdPCallbackService;

/**
 * 1.在前面的流程 IdP(GOOGLE/MS) 重新導向(302)到 OAuth 同意畫面, 
 * 2.User 登入成功後, IdP(GOOGLE/MS) 重新導向(302)到此 API
 * 3.得到 IdP(GOOGLE/MS) 核發的授權碼
 * 4.打 IdP(GOOGLE/MS) Token URL, 取得 Access Token 和 ID Token
 */
@RestController
public class AcIdPCallbackController {
	
    private AcIdPCallbackService acIdpCallbackService;

    @Autowired
    public AcIdPCallbackController(AcIdPCallbackService acIdpCallbackService) {
		super();
		this.acIdpCallbackService = acIdpCallbackService;
	}

	@GetMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/acIdPCallback")
    public void acIdPCallback(@RequestHeader HttpHeaders headers, 
    		@PathVariable("idPType") String idPType, 
    		HttpServletRequest req, 
    		HttpServletResponse resp) throws IOException {

		try {
			//checkmarx, Unchecked Input for Loop Condition
			String type = CheckmarxUtils.sanitizeForCheckmarx(idPType);
			acIdpCallbackService.acIdPCallback(headers, req, resp, type);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
    }
}
