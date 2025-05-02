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
import tpi.dgrv4.dpaa.service.AA0321Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0321Req;
import tpi.dgrv4.dpaa.vo.AA0321Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/***
 * 
 * 以組織查詢API清單
 * 
 * @author min
 *
 */
@RestController
public class AA0321Controller {
	
	private AA0321Service service;
	
	@Autowired
	public AA0321Controller(AA0321Service service) {
		super();
		this.service = service;
	}

	/**
	 * Keyword Search API (API系列 /所屬單位) 			
	 * 提供後台開窗查詢, 需要分頁				
							
	 * [Table 查詢條件]				
	 * TSMP_API (主查詢表)				
	 * 		API_KEY (Like)			
	 * 		API_NAME (Like)			
	 * 		API_DESC (Like)			
	 * 		ORG_ID (後台:只能查看自己下屬組織)	
	 * TSMP_ORGANIZATION (所屬單位名稱 Like)				
	 * 		ORG_NAME (Like)			
	 * 		ORG_CODE (Like)		
	 * 				
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/AA0321", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0321Resp> queryAPIListByOrg(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0321Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0321Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryAPIListByOrg(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
