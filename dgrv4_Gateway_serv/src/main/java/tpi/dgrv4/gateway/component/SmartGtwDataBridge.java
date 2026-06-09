package tpi.dgrv4.gateway.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.TsmpAuthCodeStatus2;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthD;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthCodeDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthDDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART → OAuthTokenService 資料橋接。
 *
 * 背景：OAuthTokenService 的 authorization_code flow 預期從
 * DGR_GTW_IDP_AUTH_M/D/AUTH_CODE 三張表讀取資料。SMART 授權流程的資料
 * 存在 DGR_SMART_AUTH_SESSION，兩者欄位結構不同。
 *
 * 本元件在 SMART auth code 產生時（autoApprove 或 consent approve），
 * 將 session 資料同步寫入 OAuthTokenService 預期的三張表，
 * 使 OAuthTokenService 不需任何修改即可處理 SMART token 請求。
 */
@Component
public class SmartGtwDataBridge {

    private static final String IDP_TYPE_SMART = "SMART";

    private final DgrGtwIdpAuthMDao authMDao;
    private final DgrGtwIdpAuthDDao authDDao;
    private final DgrGtwIdpAuthCodeDao authCodeDao;

    @Autowired
    public SmartGtwDataBridge(DgrGtwIdpAuthMDao authMDao,
            DgrGtwIdpAuthDDao authDDao,
            DgrGtwIdpAuthCodeDao authCodeDao) {
        this.authMDao = authMDao;
        this.authDDao = authDDao;
        this.authCodeDao = authCodeDao;
    }

    /**
     * 將已核准的 SMART session 橋接到 OAuthTokenService 預期的 GTW 表。
     *
     * 前置條件：session 必須已設定 authCode、authCodeExpire、grantedScope、使用者身份。
     * 呼叫時機：SmartCallbackService.approveDirectly() 或 SmartConsentService.approve() 之後。
     *
     * 寫入三張表：
     * 1. DGR_GTW_IDP_AUTH_M — 授權請求 master（state、client、redirect_uri、PKCE）
     * 2. DGR_GTW_IDP_AUTH_D — 授權 scope detail（每個 scope 一行）
     * 3. DGR_GTW_IDP_AUTH_CODE — authorization code 紀錄（code、使用者身份、過期時間）
     *
     * @param session 已核准（phase=APPROVED）的 SMART auth session
     */
    public void bridgeApprovedSession(DgrSmartAuthSession session) {
        TPILogger.tl.debug("[SmartGtwBridge] Bridging session to GTW tables, state: " + session.getState());

        DgrGtwIdpAuthM authM = findOrCreateAuthM(session);
        createAuthDRecords(authM.getGtwIdpAuthMId(), session.getGrantedScope());
        createAuthCode(session);

        TPILogger.tl.debug("[SmartGtwBridge] Bridge completed, authCode: " + session.getAuthCode());
    }

    /**
     * 查找或建立 AUTH_M 紀錄。
     *
     * ssotoken IdP 授權流程（GtwIdPAuthService）在 authorize 階段已建立 AUTH_M，
     * 此處以 state 查找既有紀錄並更新 SMART auth code；若不存在才新建。
     */
    private DgrGtwIdpAuthM findOrCreateAuthM(DgrSmartAuthSession session) {
        DgrGtwIdpAuthM authM = authMDao.findFirstByState(session.getState());

        if (authM != null) {
            authM.setAuthCode(session.getAuthCode());
            authM.setRedirectUri(session.getRedirectUri());
            authM.setCodeChallenge(session.getCodeChallenge());
            authM.setCodeChallengeMethod(session.getCodeChallengeMethod());
            authM.setUpdateUser("SYSTEM");
            authM.setUpdateDateTime(DateTimeUtil.now());
            return authMDao.save(authM);
        }

        DgrGtwIdpAuthM newAuthM = new DgrGtwIdpAuthM();
        newAuthM.setState(session.getState());
        newAuthM.setIdpType(IDP_TYPE_SMART);
        newAuthM.setClientId(session.getClientId());
        newAuthM.setAuthCode(session.getAuthCode());
        newAuthM.setRedirectUri(session.getRedirectUri());
        newAuthM.setCodeChallenge(session.getCodeChallenge());
        newAuthM.setCodeChallengeMethod(session.getCodeChallengeMethod());
        newAuthM.setCreateUser("SYSTEM");
        newAuthM.setCreateDateTime(DateTimeUtil.now());
        return authMDao.save(newAuthM);
    }

    private void createAuthDRecords(long refAuthMId, List<String> grantedScopes) {
        if (grantedScopes == null || grantedScopes.isEmpty()) {
            return;
        }
        for (String scope : grantedScopes) {
            if (StringUtils.hasLength(scope)) {
                DgrGtwIdpAuthD authD = new DgrGtwIdpAuthD();
                authD.setRefGtwIdpAuthMId(refAuthMId);
                authD.setScope(scope);
                authD.setCreateUser("SYSTEM");
                authD.setCreateDateTime(DateTimeUtil.now());
                authDDao.save(authD);
            }
        }
    }

    private void createAuthCode(DgrSmartAuthSession session) {
        DgrGtwIdpAuthCode authCode = new DgrGtwIdpAuthCode();
        authCode.setAuthCode(session.getAuthCode());
        authCode.setPhase(DgrAuthCodePhase.AUTH_CODE);
        authCode.setStatus(TsmpAuthCodeStatus2.AVAILABLE.value());
        authCode.setExpireDateTime(session.getAuthCodeExpire());
        authCode.setIdpType(IDP_TYPE_SMART);
        authCode.setClientId(session.getClientId());
        authCode.setUserName(session.getUserName());
        authCode.setUserAlias(session.getUserAlias());
        authCode.setUserEmail(session.getUserEmail());
        authCode.setState(session.getState());
        authCode.setCreateUser("SYSTEM");
        authCode.setCreateDateTime(DateTimeUtil.now());
        authCodeDao.save(authCode);
    }
}
