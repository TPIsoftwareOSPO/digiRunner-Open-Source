package tpi.dgrv4.gateway.TCP.Packet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.gateway.constant.DbActionEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.gateway.vo.ActionRecord;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static tpi.dgrv4.codec.utils.CApiKeyUtils.CAPIKEY;
import static tpi.dgrv4.codec.utils.CApiKeyUtils.CUUID;
import static tpi.dgrv4.dpaa.service.DPB9937Service.*;

public class SendDbInfoMapPacket implements Packet_i {
    private static final String CHANGEDBINFO = "changeDbInfo";

    Map<String, String> data;

    public String dbInfoJson;
    public String cusIpPort;
    public String notifyActionChange;
    public String action;
    public String token;
    public String clientId;
    public String instanceId;
    public String operationType;
    public String mainJobId;
    public String targetDbConnect;

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
        ActionRecord record = null;
        String msg = "";
        lc.paramObj.put(TPILogger.DBINFOMAP_TITLE, data);
        Map<String, Object> map = (Map<String, Object>) lc.paramObj.get(TPILogger.DBINFOMAP_TITLE);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = null;
        String logJson = null;
        long applyTime = DateTimeUtil.now().getTime();
        try {
            jsonNode = om.readTree(dbInfoJson);
            map.put(TPILogger.DBINFO, jsonNode);
//            TPILogger.dbInfoMap = map;
            TPILogger.setDbInfoMap(map);
            Boolean success = false;
            if (DbActionEnum.APPLY_DB_CONNECT.name().equals(action)) {
                ChangeDbConnInfoService a = (ChangeDbConnInfoService) lc.paramObj.get(CHANGEDBINFO);
                if (a != null) {

                    JsonNode dbInfoRespJson = (JsonNode) map.get(TPILogger.DBINFO);
                    String username = "";
                    String password = "";

                    String connect = JsonNodeUtil.getNodeAsText(dbInfoRespJson, TARGET_DB_CONNECT);
                    if ("1".equals(connect)) {
                        username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, DB_USER_1);
                        password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, DB_MIMA_1);
                    }
                    if ("2".equals(connect)) {
                        username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, DB_USER_2);
                        password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, DB_MIMA_2);
                    }

                    // 變更連線
                    // change connection
                    try {
                        success = a.changeDbConnInfo(username, password);
                    } catch (Exception e) {
                        TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
                        msg = StackTraceUtil.logTpiShortStackTrace(e);
                        throw e;
                    }
                }
            }
            record = new ActionRecord(action, success, applyTime, instanceId, operationType, msg, mainJobId, targetDbConnect);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
            record = new ActionRecord(action, false, applyTime, instanceId, operationType, msg, mainJobId, targetDbConnect);

            throw TsmpDpAaRtnCode._1297.throwing();
        } finally {
            try {
                logJson = om.writeValueAsString(record);
                callCUS0003(jsonNode, logJson);
            } catch (Exception e) {
                TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
            }

        }
    }

    private void callCUS0003(JsonNode jsonNode, String logJson) throws IOException {
        Map<String, String> header = new HashMap<>();
        String uuidForCapiKey = UUID.randomUUID().toString();
        String cuuid = uuidForCapiKey.toUpperCase();
        String capikey = CApiKeyUtils.signCKey(cuuid);
        header.put("Accept", "application/json");
        header.put("Content-Type", "application/json");
        header.put(CUUID, cuuid);
        header.put(CAPIKEY, capikey);
        header.put("Authorization", token);
        //TODO 記錄操作日誌
        if (logJson != null)
            header.put("logJson", Base64Util.base64Encode(logJson.getBytes()));
        HttpRespData dbInfoResp = null;

        String url = getApiUrl();

        dbInfoResp = HttpUtil.httpReqByRawData(url, "POST", DpaaHttpUtil.toReqPayloadJson(jsonNode, clientId), header,
                false);


        if (dbInfoResp.statusCode <= 0 || dbInfoResp.statusCode >= 400) {
            TPILogger.tl.debug("call CUS0003 error : \n " + dbInfoResp.getLogStr());
            throw TsmpDpAaRtnCode._1497.throwing(String.valueOf(dbInfoResp.statusCode), dbInfoResp.respStr);

        }

        ObjectMapper om = new ObjectMapper();
        JsonNode dbInfoRespJson = om.readTree(dbInfoResp.respStr);

        String dgRRtnCode = dbInfoRespJson.get("ResHeader").get("rtnCode").asText();

        if (!"1100".equals(dgRRtnCode)) {
            TsmpDpAaRtnCode._1497.throwing(String.valueOf(dbInfoResp.statusCode), dbInfoResp.respStr);
        }

        TPILogger.tl.debug("call CUS0003 success : \n " + url);
    }

    private String getApiUrl() {
        return TPILogger.cusSchemeForBroadcast + "://" + TPILogger.cusIpPortForBroadcast + notifyActionChange;

    }
}
