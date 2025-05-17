package tpi.dgrv4.gateway.TCP.Packet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.gateway.constant.DbActionEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class SendDbInfoMapPacket implements Packet_i {
	private static final String CHANGEDBINFO = "changeDbInfo";

	Map<String, String> data;
	public String dbInfoJson;
	public String cusIpPort;
	public String notifyActionChange;
	public String action;
	public String token;
	public String clientId;

	@Override
	public void runOnServer(LinkerServer ls) {
		if (ls.paramObj.containsKey(TPILogger.DBINFOMAP_TITLE) == false) {
			ls.paramObj.put(TPILogger.DBINFOMAP_TITLE, new HashMap<String, String>());
		}
		data = (Map<String, String>) ls.paramObj.get(TPILogger.DBINFOMAP_TITLE);

		data.put(TPILogger.DBINFO, dbInfoJson);

		if (ls.paramObj.containsKey(CHANGEDBINFO) == false && DbActionEnum.APPLY_DB_CONNECT.name().equals(action)) {
			ls.paramObj.put(CHANGEDBINFO, new ChangeDbConnInfoService(null));
		}

		CommunicationServer.cs.sendToAll(ls, this);
	}

	@Override
	public void runOnClient(LinkerClient lc) {

		lc.paramObj.put(TPILogger.DBINFOMAP_TITLE, data);
		Map<String, Object> map = (Map<String, Object>) lc.paramObj.get(TPILogger.DBINFOMAP_TITLE);

		ObjectMapper om = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = om.readTree(dbInfoJson);
			map.put(TPILogger.DBINFO, jsonNode);
			TPILogger.dbInfoMap = map;
			if (DbActionEnum.APPLY_DB_CONNECT.name().equals(action)) {
				ChangeDbConnInfoService a = (ChangeDbConnInfoService) lc.paramObj.get(CHANGEDBINFO);
				if (a != null) {

					JsonNode dbInfoRespJson = (JsonNode) map.get(TPILogger.DBINFO);
					String username = "";
					String password = "";

					String connect = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "targetDbConnect");
					if ("1".equals(connect)) {
						username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername1");
						password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima1");
					}
					if ("2".equals(connect)) {
						username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername2");
						password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima2");
					}
					// 變更連線
					a.changeDbConnInfo(username, password);
				}
			}
			callCUS0003(jsonNode);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void callCUS0003(JsonNode jsonNode) throws IOException {
		Map<String, String> header = new HashMap<>();
		String uuidForCapiKey = UUID.randomUUID().toString();
		String cuuid = uuidForCapiKey.toUpperCase();
		String capikey = CApiKeyUtils.signCKey(cuuid);
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("cuuid", cuuid);
		header.put("capi-key", capikey);
		header.put("Authorization", token);
		HttpRespData dbInfoResp = null;

		String url = getApiUrl();

		dbInfoResp = HttpUtil.httpReqByRawData(url, "POST", DpaaHttpUtil.toReqPayloadJson(jsonNode, clientId), header,
				false);
		
		
		if (dbInfoResp.statusCode <= 0 || dbInfoResp.statusCode >= 400) {
			TPILogger.tl.debug("call CUS0003 error :　\n  "+dbInfoResp.getLogStr());
			throw TsmpDpAaRtnCode._1497.throwing(String.valueOf(dbInfoResp.statusCode), dbInfoResp.respStr);
			
		}
		
		ObjectMapper om = new ObjectMapper();
		JsonNode dbInfoRespJson = om.readTree(dbInfoResp.respStr);
		
		String dgRRtnCode = dbInfoRespJson.get("ResHeader").get("rtnCode").asText();
		
		if (!"1100".equals(dgRRtnCode)) {
			TsmpDpAaRtnCode._1497.throwing(String.valueOf(dbInfoResp.statusCode), dbInfoResp.respStr);
		}
		
		TPILogger.tl.debug("call CUS0003 success :　\n  " + url);
	}

	private String getApiUrl() {
		return "https://" + TPILogger.cusIpPortForBroadcast + notifyActionChange;

	}
}
