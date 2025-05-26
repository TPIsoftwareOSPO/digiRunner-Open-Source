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
import tpi.dgrv4.dpaa.service.DPB0046Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0046Req;
import tpi.dgrv4.dpaa.vo.DPB0046Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 公告區/最新消息 (v3.4)(後台)
 * @author mini
 */
@RestController
public class DPB0046Controller {

	private DPB0046Service service;
	
	@Autowired
	public DPB0046Controller(DPB0046Service service) {
		super();
		this.service = service;
	}

	/**
	 * 公告消息-delete
	 * 於後台中delete by Id, 支援多筆一次更新				
	 * 實際作法: update 狀態=停用				
	 * 為了避免過多舊資料, 以Deferrable Job delete過期的舊記錄, 逐筆執行刪除, 每筆執行完成後以 logger 顯示於主機 console.				
	 * 舊記錄保留 n 天以 properties設定, -1表示不做刪除的動作				
	 * [delete]前 Pop 一個確認窗, 帶入標題確定要刪除?				
	 * 全部都無法刪除才會throws 1200							
	 */

	@PostMapping(value = "/dgrv4/11/DPB0046", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0046Resp> deleteNews_v3_4(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0046Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		DPB0046Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.deleteNews_v3_4(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
