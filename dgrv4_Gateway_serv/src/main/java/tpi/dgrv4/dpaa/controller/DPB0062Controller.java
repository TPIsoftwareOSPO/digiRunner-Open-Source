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
import tpi.dgrv4.dpaa.service.DPB0062Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0062Req;
import tpi.dgrv4.dpaa.vo.DPB0062Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 排程模組(v3.4)(後台)<br/>
 * @author Kim
 */
@RestController
public class DPB0062Controller {

	private DPB0062Service dpb0062Service;
	
	@Autowired
	public DPB0062Controller(DPB0062Service dpb0062Service) {
		super();
		this.dpb0062Service = dpb0062Service;
	}


	/**
	 * 提供外部新增一筆 Job
	 * 新增一筆 Record 供排程執行
	 * 新增完成後，需要 refresh Memlist
	 * 由 refItemNo, refSubitemNo 兩個欄位決定是否將 in_param , 寫入 Tsmp_dp_appt_job
	 * 若值為 (A_SCHEDULE, CALL_API1) , 則另外寫入 Table = 'TSMP_DP_CALLAPI' , TSMP_DP_APPT_JOB. IN_PARAM 則寫入 CALLAPI_ID
	 * 執行 (A_SCHEDULE, CALL_API1) 工作, 將 Response String 回寫 TSMP_DP_CALLAPI
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0062", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0062Resp> createOneJob(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0062Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0062Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0062Service.createOneJob(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
