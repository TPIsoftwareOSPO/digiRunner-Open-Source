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
import tpi.dgrv4.dpaa.service.DPB0067Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0067Req;
import tpi.dgrv4.dpaa.vo.DPB0067Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 申請單模組(v3.4)(後台)<br/>
 * (type={用戶申請API/ 後台API上下架})
 * @author Kim
 */
@RestController
public class DPB0067Controller {

	private DPB0067Service dpb0067Service;

	@Autowired
	public DPB0067Controller(DPB0067Service dpb0067Service) {
		super();
		this.dpb0067Service = dpb0067Service;
	}

	/**
	 * query個人申請單/審核工作單LikeList
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0067", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0067Resp> queryReqLikeList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0067Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0067Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0067Service.queryReqLikeList(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
