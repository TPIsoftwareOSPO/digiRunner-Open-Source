package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0073Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0073Req;
import tpi.dgrv4.dpaa.vo.DPB0073Resp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 審核模組(v3.4)(後台)
 * (type={API申請/上架/下架/異動})
 * @author Mini
 */
@RestController
public class DPB0073Controller {

	private DPB0073Service service;

	@Autowired
	public DPB0073Controller(DPB0073Service service) {
		super();
		this.service = service;
	}

	/**
	 * API開放權限設定
	 * 可異動API. public_flag 欄位 , 異動完成 body 不會回傳資料, 只有 rtn code = 0000, 允許指定多筆 API 批次異動.				
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0073", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0073Resp> setApiPublicFlag(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0073Req> req, HttpServletRequest httpReq) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0073Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			
			// 此 param 為內部使用, http 的處理放在 Controller 層級, 不要放在 Service 層級中
			InnerInvokeParam iip = InnerInvokeParam.getInstance(headers, httpReq, tsmpHttpHeader.getAuthorization());
			
			TsmpAuthorization auth = null;
			if(iip != null && StringUtils.hasText(iip.getAcToken())) {//若使用Api key調用此API,將auth換成acToken,以便後面得到真正的user name,否則會是DGRK 
				auth = ControllerUtil.parserAuthorization(iip.getAcToken());
			}else {
				auth = tsmpHttpHeader.getAuthorization();
			}
			
			resp = service.setApiPublicFlag(auth, req.getBody(), req.getReqHeader(), iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
