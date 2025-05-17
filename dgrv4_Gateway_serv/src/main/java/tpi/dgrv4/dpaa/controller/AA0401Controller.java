package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.AA0401Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0401Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class AA0401Controller {

	private AA0401Service service;
	
	@Autowired
	public AA0401Controller(AA0401Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/AA0401", //
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0401Resp> uploadModuleFile(@RequestHeader HttpHeaders headers, @RequestParam("txSN") String txSN, @RequestParam("txDate") String txDate
			, @RequestParam("txID") String txID, @RequestParam("cID") String cID, @RequestParam("file") MultipartFile mtpFile) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0401Resp resp = null;
		ReqHeader reqHeader = new ReqHeader();
		try {
			reqHeader.setcID(cID);
			reqHeader.setTxDate(txDate);
			reqHeader.setTxID(txID);
			reqHeader.setTxSN(txSN);
			
			resp = service.uploadModuleFile(tsmpHttpHeader.getAuthorization(), mtpFile.getOriginalFilename(), mtpFile.getBytes());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, reqHeader);
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(reqHeader, resp);
	}
}
