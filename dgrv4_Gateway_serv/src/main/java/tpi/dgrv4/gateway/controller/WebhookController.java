package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.WebhookService;

@RestController
public class WebhookController {	
	
	private ObjectMapper objectMapper;
	
    private TsmpTAEASKHelper tsmpTAEASKHelper;
	
	private WebhookService webhookService;
	
	@Autowired
	public WebhookController(ObjectMapper objectMapper, TsmpTAEASKHelper tsmpTAEASKHelper, WebhookService webhookService) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.webhookService = webhookService;
	}

	@PostMapping(value = "/dgrv4/webhook",
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> webhook(HttpServletRequest httpReq, 
			HttpServletResponse httpResp, @RequestHeader HttpHeaders headers) {
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			return webhookService.handleNotifications(httpReq, httpResp, headers);									
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
        return this.tsmpTAEASKHelper;
    }
}
