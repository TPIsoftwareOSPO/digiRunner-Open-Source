package tpi.dgrv4.dpaa.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.AA1003Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1003Req;
import tpi.dgrv4.dpaa.vo.AA1003Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;


/**
 * 
 * 更新組織	
 * 
 * @author Mavis
 *
 */
@RestController
public class AA1003Controller {
		
	private AA1003Service service;
		
	@Autowired
	public AA1003Controller(AA1003Service service) {
		super();
		this.service = service;
	}

	/**
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/AA1003", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA1003Resp> updateTOrgByOrgId(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA1003Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1003Resp resp = null;
		
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.updateTOrgByOrgId(tsmpHttpHeader.getAuthorization(), req.getBody());
			
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

	/**
	 * 檢查組織樹狀結構一致性
	 * 
	 * @return 檢查結果
	 */
	@PostMapping("/dgrv4/11/AA1003/validate")
	public Map<String, Object> validateOrgTree(@RequestHeader HttpHeaders headers, @RequestBody TsmpBaseReq<AA1003Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);


		try {
			TPILogger.tl.info("Starting organization tree consistency validation...");
			Map<String, Object> result = service.validateOrgTreeConsistency();
			TPILogger.tl.info(String.format("Organization tree consistency validation completed: %s", result));
			return result;
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Organization tree consistency validation failed: %s", StackTraceUtil.logStackTrace(e)));
			return Map.of(
				"isValid", false,
				"message", "Validation process error: " + e.getMessage()
			);
		}
	}
	
	/**
	 * 修復所有組織節點的路徑錯誤
	 * 
	 * @param auth 授權資訊
	 * @return 修復結果
	 */
	@PostMapping("/dgrv4/11/AA1003/repair")
	public Map<String, Object> repairAllOrgPaths(@RequestHeader HttpHeaders headers, @RequestBody TsmpBaseReq<AA1003Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

		TsmpAuthorization auth = tsmpHttpHeader.getAuthorization();
		
		try {
			String userName = auth != null ? auth.getUserName() : "system";
			TPILogger.tl.info(String.format("Starting organization path repair, executing user: %s", userName));
			
			// 先檢查一致性
			Map<String, Object> validateResult = service.validateOrgTreeConsistency();
			TPILogger.tl.info(String.format("Pre-repair validation result: %s", validateResult));
			
			// 執行修復
			Map<String, Object> repairResult = service.repairAllOrgPaths(userName);
			TPILogger.tl.info(String.format("Organization path repair completed: %s", repairResult));
			
			// 修復後再次檢查
			Map<String, Object> afterValidateResult = service.validateOrgTreeConsistency();
			TPILogger.tl.info(String.format("Post-repair validation result: %s", afterValidateResult));
			
			// 組合結果
			repairResult.put("beforeValidation", validateResult);
			repairResult.put("afterValidation", afterValidateResult);
			
			return repairResult;
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Organization path repair failed: %s", StackTraceUtil.logStackTrace(e)));
			return Map.of(
				"success", false,
				"message", "Repair process error: " + e.getMessage()
			);
		}
	}


}
