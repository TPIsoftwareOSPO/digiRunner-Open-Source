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
import tpi.dgrv4.dpaa.service.DPB0014Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0014Req;
import tpi.dgrv4.dpaa.vo.DPB0014Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * AppCaseCategoryService: 應用實例-後台<br/>
 * (目錄下包含哪些API)
 * @author Kim
 */
@RestController
public class DPB0014Controller {

	private DPB0014Service dpb0014Service;

	@Autowired
	public DPB0014Controller(DPB0014Service dpb0014Service) {
		super();
		this.dpb0014Service = dpb0014Service;
	}

	/**
	 * 實例查詢byId:<br/>
	 * 主要是查詢[TSMP_DP_APP]一筆資料, 但它的附件及使用哪些API也需要查找出來
	 * @param jsonStr
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0014", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0014Resp> queryAppById(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0014Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0014Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0014Service.queryAppById(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
