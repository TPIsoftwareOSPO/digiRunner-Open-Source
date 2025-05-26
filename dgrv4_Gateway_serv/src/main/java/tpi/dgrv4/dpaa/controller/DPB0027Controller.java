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
import tpi.dgrv4.dpaa.service.DPB0027Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0027Req;
import tpi.dgrv4.dpaa.vo.DPB0027Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * FaqService:常見問題維護-後台
 * @author Kim
 */
@RestController
public class DPB0027Controller {

	private DPB0027Service dpb0027Service;

	@Autowired
	public DPB0027Controller(DPB0027Service dpb0027Service) {
		super();
		this.dpb0027Service = dpb0027Service;
	}

	/**
	 * 常見問答Like查詢<br/>
	 * 查詢主表[TSMP_DP_FAQ_QUESTION]做分頁, order by 排序+PK
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0027", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0027Resp> queryFaqLikeList_0(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0027Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0027Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0027Service.queryFaqLikeList_0(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
