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
import tpi.dgrv4.dpaa.service.DPB0056Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0056Req;
import tpi.dgrv4.dpaa.vo.DPB0056Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 主題分類(v3.4)(後台)<br/>
 * @author Kim
 */
@RestController
public class DPB0056Controller {

	private DPB0056Service dpb0056Service;

	@Autowired
	public DPB0056Controller(DPB0056Service dpb0056Service) {
		super();
		this.dpb0056Service = dpb0056Service;
	}

	/**
	 * query主題ByPk:<br/>
	 * 找出一筆資料, 供 update 使用
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0056", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0056Resp> queryThemeByPk(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0056Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0056Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0056Service.queryThemeByPk(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
