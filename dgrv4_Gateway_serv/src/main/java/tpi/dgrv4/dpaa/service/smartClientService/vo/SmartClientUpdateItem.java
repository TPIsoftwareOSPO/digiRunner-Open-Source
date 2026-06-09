package tpi.dgrv4.dpaa.service.smartClientService.vo;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tpi.dgrv4.entity.entity.DgrSmartClient;

/**
 * SMART Client 批次更新的單筆項目。
 *
 * PATCH 語意：使用 {@link JsonNullable} 區分三態——
 * - undefined（JSON 欄位不存在）→ 不更新，保留舊值
 * - present + null（JSON 欄位值為 null）→ 清除欄位
 * - present + value（JSON 欄位有值）→ 更新為該值
 *
 * 參照 {@link tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyUpdateDto} 的做法。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SmartClientUpdateItem {

    // ==================== 必填識別欄位 ====================

    /** 識別更新目標，值本身不可更改 */
    private String clientId;

    /** 樂觀鎖版本號，與 DB 不一致時拋錯 */
    private Long version;

    // ==================== 可更新欄位（JsonNullable 三態） ====================

    private JsonNullable<String> clientType = JsonNullable.undefined();
    private JsonNullable<String> idpType = JsonNullable.undefined();
    private JsonNullable<String> idpClientId = JsonNullable.undefined();
    private JsonNullable<List<String>> allowedScopes = JsonNullable.undefined();
    private JsonNullable<List<String>> redirectUris = JsonNullable.undefined();
    private JsonNullable<String> launchMode = JsonNullable.undefined();
    private JsonNullable<String> autoApprove = JsonNullable.undefined();
    private JsonNullable<String> tokenEndpointAuthMethod = JsonNullable.undefined();
    private JsonNullable<String> jwksUri = JsonNullable.undefined();
    private JsonNullable<String> jwks = JsonNullable.undefined();

    /**
     * 將有傳的欄位套用到既有 Entity。
     *
     * 只做機械套用（Mechanism），不做驗證。
     * isPresent() = true 表示前端有傳該欄位（可能是新值或 null 清除）。
     *
     * @param entity 從 DB 查出的既有 Entity
     */
    public void applyTo(DgrSmartClient entity) {
        if (clientType != null && clientType.isPresent()) {
            entity.setClientType(clientType.get());
        }
        if (idpType != null && idpType.isPresent()) {
            entity.setIdpType(idpType.get());
        }
        if (idpClientId != null && idpClientId.isPresent()) {
            entity.setIdpClientId(idpClientId.get());
        }
        if (allowedScopes != null && allowedScopes.isPresent()) {
            entity.setAllowedScopes(allowedScopes.get());
        }
        if (redirectUris != null && redirectUris.isPresent()) {
            entity.setRedirectUris(redirectUris.get());
        }
        if (launchMode != null && launchMode.isPresent()) {
            entity.setLaunchMode(launchMode.get());
        }
        if (autoApprove != null && autoApprove.isPresent()) {
            entity.setAutoApprove(autoApprove.get());
        }
        if (tokenEndpointAuthMethod != null && tokenEndpointAuthMethod.isPresent()) {
            entity.setTokenEndpointAuthMethod(tokenEndpointAuthMethod.get());
        }
        if (jwksUri != null && jwksUri.isPresent()) {
            entity.setJwksUri(jwksUri.get());
        }
        if (jwks != null && jwks.isPresent()) {
            entity.setJwks(jwks.get());
        }
    }
}
