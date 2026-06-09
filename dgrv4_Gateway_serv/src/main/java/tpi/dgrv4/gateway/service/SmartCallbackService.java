package tpi.dgrv4.gateway.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrSmartAuthSessionDao;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.component.SmartGtwDataBridge;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART App Launch callback 處理。
 *
 * 背景：ssotoken IdP 認證完成後 redirect 回此服務，
 * 用 state 串接原本的 SMART 授權流程，驗證 dgRcode 取得使用者身份，
 * autoApprove=Y 時直接簽發 SMART auth code，N 時導向 consent 頁面。
 */
@Service
public class SmartCallbackService {

    private static final long AUTH_CODE_TTL_MS = 60_000L;
    private static final int AUTH_CODE_BYTES = 16;

    private final DgrSmartAuthSessionDao sessionDao;
    private final DgrGtwIdpAuthCodeDao idpAuthCodeDao;
    private final DgrSmartClientDao smartClientDao;
    private final SmartGtwDataBridge gtwDataBridge;

    @Autowired
    public SmartCallbackService(
            DgrSmartAuthSessionDao sessionDao,
            DgrGtwIdpAuthCodeDao idpAuthCodeDao,
            DgrSmartClientDao smartClientDao,
            SmartGtwDataBridge gtwDataBridge) {
        this.sessionDao = sessionDao;
        this.idpAuthCodeDao = idpAuthCodeDao;
        this.smartClientDao = smartClientDao;
        this.gtwDataBridge = gtwDataBridge;
    }

    /**
     * 處理 ssotoken callback。
     *
     * @param code  ssotoken 產生的 dgRcode
     * @param state SMART session 的 state
     * @return CallbackResult（Approved / NeedConsent / Error / FatalError）
     */
    @Transactional
    public CallbackResult processCallback(String code, String state) {
        // 1. 用 state 查 session
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByState(state);
        if (sessionOpt.isEmpty()) {
            TPILogger.tl.warn("[SmartCallback] session not found for state: " + state);
            return new CallbackResult.FatalError("Session not found for state: " + state);
        }

        DgrSmartAuthSession session = sessionOpt.get();

        // 2. 驗證 phase 必須為 PENDING
        if (!"PENDING".equals(session.getPhase())) {
            TPILogger.tl.warn("[SmartCallback] unexpected session phase: " + session.getPhase() + ", state: " + state);
            return new CallbackResult.FatalError("Session phase is not PENDING: " + session.getPhase());
        }

        String redirectUri = session.getRedirectUri();

        // 3. 用 dgRcode 查 IdP auth code
        DgrGtwIdpAuthCode idpAuthCode = idpAuthCodeDao.findFirstByAuthCode(code);
        if (idpAuthCode == null) {
            TPILogger.tl.warn("[SmartCallback] dgRcode not found: " + code);
            return new CallbackResult.Error(buildErrorRedirectUrl(redirectUri, "server_error",
                    "IdP auth code not found", state));
        }

        // 4. 更新 session phase=AUTHENTICATED，同時保存使用者身份
        session.setPhase("AUTHENTICATED");
        session.setUserName(idpAuthCode.getUserName());
        session.setUserAlias(idpAuthCode.getUserAlias());
        session.setUserEmail(idpAuthCode.getUserEmail());

        // 5. 查 DgrSmartClient 取得 autoApprove
        Optional<DgrSmartClient> smartClientOpt = smartClientDao.findByClientId(session.getClientId());
        if (smartClientOpt.isEmpty()) {
            TPILogger.tl.warn("[SmartCallback] smart client not found: " + session.getClientId());
            return new CallbackResult.Error(buildErrorRedirectUrl(redirectUri, "server_error",
                    "SMART client not found", state));
        }

        DgrSmartClient smartClient = smartClientOpt.get();

        // 6. 依 autoApprove 決定流程
        if ("Y".equals(smartClient.getAutoApprove()) && !requiresLaunchContextConsent(session)) {
            return approveDirectly(session, state);
        } else {
            sessionDao.save(session);
            return new CallbackResult.NeedConsent("/dgrv4/ac4/smart/consent?state=" + urlEncode(state));
        }
    }

    /**
     * 處理 IdP 認證失敗（ssotoken 帶 error 而非 code 回來）。
     */
    public CallbackResult handleIdpError(String state, String error) {
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByState(state);
        if (sessionOpt.isEmpty()) {
            return new CallbackResult.FatalError("Session not found for state: " + state);
        }
        DgrSmartAuthSession session = sessionOpt.get();
        String redirectUri = session.getRedirectUri();
        return new CallbackResult.Error(buildErrorRedirectUrl(redirectUri, error,
                "IdP authentication failed", state));
    }

    /**
     * 檢查 session 是否需要導向 consent 頁面進行 launch context 選擇。
     *
     * <p>需要 patient consent 的兩種情境（且 session.patientId 為空）：
     * <ol>
     *   <li>Standalone Launch：scope 含 {@code launch/patient}——App 明確要求挑選病患</li>
     *   <li>EHR Launch 未帶 patient：scope 含 patient-level scope（{@code patient/*.read} 等）
     *       ——授權伺服器必須讓使用者補選病患，否則 token 無法綁定 patient context</li>
     * </ol>
     *
     * <p>encounter 同理：scope 含 {@code launch/encounter} 且 session.encounterId 為空時需要 consent。
     *
     * @param session 目前的 SMART 授權 session
     * @return 若需要導向 consent 頁面選擇 launch context 則回傳 true
     */
    private boolean requiresLaunchContextConsent(DgrSmartAuthSession session) {
        List<String> requestedScope = session.getRequestedScope();
        if (requestedScope == null || requestedScope.isEmpty()) {
            return false;
        }
        Set<String> scopes = new HashSet<>(requestedScope);

        boolean hasPatientLevelScope = scopes.stream().anyMatch(s -> s.startsWith("patient/"));
        boolean needsPatient = (scopes.contains("launch/patient") || hasPatientLevelScope)
                && !StringUtils.hasText(session.getPatientId());

        boolean needsEncounter = scopes.contains("launch/encounter") && !StringUtils.hasText(session.getEncounterId());
        return needsPatient || needsEncounter;
    }

    private CallbackResult approveDirectly(DgrSmartAuthSession session, String state) {
        String authCode = generateAuthCode();
        long expire = System.currentTimeMillis() + AUTH_CODE_TTL_MS;

        session.setGrantedScope(new ArrayList<>(session.getRequestedScope()));
        session.setAuthCode(authCode);
        session.setAuthCodeExpire(expire);
        session.setPhase("APPROVED");

        sessionDao.save(session);

        gtwDataBridge.bridgeApprovedSession(session);

        String redirectUrl = session.getRedirectUri()
                + "?code=" + authCode + "&state=" + urlEncode(state);
        return new CallbackResult.Approved(redirectUrl);
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[AUTH_CODE_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildErrorRedirectUrl(String redirectUri, String error, String description, String state) {
        String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8);
        return redirectUri + "?error=" + error + "&error_description=" + encodedDesc
                + "&state=" + urlEncode(state);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ==================== Result types ====================

    public sealed interface CallbackResult {
        /** autoApprove=Y：成功簽發 auth code，redirect 回 App */
        record Approved(String redirectUrl) implements CallbackResult {}

        /** autoApprove=N：需要 consent，redirect 到 consent 頁面 */
        record NeedConsent(String consentUrl) implements CallbackResult {}

        /** 可 redirect 的錯誤（已知 redirectUri）：帶 error 參數 redirect 回 App */
        record Error(String redirectUrl) implements CallbackResult {}

        /** 無法 redirect 的錯誤（state 未知或 phase 不合法）：回傳錯誤頁 */
        record FatalError(String message) implements CallbackResult {}
    }
}
