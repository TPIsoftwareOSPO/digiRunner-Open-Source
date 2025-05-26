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
import tpi.dgrv4.dpaa.service.DPB0029Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0029Req;
import tpi.dgrv4.dpaa.vo.DPB0029Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * FaqService:常見問題維護-後台
 * @author Kim
 */
@RestController
public class DPB0029Controller {

	private DPB0029Service dpb0029Service;

	@Autowired
	public DPB0029Controller(DPB0029Service dpb0029Service) {
		super();
		this.dpb0029Service = dpb0029Service;
	}

	/**
	 * 常見問答查詢byId<br/>
	 * 查找出一筆資料, 提供給更新畫面使用
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0029", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0029Resp> queryFaqById(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0029Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0029Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0029Service.queryFaqById(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
