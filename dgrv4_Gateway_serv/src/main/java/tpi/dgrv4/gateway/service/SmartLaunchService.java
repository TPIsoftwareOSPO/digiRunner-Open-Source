package tpi.dgrv4.gateway.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.entity.DgrSmartLaunchContext;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.entity.repository.DgrSmartLaunchContextDao;
import tpi.dgrv4.gateway.component.SmartClientAuthenticator;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * EHR Launch Registration 服務。
 *
 * 背景：EHR 系統在啟動 SMART App 前，呼叫此服務預先登記 launch context
 * （patient_id、encounter_id），取得一次性的不透明 launch token。
 * SMART App 啟動時以此 token 發起授權流程，授權端點讀取對應的 context。
 *
 * 設計決策：
 * - client 認證委派給 SmartClientAuthenticator，認證失敗直接拋出 SmartAuthenticationException
 * - launchMode 驗證：只允許 "ehr" 或 "both"，"standalone" 拒絕
 * - patient_id 和 encounter_id 皆為選填；未提供時，授權流程可透過 consent 頁面補選
 * - launch token 效期 5 分鐘
 *
 * 規格依據：HL7 SMART App Launch STU2.2 §EHR Launch
 */
@Service
public class SmartLaunchService {

    /** Launch token 效期（5 分鐘，毫秒） */
    private static final long LAUNCH_TOKEN_EXPIRE_MS = 5 * 60 * 1000L;

    /** Launch 端點 path（用於組裝 tokenEndpointUrl 傳給認證元件） */
    private static final String LAUNCH_ENDPOINT_PATH = "/dgrv4/ssotoken/smart/launch";

    private final SmartClientAuthenticator authenticator;
    private final DgrSmartClientDao smartClientDao;
    private final DgrSmartLaunchContextDao launchContextDao;

    @Autowired
    public SmartLaunchService(SmartClientAuthenticator authenticator,
            DgrSmartClientDao smartClientDao,
            DgrSmartLaunchContextDao launchContextDao) {
        this.authenticator = authenticator;
        this.smartClientDao = smartClientDao;
        this.launchContextDao = launchContextDao;
    }

    /**
     * 登記 EHR Launch 上下文，回傳不透明 launch token。
     *
     * 流程：
     * 1. 認證 client（SmartClientAuthenticator）
     * 2. 查詢 client 設定，驗證 launchMode 允許 EHR Launch
     * 3. 讀取 patient_id / encounter_id（選填）
     * 4. 產生 UUID token，寫入 DgrSmartLaunchContext，expire_at = now + 5 分鐘
     * 5. 回傳 {"launch": "<token>"}
     *
     * @param authorizationHeader HTTP Authorization header（可為 null）
     * @param params              form body 參數（含 patient_id、encounter_id）
     * @param baseUrl             請求來源 base URL（用於組裝 launch endpoint URL）
     * @return 成功回應 Map，包含 "launch" key
     * @throws SmartClientAuthenticator.SmartAuthenticationException client 認證失敗
     * @throws SmartLaunchException                                  launchMode 不符或缺少必要參數
     */
    public Map<String, String> registerLaunch(String authorizationHeader,
            Map<String, String> params, String baseUrl) {

        // 1. 認證 client（失敗則 SmartAuthenticationException 向上傳遞）
        String launchEndpointUrl = baseUrl + LAUNCH_ENDPOINT_PATH;
        String clientId = authenticator.authenticate(authorizationHeader, params, launchEndpointUrl);
        TPILogger.tl.info("[SMART Launch] client authenticated: " + clientId);

        // 2. 查詢 client 設定，驗證 launchMode
        DgrSmartClient client = smartClientDao.findByClientId(clientId)
            .orElseThrow(() -> new SmartLaunchException("invalid_client",
                "SMART client not found: " + clientId));

        validateLaunchMode(client);

        // 3. 讀取 patient / encounter / provider（皆為選填，未提供時授權流程可透過 consent 補選）
        // patient 支援逗號分隔：1 個進 patientId，多個進 patientIds（候選清單）
        String patientParam = params.get("patient");
        String encounterId = params.get("encounter");
        String providerIds = params.get("provider");
        // fhirUser resource type：'patient' or 'practitioner'，預設 practitioner
        String userType = params.get("user_type");
        if (!"patient".equalsIgnoreCase(userType) && !"practitioner".equalsIgnoreCase(userType)) {
            userType = "practitioner";
        } else {
            userType = userType.toLowerCase();
        }

        String patientIdSingle = null;
        List<String> patientIdsMulti = null;
        if (StringUtils.hasText(patientParam)) {
            List<String> parsed = Arrays.stream(patientParam.trim().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
            if (parsed.size() == 1) {
                patientIdSingle = parsed.get(0);
            } else if (parsed.size() > 1) {
                patientIdsMulti = parsed;
            }
        }

        // 4. 產生 UUID launch token，寫入 DgrSmartLaunchContext
        String launchToken = UUID.randomUUID().toString();
        DgrSmartLaunchContext context = new DgrSmartLaunchContext();
        context.setLaunchToken(launchToken);
        context.setClientId(clientId);
        context.setPatientId(patientIdSingle);
        context.setPatientIds(patientIdsMulti);
        context.setEncounterId(StringUtils.hasText(encounterId) ? encounterId : null);
        context.setProviderIds(StringUtils.hasText(providerIds)
                ? Arrays.stream(providerIds.trim().split(",")).map(String::trim).filter(StringUtils::hasText).toList()
                : null);
        context.setFhirUserType(userType);
        context.setConsumed(Boolean.FALSE);
        context.setExpireAt(System.currentTimeMillis() + LAUNCH_TOKEN_EXPIRE_MS);

        launchContextDao.save(context);
        // 僅記前 8 字元，避免完整 token 出現在日誌（安全防護）
        String tokenPrefix = launchToken.length() > 8 ? launchToken.substring(0, 8) : launchToken;
        TPILogger.tl.debug("[SMART Launch] launch context registered, token prefix: " + tokenPrefix);

        // 5. 回傳
        Map<String, String> response = new LinkedHashMap<>();
        response.put("launch", launchToken);
        return response;
    }

    /**
     * 驗證 client 的 launchMode 是否允許 EHR Launch。
     * 只有 "ehr" 或 "both" 才允許；"standalone" 拒絕。
     */
    private void validateLaunchMode(DgrSmartClient client) {
        String launchMode = client.getLaunchMode();
        if ("standalone".equals(launchMode)) {
            throw new SmartLaunchException("unauthorized_client",
                "Client launch mode does not allow EHR launch");
        }
        // null 或其他未知值也視為不支援
        if (!"ehr".equals(launchMode) && !"both".equals(launchMode)) {
            throw new SmartLaunchException("unauthorized_client",
                "Client launch mode does not allow EHR launch");
        }
    }

    // ==================== 例外 ====================

    /**
     * EHR Launch Registration 業務例外。
     * error 和 description 對應 RFC 6749 §5.2 的 error response。
     */
    public static class SmartLaunchException extends RuntimeException {
        private final String error;
        private final String description;

        public SmartLaunchException(String error, String description) {
            super(description);
            this.error = error;
            this.description = description;
        }

        public String getError() {
            return error;
        }

        public String getDescription() {
            return description;
        }
    }
}
