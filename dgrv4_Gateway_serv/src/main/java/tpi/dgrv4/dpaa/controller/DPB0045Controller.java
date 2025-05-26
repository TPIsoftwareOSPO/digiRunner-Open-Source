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
import tpi.dgrv4.dpaa.service.DPB0045Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0045Req;
import tpi.dgrv4.dpaa.vo.DPB0045Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 公告區/最新消息(v3.4)(後台)
 * @author mini
 */
@RestController
public class DPB0045Controller {
	
	private DPB0045Service service;
	
	@Autowired
	public DPB0045Controller(DPB0045Service service) {
		super();
		this.service = service;
	}

	/**
	 * 公告消息-queryId				
	 * 於後台中query by Id				
	 * 查詢的資料可用於 update				
	 * 前台使用這支API不用取orgId, 且需要 where 狀態= 啟用(寫在後台程)				
	 * 後台使用這支API, 不考慮狀態								
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0045", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0045Resp> queryNewsById(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0045Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0045Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryNewsById(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
