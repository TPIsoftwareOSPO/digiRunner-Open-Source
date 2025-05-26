package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.service.DPB0059Service;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.DPB0059Req;
import tpi.dgrv4.gateway.vo.DPB0059Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 排程模組(v3.4)(後台)<br/>
 * @author Kim
 */
@RestController
public class DPB0059Controller {

	private DPB0059Service dpb0059Service;

	@Autowired
	public DPB0059Controller(DPB0059Service dpb0059Service) {
		super();
		this.dpb0059Service = dpb0059Service;
	}

	/**
	 * 指定某一筆工作[執行]/[重做]
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0059", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0059Resp> doJobByPk(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0059Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0059Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0059Service.doJobByPk(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new DgrException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
