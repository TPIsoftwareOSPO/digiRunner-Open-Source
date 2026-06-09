package tpi.dgrv4.dpaa.service.smartClientService.vo;

import java.util.Date;
import java.util.List;

import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.entity.entity.DgrSmartClient;

/**
 * SMART Client 查詢回應 DTO。
 *
 * 對應 {@link DgrSmartClient} 的所有欄位，
 * 其中 jwks（CLOB）替換為 hasJwks 布林標記，避免列表查詢回傳大量 JSON。
 */
public record SmartClientDto(
    String smartClientId,
    String clientId,
    String clientType,
    String idpType,
    String idpClientId,
    String tokenEndpointAuthMethod,
    List<String> allowedScopes,
    List<String> redirectUris,
    String launchMode,
    String autoApprove,
    String jwksUri,
    boolean hasJwks,
    Date createDateTime,
    String createUser,
    Date updateDateTime,
    String updateUser,
    String version
) {

    /**
     * 從 Entity 建構 DTO。
     *
     * @param e SMART Client Entity
     */
    public static SmartClientDto from(DgrSmartClient e) {
        return new SmartClientDto(
            IdCodec.toString(e.getSmartClientId()),
            e.getClientId(),
            e.getClientType(),
            e.getIdpType(),
            e.getIdpClientId(),
            e.getTokenEndpointAuthMethod(),
            e.getAllowedScopes(),
            e.getRedirectUris(),
            e.getLaunchMode(),
            e.getAutoApprove(),
            e.getJwksUri(),
            e.getJwks() != null && !e.getJwks().isEmpty(),
            e.getCreateDateTime(),
            e.getCreateUser(),
            e.getUpdateDateTime(),
            e.getUpdateUser(),
            e.getVersion().toString()
        );
    }
}
