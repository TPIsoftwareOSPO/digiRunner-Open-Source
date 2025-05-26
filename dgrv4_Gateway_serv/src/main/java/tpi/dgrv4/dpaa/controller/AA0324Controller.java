package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0324Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0324Req;
import tpi.dgrv4.dpaa.vo.AA0324Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/***
 * 
 * 取得ES Log開關狀態
 * 
 * @author min
 *
 */
@RestController
public class AA0324Controller {

	private AA0324Service service;

	@Autowired
	public AA0324Controller(AA0324Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/11/AA0324", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0324Resp> getESLogDisable(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0324Req> req) {
		
		AA0324Resp resp = null;
		try {
			resp = service.getESLogDisable(req.getBody(), req.getReqHeader(),
					headers);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
