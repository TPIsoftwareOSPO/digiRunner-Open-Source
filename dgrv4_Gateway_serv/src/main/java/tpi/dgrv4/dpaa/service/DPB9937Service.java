package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9937Req;
import tpi.dgrv4.gateway.TCP.Packet.SendDbInfoMapPacket;
import tpi.dgrv4.gateway.config.CustomDataSourceConfig;
import tpi.dgrv4.gateway.constant.DbConnectionMode;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@ConditionalOnProperty(name = "db.connection.mode", havingValue = "api")
@Service
public class DPB9937Service {

    @Value(value = "${cus.api.notifyActionChange}")
    private String notifyActionChange;
    @Value(value = "${digi.instance.id}")
    private String instanceId;
    private CustomDataSourceConfig customDataSourceConfig;
    private CApiKeyService cApiKeyService;

    private String cusIpPort;
    public static final String DB_USER_1 = "dbUsername1";
    public static final String DB_USER_2 = "dbUsername2";
    public static final String DB_MIMA_1 = "dbMima1";
    public static final String DB_MIMA_2 = "dbMima2";
    public static final String TARGET_DB_CONNECT = "targetDbConnect";

    @Autowired
    public DPB9937Service(CustomDataSourceConfig customDataSourceConfig, CApiKeyService cApiKeyService) {
        super();
        this.customDataSourceConfig = customDataSourceConfig;
        this.cApiKeyService = cApiKeyService;
    }

    public void getDbInfo(TsmpAuthorization auth, HttpHeaders headers, DPB9937Req req) {
        // 使用 CApikey 做安全驗證
        // use CApikey for security
        getCApiKeyService().verifyCApiKey(headers, false, false);
        String authorization = headers.getFirst("authorization");
        getDbInfo(auth, authorization, req);
    }

    public void getDbInfo(TsmpAuthorization auth, String authorization, DPB9937Req req) {
        try {


            String mode = getMode();
            // 只有在api mode 情況下才能使用
            // only in api mode
            if (!DbConnectionMode.API.equalsIgnoreCase(mode)) {
                TPILogger.tl.error(" mode: " + mode);
                TPILogger.dbConnByApi = false;
                // 沒有權限
                // no permission
                throw TsmpDpAaRtnCode._1219.throwing();

            }
            cusIpPort = getCusIpPort();

            TPILogger.dbConnByApi = true;
            // 從 API取資訊
            // get info from API
            Map<String, Object> map = new HashMap<>();
            Object item = req.getdPB9937ReqItem();
            ObjectMapper om = new ObjectMapper();
            String jsonString = om.writeValueAsString(item);

            JsonNode jsonNode = om.readTree(jsonString);
            String action = jsonNode.get("action").asText();
            String operationType = jsonNode.get("operationType").asText();
            String mainJobId = jsonNode.get("mainJobId").asText();
            String targetDbConnect = jsonNode.get("targetDbConnect").asText();
            map.put(TPILogger.DBINFO, jsonNode);
            sendPacket(jsonString, authorization, auth.getClientId(), action, map, operationType, mainJobId, targetDbConnect);
            TPILogger.setDbInfoMap(map);
//			TPILogger.dbInfoMap = map;


        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    protected void sendPacket(String jsonString, String token, String clientId, String action, Map<String, Object> map, String operationType, String mainJobId, String targetDbConnect) {
        SendDbInfoMapPacket dbInfoPacket = new SendDbInfoMapPacket();

        dbInfoPacket.dbInfoJson = jsonString;
        dbInfoPacket.token = token;
        dbInfoPacket.clientId = clientId;
        dbInfoPacket.cusIpPort = cusIpPort;
        dbInfoPacket.notifyActionChange = notifyActionChange;
        dbInfoPacket.instanceId = instanceId;
        dbInfoPacket.action = action;
        dbInfoPacket.operationType = operationType;
        dbInfoPacket.mainJobId = mainJobId;
        dbInfoPacket.targetDbConnect = targetDbConnect;
        // 傳送給給所有節點
        // send to all nodes
        TPILogger.lc.send(dbInfoPacket);
        TPILogger.lc.paramObj.put(TPILogger.DBINFOMAP_TITLE, map);
    }

    protected String getCusIpPort() {
        return getCustomDataSourceConfig().getCusIpPort();
    }

    protected String getMode() {
        return getCustomDataSourceConfig().getDbconnectionMode();
    }

    protected CustomDataSourceConfig getCustomDataSourceConfig() {
        return customDataSourceConfig;
    }

    protected CApiKeyService getCApiKeyService() {
        return cApiKeyService;
    }
}
