package tpi.dgrv4.gateway.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.nimbusds.jwt.JWTClaimsSet;

import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.repository.DgrSmartAuthSessionDao;
import tpi.dgrv4.gateway.component.SmartTokenVerifier;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART Token Revocation 服務（RFC 7009）。
 *
 * 兩條核發路徑的 token 都已寫入平台撤銷帳本 TSMP_TOKEN_HISTORY
 * （app-launch 走 SmartTokenService.writeSmartTokenHistory，
 * client_credentials 委派 PATH_V2 後走 OAuthTokenService.doTokenHistory），
 * 因此撤銷沿用平台原生做法：標記 revoked_status / rft_revoked_status 為 "R"。
 *
 * token 型別以「能否驗章解析」判斷，不依賴 token_type_hint：
 * - 解不開且命中 session.refresh_token → app-launch 的 opaque refresh token。
 *   該值在發證時即以原字串寫入 retoken_jti，故可直接作為撤銷 key。
 * - 驗章通過 → access token（兩條路徑皆為 RS256 JWS），以 payload 的 jti 撤銷。
 *
 * 歸屬檢查：只允許撤銷自己名下的 token，避免任一 client 撤銷他人 token。
 * 依 RFC 7009 §2.2，查無、無法驗章或不屬於呼叫者的 token 一律靜默視為成功，
 * 避免回應差異被用於 token 存在性探測。
 */
@Service
public class SmartRevokeService {

    private final DgrSmartAuthSessionDao sessionDao;
    private final SmartTokenVerifier tokenVerifier;
    private final OAuthRevocationService oauthRevocationService;

    @Autowired
    public SmartRevokeService(DgrSmartAuthSessionDao sessionDao,
            SmartTokenVerifier tokenVerifier,
            OAuthRevocationService oauthRevocationService) {
        this.sessionDao = sessionDao;
        this.tokenVerifier = tokenVerifier;
        this.oauthRevocationService = oauthRevocationService;
    }

    /**
     * 撤銷 token。
     *
     * @param token    要撤銷的 token 值
     * @param clientId 認證通過的呼叫者 client_id，用於歸屬檢查
     */
    @Transactional
    public void revoke(String token, String clientId) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        // 1. opaque refresh token：以值查 session
        Optional<DgrSmartAuthSession> sessionOpt = sessionDao.findByRefreshToken(token);
        if (sessionOpt.isPresent()) {
            revokeRefreshToken(sessionOpt.get(), token, clientId);
            return;
        }

        // 2. access token：驗章後以 jti 撤銷
        revokeAccessToken(token, clientId);
    }

    private void revokeRefreshToken(DgrSmartAuthSession session, String token, String clientId) {
        if (!isOwnedBy(session.getClientId(), clientId)) {
            // 不記 token 值本身，避免敏感資訊進 log
            TPILogger.tl.warn("[SMART Revoke] refresh token does not belong to caller, caller=" + clientId
                    + ", owner=" + session.getClientId());
            return;
        }

        session.setRefreshToken(null);
        session.setRefreshTokenExpire(null);
        sessionDao.save(session);

        // opaque refresh token 值即為 TSMP_TOKEN_HISTORY 的 retoken_jti
        oauthRevocationService.doRevocation(DgrTokenType.REFRESH_TOKEN, token);
        TPILogger.tl.info("[SMART Revoke] refresh token revoked, clientId=" + clientId);
    }

    private void revokeAccessToken(String token, String clientId) {
        JWTClaimsSet claims = tokenVerifier.verify(token);
        if (claims == null) {
            TPILogger.tl.debug("[SMART Revoke] token not verifiable, treated as no-op, clientId=" + clientId);
            return;
        }

        String tokenClientId;
        try {
            tokenClientId = claims.getStringClaim("client_id");
        } catch (Exception e) {
            TPILogger.tl.debug("[SMART Revoke] client_id claim unreadable: " + e.getMessage());
            return;
        }

        if (!isOwnedBy(tokenClientId, clientId)) {
            TPILogger.tl.warn("[SMART Revoke] access token does not belong to caller, caller=" + clientId
                    + ", owner=" + tokenClientId);
            return;
        }

        String jti = claims.getJWTID();
        if (!StringUtils.hasText(jti)) {
            TPILogger.tl.debug("[SMART Revoke] token has no jti, nothing to revoke, clientId=" + clientId);
            return;
        }

        oauthRevocationService.doRevocation(DgrTokenType.ACCESS_TOKEN, jti);
        TPILogger.tl.info("[SMART Revoke] access token revoked, clientId=" + clientId + ", jti=" + jti);
    }

    private boolean isOwnedBy(String tokenClientId, String callerClientId) {
        return StringUtils.hasText(tokenClientId) && tokenClientId.equals(callerClientId);
    }
}
