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
import tpi.dgrv4.dpaa.service.DPB0063Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0063Req;
import tpi.dgrv4.dpaa.vo.DPB0063Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 關卡設定檔(v3.4)(後台)

 * @author mini
 */
@RestController
public class DPB0063Controller {

	private DPB0063Service service;
	
	@Autowired
	public DPB0063Controller(DPB0063Service service) {
		super();
		this.service = service;
	}

	/**
	 * save關卡與角色,優先使用status取代create/delete/clear操作
	 * 若沒有相同的 PK 值, 則新增.
	 * 若已存在相同的PK, 則 update [STATUS]
	 * create = 啟用
	 * delete = 停用
	 * clear = 全部停用
	 */

	@PostMapping(value = "/dgrv4/11/DPB0063", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0063Resp> saveLayer(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0063Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0063Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.saveLayer(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
