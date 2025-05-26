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
import tpi.dgrv4.dpaa.service.DPB0026Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0026Req;
import tpi.dgrv4.dpaa.vo.DPB0026Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * FaqService:常見問題維護-後台
 * @author Kim
 */
@RestController
public class DPB0026Controller {

	private DPB0026Service dpb0026Service;

	@Autowired
	public DPB0026Controller(DPB0026Service dpb0026Service) {
		super();
		this.dpb0026Service = dpb0026Service;
	}

	/**
	 * 常見問答新增<br/>
	 * 新增三張 Table , file 部分先在前端轉 base64
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0026", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0026Resp> addFaq(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0026Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0026Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0026Service.addFaq(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
