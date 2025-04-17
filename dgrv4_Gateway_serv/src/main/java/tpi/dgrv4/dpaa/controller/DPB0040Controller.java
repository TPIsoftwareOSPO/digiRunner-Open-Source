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
import tpi.dgrv4.dpaa.service.DPB0040Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0040Req;
import tpi.dgrv4.dpaa.vo.DPB0040Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 查詢可用Module<br/>
 * 分頁查詢Tsmp_API_MODULE , Like 1欄位
 * moduleName
 * where active=1
 * 若 moduleNanme 存在於 TSMP_DP_DENIED_MODULE者, 表示不公開				
 * @author John
 */
@RestController
public class DPB0040Controller {

	private DPB0040Service dpb0040Service;

	@Autowired
	public DPB0040Controller(DPB0040Service dpb0040Service) {
		super();
		this.dpb0040Service = dpb0040Service;
	}

	/**
	 * 主題分類刪除byId:<br/>
	 * 刪除主表一筆資料及其關連表的資料	
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0040", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0040Resp> queryModuleLikeList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0040Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0040Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0040Service.queryModuleLikeList(tsmpHttpHeader.getAuthorization().getClientId(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

//	public String queryModuleLikeListxxx(TsmpBaseReq<DPB0040Req> req) {
//		
//	}
//	
//	public String queryModuleLikeListxxx(DPB0040Req req) {
//		
//	}
}

