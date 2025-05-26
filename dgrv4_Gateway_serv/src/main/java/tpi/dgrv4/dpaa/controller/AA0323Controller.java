package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0323Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0323Req;
import tpi.dgrv4.dpaa.vo.AA0323Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/***
 * 
 * 將Composer節點資料從RDB取出
 * 
 * @author min
 *
 */
@RestController
public class AA0323Controller {

	private AA0323Service service;

	@Autowired
	public AA0323Controller(AA0323Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/11/AA0323", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0323Resp> exportComposerFlow(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0323Req> req) {
		
		AA0323Resp resp = null;
		try {
			resp = service.exportComposerFlow(req.getBody(), req.getReqHeader(),
					headers);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
