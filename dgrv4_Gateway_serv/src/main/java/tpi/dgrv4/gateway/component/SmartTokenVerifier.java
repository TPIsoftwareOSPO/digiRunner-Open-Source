package tpi.dgrv4.gateway.component;

import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART token 的簽章驗證。
 *
 * 兩條核發路徑（app-launch 與 client_credentials）簽出的 access token 皆為 RS256 JWS，
 * 但用的 key 不同，由 {@link SmartKeyProvider#getVerificationKey(String)} 依 kid 分派。
 * 本元件封裝「解析 + 驗章」這段共用邏輯，供 introspection 與 revocation 使用。
 *
 * 不檢查 exp——是否容許過期 token 由呼叫端自行決定：
 * introspection 須判為 inactive，revocation 則對過期 token 撤銷亦無害。
 */
@Component
public class SmartTokenVerifier {

    private final SmartKeyProvider keyProvider;

    @Autowired
    public SmartTokenVerifier(SmartKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    /**
     * 解析並驗章。
     *
     * @param token JWS 格式的 token 字串
     * @return 驗章通過的 claims；非 JWS、演算法非 RS256、kid 無法對應或驗章失敗一律回 null
     */
    public JWTClaimsSet verify(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            SignedJWT jwt = SignedJWT.parse(token);

            // 簽發端固定使用 RS256，其他演算法一律視為無效 token
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())) {
                return null;
            }

            // 依 kid 解析驗章公鑰：core key（Backend Services）或 GtwIdP key（app-launch）
            RSAPublicKey verifyKey = keyProvider.getVerificationKey(jwt.getHeader().getKeyID());
            if (verifyKey == null) {
                return null;
            }

            JWSVerifier verifier = new RSASSAVerifier(verifyKey);
            if (!jwt.verify(verifier)) {
                return null;
            }

            return jwt.getJWTClaimsSet();

        } catch (Exception e) {
            TPILogger.tl.debug("[SMART TokenVerifier] Failed to verify token: " + e.getMessage());
            return null;
        }
    }
}
