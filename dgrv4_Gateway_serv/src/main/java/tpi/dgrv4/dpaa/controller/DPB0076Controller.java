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
import tpi.dgrv4.dpaa.service.DPB0076Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0076Req;
import tpi.dgrv4.dpaa.vo.DPB0076Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * LOV 模組(v3.4)(後台)
 * 
 * @author mini
 */
@RestController
public class DPB0076Controller {
	
	private DPB0076Service service;
	
	@Autowired
	public DPB0076Controller(DPB0076Service service) {
		super();
		this.service = service;
	}

	/**
	 * 主題分類 Lov
	 * 系統中所有需要選取並代ThemeId 的開窗功能, 作成下拉複選, 回傳的內容需分頁
	 * 按放大鏡, Open Lov 可以輸入 Keyword後enter 執行Search
	 * 回傳主題, 勾選 check box 可以帶回 id 及 value
	 * ex : 2 - 健保, 3 - 社會					
	 * 				
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0076", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0076Resp> queryThemeLov(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0076Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0076Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryThemeLov(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
