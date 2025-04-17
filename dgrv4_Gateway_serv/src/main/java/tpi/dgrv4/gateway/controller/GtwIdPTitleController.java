package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.GtwIdPTitleService;

/**
 * @author Mini 
 * 
 * 進入 GTW LDAP 登入畫面時, 取得要顯示的登入頁標題
 */
@RestController
public class GtwIdPTitleController {

	private GtwIdPTitleService service;

	@Autowired
	public GtwIdPTitleController(GtwIdPTitleService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/getTitle")
	public ResponseEntity<String> getTitle(@RequestHeader HttpHeaders headers, HttpServletRequest req,
			HttpServletResponse resp, @PathVariable(value = "idPType", required = true) String idPType) {

		return service.getTitle(headers, req, resp, idPType);
	}
}
