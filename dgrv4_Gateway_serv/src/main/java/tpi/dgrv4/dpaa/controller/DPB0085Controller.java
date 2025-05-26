
package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0085Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0085Req;
import tpi.dgrv4.dpaa.vo.DPB0085Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0085Controller {
	
	private DPB0085Service service;
	
	@Autowired
	public DPB0085Controller(DPB0085Service service) {
		super();
		this.service = service;
	}

	/** 
	 *	ClientPEM檔-上傳/解析/save				
	 *	1. 上傳憑證PEM檔
	 *	2. 解析PEM檔內容
	 *	3. 比對該憑證效期 (憑證最多保留2張憑證到期日最新的) , 同一個 Client Id 最多只有2張憑證, 若已有2張則把最舊的刪除, 若上傳相同的憑證則 update, 相同憑證的判斷為憑證創建日
	 *		& 憑證到期日都相同
	 *	4. 將對應之內容與PEM檔原始資料 Insert 至 TSMP_CLIENT_CERT
	 *	5. 若是異動2筆資料, 則需要依 ""規範"" 加註 @Transactional"				
	 */
	@PostMapping(value = "/dgrv4/11/DPB0085", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0085Resp> uploadClientCA(@RequestHeader HttpHeaders headers, @RequestBody TsmpBaseReq<DPB0085Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0085Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.uploadClientCA(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
