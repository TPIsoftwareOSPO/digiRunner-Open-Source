package tpi.dgrv4.gateway.component;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

/**
 * SMART token 端點所需的 RSA 公鑰管理元件。
 *
 * 背景：SMART on FHIR 要求在 /jwks 端點公告公鑰，讓 resource server 驗證 access token 簽章。
 * 本元件從 TsmpCoreTokenEntityHelper 取得 DGR 既有的 RSA KeyPair，轉換為符合 RFC 7517 的
 * JWK JSON，kid 使用 RFC 7638 SHA-256 thumbprint 確保跨次呼叫一致性。
 *
 * 用法：
 *   {@literal @}Autowired SmartKeyProvider keyProvider;
 *   String jwkJson = keyProvider.getPublicJwkJson(); // 放入 JWKS Set 對外公告
 *
 * @throws IllegalStateException 當 KeyPair 無法從資料庫讀取時
 */
@Component
public class SmartKeyProvider {

    private final TsmpCoreTokenEntityHelper keyHelper;

    private final TsmpSettingService tsmpSettingService;

    @Autowired
    public SmartKeyProvider(TsmpCoreTokenEntityHelper keyHelper, TsmpSettingService tsmpSettingService) {
        this.keyHelper = keyHelper;
        this.tsmpSettingService = tsmpSettingService;
    }

    /**
     * 取得 RSA 公鑰的 JWK JSON 字串（不含私鑰）。
     *
     * kid 使用公鑰的 SHA-256 thumbprint（RFC 7638），確保同一把公鑰的 kid 固定不變，
     * 方便驗證端在 key rotation 前後正確比對。
     *
     * @return 符合 RFC 7517 的 JWK JSON，包含 kty、use、alg、kid、n、e
     * @throws IllegalStateException 當 KeyPair 不可用（DB 讀取失敗或未初始化）
     */
    public String getPublicJwkJson() {
        KeyPair keyPair = keyHelper.getKeyPair();
        if (keyPair == null) {
            throw new IllegalStateException("SMART token 簽章金鑰不可用：TsmpCoreTokenEntityHelper.getKeyPair() 回傳 null，請確認 KeyPair 已正確儲存於資料庫");
        }

        try {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();

            RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                // SHA-256 thumbprint 為確定性運算，同一把公鑰每次結果相同
                .keyID(buildRsaKey(rsaPublicKey).computeThumbprint().toString())
                .build();

            return rsaKey.toPublicJWK().toJSONString();

        } catch (Exception e) {
            throw new IllegalStateException("無法將 RSA 公鑰轉換為 JWK 格式：" + e.getMessage(), e);
        }
    }

    /**
     * 取得簽章用 KeyPair（含私鑰）。
     *
     * 用途：SmartTokenService 需要 PrivateKey 來簽發 JWT access token。
     *
     * @return RSA KeyPair
     * @throws IllegalStateException 當 KeyPair 不可用
     */
    public KeyPair getSigningKeyPair() {
        KeyPair keyPair = keyHelper.getKeyPair();
        if (keyPair == null) {
            throw new IllegalStateException("SMART 簽章金鑰不可用：TsmpCoreTokenEntityHelper.getKeyPair() 回傳 null");
        }
        return keyPair;
    }

    /**
     * 取得 kid（SHA-256 thumbprint），與 JWKS 端點公告的 kid 一致。
     *
     * @return kid 字串
     * @throws IllegalStateException 當 KeyPair 不可用
     */
    public String getKid() {
        KeyPair keyPair = getSigningKeyPair();
        try {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
            return buildRsaKey(rsaPublicKey).computeThumbprint().toString();
        } catch (Exception e) {
            throw new IllegalStateException("無法計算 kid：" + e.getMessage(), e);
        }
    }

    /**
     * 依 JWT header 的 kid 解析驗章公鑰。
     *
     * 簽發端目前有兩把 key（見 SmartTokenService）：
     * - core key（kid = RFC 7638 thumbprint）：Backend Services access token、id_token
     * - GtwIdP key（GTW_IDP_JWK1/JWK2 setting，kid 為 UUID）：app-launch access token
     *
     * @param kid JWT header 的 kid；null 或空白時 fallback 到 core key
     * @return 對應的 RSA 公鑰；kid 無法對應任何已知 key 時回傳 null
     */
    public RSAPublicKey getVerificationKey(String kid) {
        if (!StringUtils.hasText(kid) || kid.equals(getKid())) {
            return (RSAPublicKey) getSigningKeyPair().getPublic();
        }

        for (String jwkJson : new String[] {
                tsmpSettingService.getVal_GTW_IDP_JWK1(),
                tsmpSettingService.getVal_GTW_IDP_JWK2() }) {
            if (!StringUtils.hasText(jwkJson)) {
                continue;
            }
            try {
                JWK jwk = JWK.parse(jwkJson);
                if (kid.equals(jwk.getKeyID())) {
                    return jwk.toRSAKey().toRSAPublicKey();
                }
            } catch (Exception e) {
                // 單把 JWK 設定損壞時不影響另一把的比對
                TPILogger.tl.warn("[SMART KeyProvider] GTW_IDP_JWK 設定無法解析: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * 計算 thumbprint 時需要先建立不帶 kid 的 RSAKey。
     * 避免循環依賴（計算 thumbprint 需要 RSAKey，RSAKey 建立時需要 kid）。
     */
    private RSAKey buildRsaKey(RSAPublicKey rsaPublicKey) throws Exception {
        return new RSAKey.Builder(rsaPublicKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .build();
    }
}
