package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.AcIdPTitleService;

@RestController
public class AcIdPTitleController {
	private AcIdPTitleService service;

	@Autowired
	public AcIdPTitleController(AcIdPTitleService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/getTitle")
	public ResponseEntity<String> getTitle(@PathVariable(value = "idPType", required = true) String idPType) {

		String title = service.getTitle(idPType);
		return ResponseEntity.ok(title);
	}
}
