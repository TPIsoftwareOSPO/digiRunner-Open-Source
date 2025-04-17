package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0201Req;
import tpi.dgrv4.dpaa.vo.DPB0201Resp;
import tpi.dgrv4.dpaa.vo.DPB0201RespItem;
import tpi.dgrv4.gateway.TCP.Packet.RequireWebsiteTargetThroughputPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0201Service {
	private ObjectMapper objectMapper;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0201Service(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public DPB0201Resp queryTargetThroughput(TsmpAuthorization auth, DPB0201Req req) {

		DPB0201Resp resp = new DPB0201Resp();
		try {
			if(StringUtils.hasText(req.getWebsiteName())) {
				String strJson =  getData(req);
				if(StringUtils.hasText(strJson)) {
					Map<String, Map<String, Integer>> map = getObjectMapper().readValue(strJson, new TypeReference<Map<String, Map<String, Integer>>>() {});
					List<DPB0201RespItem> list = new ArrayList<>();
					for(String targetUrlKey : map.keySet()) {
						DPB0201RespItem vo = new DPB0201RespItem();
						Map<String, Integer> typeMap = map.get(targetUrlKey);
						vo.setTargetUrl(targetUrlKey);
						if(typeMap.containsKey(WebsiteService.TYPE_REQ)) {
							vo.setReq(typeMap.get(WebsiteService.TYPE_REQ));
						}
						if(typeMap.containsKey(WebsiteService.TYPE_RESP)) {
							vo.setResp(typeMap.get(WebsiteService.TYPE_RESP));
						}
						list.add(vo);
					}
					resp.setItemList(list);
				}
			}
		}catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		return resp;
	}
	
	protected String getData(DPB0201Req req) { 
		if (TPILogger.lc != null) {
			RequireWebsiteTargetThroughputPacket packet = new RequireWebsiteTargetThroughputPacket();
			packet.websiteName = req.getWebsiteName();
			packet.minTimestampSec = (System.currentTimeMillis()/1000) - 5;//server在5秒內的資料有效
			TPILogger.lc.send(packet);
			synchronized (RequireWebsiteTargetThroughputPacket.waitKey) {
				try {
					RequireWebsiteTargetThroughputPacket.waitKey.wait(3000);
				} catch (InterruptedException e) {
	                Thread.currentThread().interrupt(); // 重新中斷當前的 thread
					logger.error(StackTraceUtil.logStackTrace(e));
				}
			}
			String strJson =(String)TPILogger.lc.paramObj.get(RequireWebsiteTargetThroughputPacket.sumWebsiteTargetThroughput);
			return strJson;
		}else {
			return null;
		}
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
