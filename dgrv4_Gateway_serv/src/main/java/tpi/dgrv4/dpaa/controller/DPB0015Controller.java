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
import tpi.dgrv4.dpaa.service.DPB0015Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0015Req;
import tpi.dgrv4.dpaa.vo.DPB0015Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * AppCaseCategoryService: 應用實例-後台<br/>
 * (目錄下包含哪些API)
 * @author Kim
 */
@RestController
public class DPB0015Controller {

	private DPB0015Service dpb0015Service;

	@Autowired
	public DPB0015Controller(DPB0015Service dpb0015Service) {
		super();
		this.dpb0015Service = dpb0015Service;
	}

	/**
	 * 實例更新byId:<br/>
	 * 主要是更新[TSMP_DP_APP]一筆資料,<br/>
	 * 次要為更新本實例包含什麼API [TSMP_DP_API_APP] ,<br/>
	 * 再次要是有哪些附加檔案				
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0015", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0015Resp> updateAppById(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0015Req> req) {
		
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0015Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0015Service.updateAppById(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
