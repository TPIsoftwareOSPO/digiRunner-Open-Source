package tpi.dgrv4.dpaa.service.smartClientService.constant;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SMART Client 認證類型。
 *
 * 每個類型攜帶其允許的 token endpoint 認證方式和是否需要 JWK 公鑰，
 * 讓驗證邏輯從 enum 宣告取規則，不在 Service 硬編 switch-case。
 *
 * 規格依據：HL7 SMART App Launch STU2.2
 *
 * @see <a href="https://hl7.org/fhir/smart-app-launch/STU2.2/">SMART App Launch STU2.2</a>
 */
public enum SmartClientType {

    /** 無密鑰（瀏覽器 SPA、原生應用），不使用 token endpoint 認證 */
    PUBLIC("public", Set.of(), false),

    /** 對稱式密鑰（client_secret），支援 Basic Auth 或 POST body */
    CONFIDENTIAL_SYMMETRIC("confidential-symmetric",
        Set.of("client_secret_basic", "client_secret_post"), false),

    /** 非對稱式密鑰（JWT assertion + 公鑰），需要 jwksUri 或 jwks */
    CONFIDENTIAL_ASYMMETRIC("confidential-asymmetric",
        Set.of("private_key_jwt"), true);

    private final String key;
    private final Set<String> allowedAuthMethods;
    private final boolean requiresJwk;

    SmartClientType(String key, Set<String> allowedAuthMethods, boolean requiresJwk) {
        this.key = key;
        this.allowedAuthMethods = allowedAuthMethods;
        this.requiresJwk = requiresJwk;
    }

    public String key() {
        return key;
    }

    /** 該類型允許的 tokenEndpointAuthMethod 值。空集合表示不應有 authMethod */
    public Set<String> allowedAuthMethods() {
        return allowedAuthMethods;
    }

    /** 是否需要提供 JWK 公鑰（jwksUri 或 jwks 擇一） */
    public boolean requiresJwk() {
        return requiresJwk;
    }

    /**
     * 從輸入字串比對類型（忽略大小寫）。
     */
    public static Optional<SmartClientType> fromKey(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        for (SmartClientType value : values()) {
            if (value.key.equalsIgnoreCase(trimmed)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static Set<String> allowedKeys() {
        return Arrays.stream(values())
            .map(SmartClientType::key)
            .collect(Collectors.toSet());
    }
}
