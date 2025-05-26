package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0087Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0087Req;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * PEM檔案下載
 * 
 * @author Kim
 */
@RestController
public class DPB0087Controller {

	private DPB0087Service service;

	@Autowired
	public DPB0087Controller(DPB0087Service service) {
		super();
		this.service = service;
	}

	/**
	 * 下載PEM檔案:<br/>
	 * 依據指定的certId下載Zip檔案
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0087", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> downLoadPEMFile(@RequestHeader HttpHeaders headers, //
			@RequestBody TsmpBaseReq<DPB0087Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			return service.downLoadPEMFile(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

}
