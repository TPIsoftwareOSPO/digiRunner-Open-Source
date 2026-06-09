package tpi.dgrv4.gateway.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrSmartAuthSessionDao;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.SmartGtwDataBridge;
import tpi.dgrv4.gateway.component.SmartScopeDescriber;
import tpi.dgrv4.gateway.component.SmartScopeDescriber.ScopeDescription;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART App Launch Consent 處理。
 *
 * 背景：autoApprove=N 的 SMART client 在使用者身份驗證完成後，
 * 需要進入 consent 頁面讓使用者選擇批准（approve）或拒絕（deny）。
 * 此 service 處理使用者的 consent 決策，approve 產生 auth code redirect，
 * deny 回傳 access_denied redirect。
 * 並提供 getConsentInfo 供前端 consent 頁面取得顯示所需資訊。
 */
@Service
public class SmartConsentService {

    private static final long AUTH_CODE_TTL_MS = 60_000L;
    private static final int AUTH_CODE_BYTES = 16;

    private final DgrSmartAuthSessionDao sessionDao;
    private final TsmpClientDao tsmpClientDao;
    private final DgrSmartClientDao smartClientDao;
    private final SmartScopeDescriber scopeDescriber;
    private final SmartGtwDataBridge gtwDataBridge;

    @Autowired
    public SmartConsentService(DgrSmartAuthSessionDao sessionDao,
            TsmpClientDao tsmpClientDao,
            DgrSmartClientDao smartClientDao,
            SmartScopeDescriber scopeDescriber,
            SmartGtwDataBridge gtwDataBridge) {
        this.sessionDao = sessionDao;
        this.tsmpClientDao = tsmpClientDao;
        this.smartClientDao = smartClientDao;
        this.scopeDescriber = scopeDescriber;
        this.gtwDataBridge = gtwDataBridge;
    }

    /**
     * 處理使用者批准 consent。
     *
     * @param state          SMART session 的 state
     * @param approvedScopes 使用者選擇批准的 scope（空格分隔，需為 requestedScope 子集）
     * @param patientId      病患 ID；需要 patient context 時必填
     * @param encounterId    就醫 ID；grantedScope 含 launch/encounter 時必填，否則忽略
     * @return ConsentResult（Redirect 或 FatalError）
     */
    @Transactional
    public ConsentResult approve(String state, String approvedScopes,
            String patientId, String encounterId, String fhirUser) {
        // 1. 用 state 查 session
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByState(state);
        if (sessionOpt.isEmpty()) {
            TPILogger.tl.warn("[SmartConsent] session not found for state: " + state);
            return new ConsentResult.FatalError("Session not found for state: " + state);
        }

        DgrSmartAuthSession session = sessionOpt.get();

        // 2. 驗證 phase 必須為 AUTHENTICATED
        if (!"AUTHENTICATED".equals(session.getPhase())) {
            TPILogger.tl.warn("[SmartConsent] unexpected session phase: " + session.getPhase() + ", state: " + state);
            return new ConsentResult.FatalError("Session phase is not AUTHENTICATED: " + session.getPhase());
        }

        // 3. 驗證 approvedScopes 是 requestedScope 的子集
        if (!isScopeSubset(approvedScopes, new HashSet<>(session.getRequestedScope()))) {
            TPILogger.tl.warn("[SmartConsent] approvedScopes exceed requestedScope. state: " + state
                    + ", approved: " + approvedScopes + ", requested: " + session.getRequestedScope());
            String redirectUrl = session.getRedirectUri() + "?error=invalid_scope&state=" + urlEncode(state);
            return new ConsentResult.Redirect(redirectUrl);
        }

        // 4. 驗證 launch context
        Set<String> grantedTokens = toScopeTokenSet(approvedScopes);
        boolean needsPatient = requiresPatientContext(grantedTokens, session);
        boolean needsEncounter = grantedTokens.contains("launch/encounter") && !StringUtils.hasText(session.getEncounterId());
        boolean needsProvider = requiresProviderSelection(grantedTokens, session);

        if (needsPatient && !StringUtils.hasText(patientId)) {
            TPILogger.tl.warn("[SmartConsent] patient context required but patientId is missing. state: " + state);
            String redirectUrl = session.getRedirectUri() + "?error=invalid_request"
                    + "&error_description=" + urlEncode("Patient ID is required when patient-level scopes are granted")
                    + "&state=" + urlEncode(state);
            return new ConsentResult.Redirect(redirectUrl);
        }
        // 若有 patient 候選清單，使用者選擇必須屬於該清單
        if (needsPatient && StringUtils.hasText(patientId)
                && session.getPatientCandidates() != null && !session.getPatientCandidates().isEmpty()
                && !session.getPatientCandidates().contains(patientId)) {
            TPILogger.tl.warn("[SmartConsent] patientId not in candidates. state: " + state + ", choice: " + patientId);
            String redirectUrl = session.getRedirectUri() + "?error=invalid_request"
                    + "&error_description=" + urlEncode("Selected patient is not among allowed candidates")
                    + "&state=" + urlEncode(state);
            return new ConsentResult.Redirect(redirectUrl);
        }
        if (needsEncounter && !StringUtils.hasText(encounterId)) {
            TPILogger.tl.warn("[SmartConsent] launch/encounter scope granted but encounterId is missing. state: " + state);
            String redirectUrl = session.getRedirectUri() + "?error=invalid_request"
                    + "&error_description=" + urlEncode("Encounter ID is required when launch/encounter scope is granted")
                    + "&state=" + urlEncode(state);
            return new ConsentResult.Redirect(redirectUrl);
        }
        if (needsProvider) {
            if (!StringUtils.hasText(fhirUser)) {
                TPILogger.tl.warn("[SmartConsent] fhirUser required but missing. state: " + state);
                String redirectUrl = session.getRedirectUri() + "?error=invalid_request"
                        + "&error_description=" + urlEncode("fhirUser selection is required when fhirUser scope is granted")
                        + "&state=" + urlEncode(state);
                return new ConsentResult.Redirect(redirectUrl);
            }
            if (!isValidFhirUserChoice(fhirUser, session.getProviderCandidates() != null ? session.getProviderCandidates() : List.of())) {
                TPILogger.tl.warn("[SmartConsent] fhirUser not in candidates. state: " + state + ", choice: " + fhirUser);
                String redirectUrl = session.getRedirectUri() + "?error=invalid_request"
                        + "&error_description=" + urlEncode("Selected fhirUser is not among allowed candidates")
                        + "&state=" + urlEncode(state);
                return new ConsentResult.Redirect(redirectUrl);
            }
        }

        // 5. 設定 grantedScope = approvedScopes
        // 6. 產生 auth code（22 字元 URL-safe Base64、60 秒效期）
        String authCode = generateAuthCode();
        long expire = System.currentTimeMillis() + AUTH_CODE_TTL_MS;

        session.setGrantedScope(List.of(approvedScopes.trim().split("\\s+")));
        session.setAuthCode(authCode);
        session.setAuthCodeExpire(expire);

        // 7. 寫入 launch context
        if (needsPatient) {
            session.setPatientId(patientId);
        }
        if (needsEncounter) {
            session.setEncounterId(encounterId);
        }
        if (needsProvider) {
            session.setFhirUser(fhirUser);
        }

        // 8. Patient Portal context：fhirUser 自動由 patientId 推導
        // 觸發條件：scope 含 fhirUser、launch 是 Patient 型、session 尚未有 fhirUser、patient 已決定
        if (grantedTokens.contains("fhirUser")
                && !StringUtils.hasText(session.getFhirUser())
                && "Patient".equals(determineFhirUserResourceType(session))
                && StringUtils.hasText(session.getPatientId())) {
            session.setFhirUser("Patient/" + session.getPatientId());
        }

        // 8. 更新 phase 為 APPROVED
        session.setPhase("APPROVED");

        sessionDao.save(session);

        gtwDataBridge.bridgeApprovedSession(session);

        String redirectUrl = session.getRedirectUri()
                + "?code=" + authCode + "&state=" + urlEncode(state);
        return new ConsentResult.Redirect(redirectUrl);
    }

    /**
     * 處理使用者拒絕 consent。
     *
     * @param state SMART session 的 state
     * @return ConsentResult（Redirect with access_denied 或 FatalError）
     */
    @Transactional
    public ConsentResult deny(String state) {
        // 1. 用 state 查 session
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByState(state);
        if (sessionOpt.isEmpty()) {
            TPILogger.tl.warn("[SmartConsent] session not found for state: " + state);
            return new ConsentResult.FatalError("Session not found for state: " + state);
        }

        DgrSmartAuthSession session = sessionOpt.get();

        // 2. 驗證 phase 必須為 AUTHENTICATED
        if (!"AUTHENTICATED".equals(session.getPhase())) {
            TPILogger.tl.warn("[SmartConsent] unexpected session phase: " + session.getPhase() + ", state: " + state);
            return new ConsentResult.FatalError("Session phase is not AUTHENTICATED: " + session.getPhase());
        }

        // 3. 更新 phase 為 DENIED
        session.setPhase("DENIED");
        sessionDao.save(session);

        String redirectUrl = session.getRedirectUri()
                + "?error=access_denied&state=" + urlEncode(state);
        return new ConsentResult.Redirect(redirectUrl);
    }

    /**
     * 判斷是否需要 patient context。
     * 兩種情境（且 session 尚無 patientId）：
     * 1. scope 含 launch/patient（Standalone Launch 明確要求挑選病患）
     * 2. scope 含 patient/ 開頭的 FHIR scope（如 patient/*.read）——沒有 patient 就無法綁定 compartment
     */
    private boolean requiresPatientContext(Set<String> scopes, DgrSmartAuthSession session) {
        if (StringUtils.hasText(session.getPatientId())) {
            return false;
        }
        boolean hasPatientLevelScope = scopes.stream().anyMatch(s -> s.startsWith("patient/"));
        return scopes.contains("launch/patient") || hasPatientLevelScope;
    }

    /**
     * 判斷是否需要 fhirUser 選擇（僅 Provider 端 launch 才需要獨立挑選）。
     *
     * Patient Portal Launch（requested scope 含 launch/patient）的 fhirUser 是 Patient，
     * 跟病人 pick 對應同一筆 Patient，自動推導即可，不開獨立挑選頁。
     */
    private boolean requiresProviderSelection(Set<String> scopes, DgrSmartAuthSession session) {
        if (!scopes.contains("fhirUser")) {
            return false;
        }
        if (StringUtils.hasText(session.getFhirUser())) {
            return false;
        }
        // Patient Portal context：fhirUser = Patient/{patientId}，由 patient pick 帶動
        if ("Patient".equals(determineFhirUserResourceType(session))) {
            return false;
        }
        return true;
    }

    /**
     * 判斷本次 launch 的 fhirUser 是 Patient 還是 Practitioner。
     *
     * 規則（優先序）：
     * 1. session.fhirUserType（由 launch 註冊明確指定）→ 'patient' → Patient
     * 2. requested scope 含 launch/patient（standalone Patient Portal）→ Patient
     * 3. 其他 → Practitioner（預設）
     *
     * 第 1 條對應官方 launcher 在 iss URL 編碼 launch type 的做法；
     * 第 2 條對應 standalone Patient Portal Launch（沒有 launch 註冊步驟）。
     */
    private String determineFhirUserResourceType(DgrSmartAuthSession session) {
        if ("patient".equalsIgnoreCase(session.getFhirUserType())) {
            return "Patient";
        }
        List<String> scope = session.getRequestedScope();
        if (scope == null || scope.isEmpty()) {
            return "Practitioner";
        }
        return scope.contains("launch/patient") ? "Patient" : "Practitioner";
    }

    private List<String> parsePatientCandidates(List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates;
    }

    private List<String> parseProviderCandidates(List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(StringUtils::hasText)
                .map(id -> "Practitioner/" + id)
                .toList();
    }

    /**
     * 驗證使用者選的 fhirUser 是否合法：
     * - 有候選清單：必須屬於候選
     * - 無候選清單：必須符合 FHIR Reference 格式（{ResourceType}/{id}）
     */
    private boolean isValidFhirUserChoice(String fhirUser, List<String> candidates) {
        if (!StringUtils.hasText(fhirUser)) {
            return false;
        }
        if (candidates != null && !candidates.isEmpty()) {
            return parseProviderCandidates(candidates).contains(fhirUser);
        }
        return fhirUser.matches("^(Practitioner|Patient)/[A-Za-z0-9\\-\\.]{1,64}$");
    }

    /**
     * 將空格分隔的 scope 字串解析為 token set。
     */
    private Set<String> toScopeTokenSet(String scopeString) {
        return new HashSet<>(Arrays.asList(scopeString.split("\\s+")));
    }

    private boolean isScopeSubset(String approvedScopes, Set<String> requestedScope) {
        for (String scope : approvedScopes.split("\\s+")) {
            if (!scope.isBlank() && !requestedScope.contains(scope)) {
                return false;
            }
        }
        return true;
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[AUTH_CODE_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 取得 consent 頁面所需的顯示資訊。
     *
     * @param state SMART session 的 state
     * @return ConsentInfo（clientName、clientId 與 scope 描述清單）
     * @throws IllegalArgumentException 若 state 不存在
     * @throws IllegalStateException    若 session phase 非 AUTHENTICATED
     */
    public ConsentInfo getConsentInfo(String state) {
        // 1. 查詢 session
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByState(state);
        if (sessionOpt.isEmpty()) {
            TPILogger.tl.warn("[SmartConsent] getConsentInfo: session not found for state: " + state);
            throw new IllegalArgumentException("Session not found for state: " + state);
        }

        DgrSmartAuthSession session = sessionOpt.get();

        // 2. phase 必須為 AUTHENTICATED，才是等待 consent 的狀態
        if (!"AUTHENTICATED".equals(session.getPhase())) {
            TPILogger.tl.warn("[SmartConsent] getConsentInfo: unexpected phase: "
                    + session.getPhase() + ", state: " + state);
            throw new IllegalStateException("Session phase is not AUTHENTICATED: " + session.getPhase());
        }

        // 3. 取得 clientName；TsmpClient 查無資料時 fallback 為 clientId
        String clientId = session.getClientId();
        String clientName = clientId;
        TsmpClient tsmpClient = tsmpClientDao.findFirstByClientId(clientId);
        if (tsmpClient != null && StringUtils.hasText(tsmpClient.getClientName())) {
            clientName = tsmpClient.getClientName();
        }

        // 4. 解析 scope 描述
        List<ScopeDescription> scopes = scopeDescriber.describe(String.join(" ", session.getRequestedScope()));

        // 5. 判斷是否需要病患 / 就醫上下文選擇
        Set<String> scopeTokens = new HashSet<>(session.getRequestedScope());
        boolean needsPatientSelection = requiresPatientContext(scopeTokens, session);
        boolean needsEncounterSelection = scopeTokens.contains("launch/encounter") && !StringUtils.hasText(session.getEncounterId());

        // 6. 判斷是否需要 fhirUser 選擇（候選 1 個時 authorize 階段已自動帶入）
        boolean needsProviderSelection = requiresProviderSelection(scopeTokens, session);
        List<String> providerCandidates = parseProviderCandidates(session.getProviderCandidates());

        // 7. 讀取 SMART client 的 autoApprove 設定（決定前端是否顯示 consent UI）
        boolean autoApprove = smartClientDao.findByClientId(clientId)
                .map(DgrSmartClient::getAutoApprove)
                .map("Y"::equals)
                .orElse(false);

        List<String> patientCandidates = parsePatientCandidates(session.getPatientCandidates());

        return new ConsentInfo(clientId, clientName, scopes,
                needsPatientSelection, needsEncounterSelection, needsProviderSelection,
                providerCandidates, session.getFhirUser(), autoApprove,
                patientCandidates,
                session.getWebsiteName(), session.getFhirBasePath());
    }

    // ==================== Result types ====================

    public sealed interface ConsentResult {
        /** 可 redirect 的結果（成功簽發 auth code 或錯誤 redirect） */
        record Redirect(String url) implements ConsentResult {}

        /** 無法 redirect 的錯誤（state 未知或 phase 不合法）：回傳錯誤頁 */
        record FatalError(String message) implements ConsentResult {}
    }

    /**
     * consent-info API 的回傳結構。
     *
     * @param clientId                SMART client ID
     * @param clientName              應用程式顯示名稱（查無時 fallback 為 clientId）
     * @param scopes                  scope 描述清單
     * @param needsPatientSelection   需要選擇病患時為 true
     * @param needsEncounterSelection 需要選擇就醫紀錄時為 true
     * @param needsProviderSelection  需要選擇 fhirUser 時為 true（候選 0 或 2+ 時）
     * @param providerCandidates      Provider 候選清單，FHIR Reference 格式（如 Practitioner/123）
     * @param fhirUser                已選定的 fhirUser（候選 1 個時 authorize 已自動帶入）
     * @param websiteName             FHIR Proxy 名稱（供前端組裝 Patient 查詢 URL）
     * @param fhirBasePath            FHIR base path（如 /fhir/r4）
     */
    public record ConsentInfo(
            String clientId,
            String clientName,
            List<ScopeDescription> scopes,
            boolean needsPatientSelection,
            boolean needsEncounterSelection,
            boolean needsProviderSelection,
            List<String> providerCandidates,
            String fhirUser,
            boolean autoApprove,
            List<String> patientCandidates,
            String websiteName,
            String fhirBasePath) {}
}
