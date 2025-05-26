package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0004Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0004Req;
import tpi.dgrv4.dpaa.vo.DPB0004Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 會員審核-後台
 * @author Kim
 */
//@RestController
public class DPB0004Controller {

	private DPB0004Service dpb0004Service;

	@Autowired
	public DPB0004Controller(DPB0004Service dpb0004Service) {
		super();
		this.dpb0004Service = dpb0004Service;
	}

	/**
	 * 前朝遺物,不再使用
	 * 查詢未放行會員:<br/>
	 * 查詢已狀態=1：送審，4：重新送審
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0004", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0004Resp> queryUnReleaseMember(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0004Req> req) {
	
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0004Resp resp = null;
		try {
//			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			String clientId = tsmpHttpHeader.getAuthorization().getClientId();
			resp = dpb0004Service.queryUnReleaseMember(clientId, req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
