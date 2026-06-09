package tpi.dgrv4.dpaa.service.smartClientService;

import java.util.List;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartClientType;

/**
 * SMART Client 共用驗證工具。
 *
 * 提供 Create（DPB0331）和 Update（DPB0332）共用的驗證方法。
 * 只包含 Mechanism（驗證邏輯），不包含 Policy（合法值定義在 enum）。
 */
public final class SmartClientValidator {

    private SmartClientValidator() {}

    /**
     * 必填欄位檢查。
     *
     * @param value     欄位值
     * @param fieldName 欄位名稱（用於錯誤訊息）
     * @param hint      修正提示（告訴使用者怎麼修）
     */
    public static void requireNonEmpty(List<String> value, String fieldName, String hint) {
        if (value == null || value.isEmpty()) {
            throw TsmpDpAaRtnCode._1559.throwing(
                "Field '" + fieldName + "' is required. " + hint);
        }
    }

    public static void requireNonBlank(String value, String fieldName, String hint) {
        if (!StringUtils.hasText(value)) {
            throw TsmpDpAaRtnCode._1559.throwing(
                "Field '" + fieldName + "' is required. " + hint);
        }
    }

    /**
     * clientType 與認證方式的交叉驗證。
     *
     * 規則來自 {@link SmartClientType} 的 allowedAuthMethods() 和 requiresJwk()，
     * 此方法只負責驗證機制，不定義規則。
     *
     * @param clientType 已解析的 clientType enum
     * @param authMethod tokenEndpointAuthMethod 的值
     * @param jwksUri    jwksUri 的值
     * @param jwks       jwks 的值
     */
    public static void validateClientTypeConstraints(
            SmartClientType clientType, String authMethod, String jwksUri, String jwks) {

        boolean hasAuthMethod = StringUtils.hasText(authMethod);
        boolean hasJwksUri = StringUtils.hasText(jwksUri);
        boolean hasJwks = StringUtils.hasText(jwks);

        // authMethod 驗證
        if (clientType.allowedAuthMethods().isEmpty()) {
            // 此 clientType 不使用 token endpoint 認證（如 public）
            if (hasAuthMethod) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    clientType.key() + " client does not use token endpoint authentication. "
                    + "Remove 'tokenEndpointAuthMethod' or change 'clientType'.");
            }
        } else {
            // 此 clientType 需要 authMethod
            requireNonBlank(authMethod, "tokenEndpointAuthMethod",
                clientType.key() + " client requires one of: " + clientType.allowedAuthMethods() + ".");
            if (!clientType.allowedAuthMethods().contains(authMethod)) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    "Field 'tokenEndpointAuthMethod' is invalid: '" + authMethod + "'. "
                    + clientType.key() + " client must use one of: " + clientType.allowedAuthMethods() + ".");
            }
        }

        // JWK 驗證
        if (clientType.requiresJwk()) {
            if (!hasJwksUri && !hasJwks) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    clientType.key() + " client requires a public key. "
                    + "Provide 'jwksUri' (recommended) or 'jwks' (inline JSON).");
            }
            if (hasJwksUri && hasJwks) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    "Provide either 'jwksUri' or 'jwks', not both. "
                    + "'jwksUri' is recommended as it supports key rotation.");
            }
        } else {
            if (hasJwksUri || hasJwks) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    clientType.key() + " client does not use asymmetric keys. "
                    + "Remove 'jwksUri' and 'jwks' or change 'clientType' to confidential-asymmetric.");
            }
        }
    }
}
