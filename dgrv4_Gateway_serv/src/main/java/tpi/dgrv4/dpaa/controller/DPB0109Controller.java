package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0109Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0109Req;
import tpi.dgrv4.dpaa.vo.DPB0109Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 事件檢視器(後台) - 更新事件是否封存依PK<br/>
 * 
 */
//@RestController 停用[事件檢視器]功能 2022/09/26
public class DPB0109Controller {

	private DPB0109Service dpb0109Service;
	
	@Autowired
	public DPB0109Controller(DPB0109Service dpb0109Service) {
		super();
		this.dpb0109Service = dpb0109Service;
	}


	/**
	 * 更新事件是否封存依PK
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0109", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0109Resp> archiveEventByPk(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0109Req> req) {
		
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0109Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0109Service.archiveEventByPk(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}	

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
