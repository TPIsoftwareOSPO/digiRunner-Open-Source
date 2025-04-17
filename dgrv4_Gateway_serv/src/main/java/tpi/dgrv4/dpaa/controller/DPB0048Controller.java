package tpi.dgrv4.dpaa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import tpi.dgrv4.dpaa.vo.DPB0047SubItems;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 清單列表(v3.4)(後台)，公告維護-狀態專用
 * 
 * @author min
 *
 */
@RestController
public class DPB0048Controller {
	
	private DPB0047Service service;
	
	@Autowired
	public DPB0048Controller(DPB0047Service service) {
		super();
		this.service = service;
	}

	/**
	 * 
	 * 1在【tsmp_dp_items】中 item_note = ENABLE_FLAG的資料是需多API共用的。
	 * 1.1在【公告維護】也是使用 item_note = ENABLE_FLAG的資料，但不需要【鎖定】資料，所以要過濾掉。	
	 * 2不重複程式直接呼叫DPB0047Service，取得的資料在Controller將【鎖定】資料過濾掉。
	 * 
	 */

	@PostMapping(value = "/dgrv4/11/DPB0048", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0047Resp> querySubItemsByItemNo_custom(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0047Req> req) {
		
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0047Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.querySubItemsByItemNo(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
			
			List<DPB0047SubItems> subItemsList = resp.getSubItems();
			subItemsList = subItemsList.stream().filter((item1)->{
				// 將【鎖定】資料過濾掉。
				if ("2".equals(item1.getSubitemNo())) {
					return false;
				}
				return true;
			}).collect(Collectors.toCollection(ArrayList::new));
			
			resp.setSubItems(subItemsList);
			
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
