package tpi.dgrv4.gateway.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.nimbusds.jwt.JWTClaimsSet;

import tpi.dgrv4.gateway.component.SmartTokenVerifier;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.constant.DgrTokenType;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART Token Introspection 服務（RFC 7662）。
 *
 * 解析並驗證 JWT access token，回傳 active/inactive 與 claims。
 * 驗章通過後仍須查平台撤銷帳本 TSMP_TOKEN_HISTORY：JWT 本身無狀態，
 * 被撤銷的 token 簽章依舊有效，唯有查帳才能反映 SmartRevokeService 寫下的撤銷標記。
 */
@Service
public class SmartIntrospectionService {

    private final SmartTokenVerifier tokenVerifier;
    private final TokenHelper tokenHelper;

    @Autowired
    public SmartIntrospectionService(SmartTokenVerifier tokenVerifier, TokenHelper tokenHelper) {
        this.tokenVerifier = tokenVerifier;
        this.tokenHelper = tokenHelper;
    }

    /**
     * 驗證 token 並回傳 RFC 7662 格式的 introspection response。
     *
     * @param token 要驗證的 token 字串
     * @return introspection response（至少含 active 欄位）
     */
    public Map<String, Object> introspect(String token) {
        if (!StringUtils.hasText(token)) {
            return inactiveResponse();
        }

        try {
            JWTClaimsSet claims = tokenVerifier.verify(token);
            if (claims == null) {
                return inactiveResponse();
            }

            Date exp = claims.getExpirationTime();
            if (exp != null && new Date().after(exp)) {
                return inactiveResponse();
            }

            // 撤銷帳本：revoked_status 有值即視為已撤銷（"R" 撤銷、"N" 後踢前、"RT" 換發作廢）
            String jti = claims.getJWTID();
            if (StringUtils.hasText(jti)
                    && tokenHelper.checkTokenRevoked(DgrTokenType.ACCESS_TOKEN, jti)) {
                TPILogger.tl.debug("[SMART Introspect] token revoked, jti=" + jti);
                return inactiveResponse();
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("active", true);

            if (claims.getIssuer() != null) response.put("iss", claims.getIssuer());
            if (claims.getSubject() != null) response.put("sub", claims.getSubject());
            if (claims.getAudience() != null && !claims.getAudience().isEmpty()) {
                response.put("aud", claims.getAudience().size() == 1
                        ? claims.getAudience().get(0)
                        : claims.getAudience());
            }
            if (exp != null) response.put("exp", exp.getTime() / 1000);
            if (claims.getIssueTime() != null) response.put("iat", claims.getIssueTime().getTime() / 1000);
            if (claims.getJWTID() != null) response.put("jti", claims.getJWTID());

            String clientId = claims.getStringClaim("client_id");
            if (clientId != null) response.put("client_id", clientId);

            Object scopeClaim = claims.getClaim("scope");
            if (scopeClaim instanceof java.util.List<?> scopeList) {
                response.put("scope", String.join(" ", scopeList.stream()
                        .map(Object::toString).toList()));
            } else if (scopeClaim instanceof String s) {
                response.put("scope", s);
            }

            if (claims.getStringClaim("patient") != null) {
                response.put("patient", claims.getStringClaim("patient"));
            }
            if (claims.getStringClaim("encounter") != null) {
                response.put("encounter", claims.getStringClaim("encounter"));
            }

            response.put("token_type", "Bearer");

            return response;

        } catch (Exception e) {
            TPILogger.tl.debug("[SMART Introspect] Failed to parse token: " + e.getMessage());
            return inactiveResponse();
        }
    }

    private Map<String, Object> inactiveResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("active", false);
        return response;
    }
}
