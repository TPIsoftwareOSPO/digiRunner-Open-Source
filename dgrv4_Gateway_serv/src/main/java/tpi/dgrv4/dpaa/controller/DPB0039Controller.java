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
import tpi.dgrv4.dpaa.service.DPB0039Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0039Req;
import tpi.dgrv4.dpaa.vo.DPB0039Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * TsmpUserService:TSMP管理者-後台
 * @author Kim
 */
@RestController
public class DPB0039Controller {

	private DPB0039Service dpb0039Service;
	
	@Autowired
	public DPB0039Controller(DPB0039Service dpb0039Service) {
		super();
		this.dpb0039Service = dpb0039Service;
	}

	/**
	 * Tsmp管理者Like查詢<br/>
	 * 分頁查詢TsmpUser,<br/>
	 * Like 3 欄位:userName, userAlias, userEmail<br/>
	 * where userStatus = 1<br/>
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0039", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0039Resp> queryTsmpUserLikeList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0039Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0039Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0039Service.queryTsmpUserLikeList(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
