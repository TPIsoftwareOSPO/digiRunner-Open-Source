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
import tpi.dgrv4.dpaa.service.DPB0075Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0075Req;
import tpi.dgrv4.dpaa.vo.DPB0075Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * LOV 模組(v3.4)(後台)
 * 
 * @author mini
 */
@RestController
public class DPB0075Controller {
	
	private DPB0075Service service;
	
	@Autowired
	public DPB0075Controller(DPB0075Service service) {
		super();
		this.service = service;
	}

	/**
	 * Keyword Search API (API系列 /主題分類系列 /所屬單位) (分為已上架/未上架)				
	 * 提供後台開窗查詢, 需要分頁				
							
	 * [Table 查詢條件]				
	 * TSMP_API (主查詢表)				
	 * 		API_KEY (Like)			
	 * 		API_NAME (Like)			
	 * 		API_DESC (Like)			
	 * 		ORG_ID (後台:只能查看自己下屬組織)			
	 * TSMP_API_EXT (一對一存在, 不存在表示未上架)				
	 * 		DP_STATUS (後台:傳入上下架參數)			
	 * TSMP_DP_API_THEME(API與theme對應檔) + TSMP_DP_THEME_CATEGORY				
	 * 		THEME_NAME (Like)			
	 * TSMP_ORGANIZATION (所屬單位名稱 Like)				
	 * 		ORG_NAME (Like)			
	 * 		ORG_CODE (Like)		
	 * 				
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0075", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0075Resp> queryApiLov(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0075Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0075Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryApiLov(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
