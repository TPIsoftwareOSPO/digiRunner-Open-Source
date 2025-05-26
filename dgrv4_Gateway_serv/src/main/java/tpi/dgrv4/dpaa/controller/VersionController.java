package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0118Service;
import tpi.dgrv4.dpaa.vo.DPB0118Resp;
import tpi.dgrv4.escape.ESAPI;

@RestController
public class VersionController {
	
	private DPB0118Service service;
	
	@Autowired
	public VersionController(ApplicationContext applicationContext) {
		super();
		if (applicationContext != null) {
			this.service = applicationContext.getBean(DPB0118Service.class); // non-Singleton
		}
	}

	@GetMapping(value = "/dgrv4/version")
	public ResponseEntity<?> queryVersion(HttpServletRequest request,
			@RequestParam(value = "url", required = false) String url) {
		DPB0118Resp resp = null;
		String json = "null";
		try {
			String ipAddress = null;
			if(request!=null) {
				ipAddress = request.getRemoteAddr();
			}
			boolean value = false;
			if (StringUtils.hasLength(url)) {
				value = true;
			}
			
			resp = service.queryModuleVersion(ipAddress,null,false); // Service
			ObjectMapper om = new ObjectMapper();
			json = om.writeValueAsString(resp);
			
			//checkmarx, Reflected XSS All Clients 
			json = ESAPI.encoder().encodeForHTML(json);
		} catch (Exception e) {
			throw new TsmpDpAaException(e.getMessage(), e);
		}
		return new ResponseEntity<Object>(json, HttpStatus.OK);
	}
}
