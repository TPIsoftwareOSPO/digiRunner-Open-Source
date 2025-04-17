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
import tpi.dgrv4.dpaa.service.DPB0047Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0047Req;
import tpi.dgrv4.dpaa.vo.DPB0047Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 清單列表(v3.4)(後台)
 * @author mini
 */

@RestController
public class DPB0047Controller {
	
	private DPB0047Service service;
	
	@Autowired
	public DPB0047Controller(DPB0047Service service) {
		super();
		this.service = service;
	}

	/**
	 * query XX類型 Lov List
	 * 代入 TSMP_DP_ITEMS.itemNo, 取出此類型下的子類型
	 * itemNo需要經過BcryptParam編碼, 參考的SQL = select item_no  
	 * from TSMP_DP_ITEMS group by item_no 
	 * order by  sort_by, item_id; //這張表index不用排序sort_by
	 * 可做為Flag 設計的應用, ex:前台檢查是否有使用"會員註冊", 
	 * callThisMethod(itemNo="MEMBER_REG_FLAG", isDefault=Y), 會回傳 subItemNo值 
	 */

	@PostMapping(value = "/dgrv4/11/DPB0047", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0047Resp> querySubItemsByItemNo(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0047Req> req) {
		
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0047Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.querySubItemsByItemNo(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
