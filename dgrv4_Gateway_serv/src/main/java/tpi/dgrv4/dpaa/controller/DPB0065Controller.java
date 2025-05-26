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
import tpi.dgrv4.dpaa.service.DPB0065Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0065Req;
import tpi.dgrv4.dpaa.vo.DPB0065Resp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 申請單模組(v3.4)(後台)<br/>
 * (type={用戶申請API/ 後台API上下架})
 * @author Kim
 */
@RestController
public class DPB0065Controller {

	private DPB0065Service dpb0065Service;
					
	@Autowired
	public DPB0065Controller(DPB0065Service dpb0065Service) {
		super();
		this.dpb0065Service = dpb0065Service;
	}

	/**
	 * create申請單<br/>
	 * 建立不同類型的申請單
	 * @param jsonStr
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0065", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0065Resp> createReq(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0065Req> req, HttpServletRequest httpReq) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0065Resp resp = null;
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
						
			resp = dpb0065Service.createReq(auth, req.getBody(), req.getReqHeader(), iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
