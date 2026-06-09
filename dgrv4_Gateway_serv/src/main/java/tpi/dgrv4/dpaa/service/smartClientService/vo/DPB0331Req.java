package tpi.dgrv4.dpaa.service.smartClientService.vo;

import java.util.List;

import tpi.dgrv4.entity.entity.DgrSmartClient;

/**
 * DPB0331 新增 SMART Client 請求。
 *
 * 所有欄位 nullable，必填與交叉驗證在 Service 層處理。
 *
 * @param clientId                對應 tsmp_client.client_id，需驗證存在且不重複
 * @param clientType              認證方式：public / confidential-symmetric / confidential-asymmetric
 * @param idpType                 身份驗證方式：GOOGLE / MS / OIDC / LDAP / JDBC / API / CUS
 * @param idpClientId             用來做 ssotoken 第二段驗證的 client ID，對應 tsmp_client.client_id
 * @param allowedScopes           SMART scope 清單
 * @param redirectUris            redirect URI 清單
 * @param launchMode              啟動模式：standalone / ehr / both，未傳時預設 standalone
 * @param autoApprove             是否跳過 consent：Y / N，未傳時預設 N
 * @param tokenEndpointAuthMethod Token endpoint 認證方式，confidential 類型必填
 * @param jwksUri                 JWK Set URL，confidential-asymmetric 時與 jwks 擇一必填
 * @param jwks                    JWK Set JSON，confidential-asymmetric 時與 jwksUri 擇一必填
 */
public record DPB0331Req(
    String clientId,
    String clientType,
    String idpType,
    String idpClientId,
    List<String> allowedScopes,
    List<String> redirectUris,
    String launchMode,
    String autoApprove,
    String tokenEndpointAuthMethod,
    String jwksUri,
    String jwks
) {

    /**
     * 純欄位映射為 Entity。
     *
     * 只做 Req → Entity 的機械映射，不處理預設值或業務邏輯。
     * 預設值（autoApprove、launchMode）和審計欄位（createUser）由 Service 層補充。
     */
    public DgrSmartClient toEntity() {
        var entity = new DgrSmartClient();
        entity.setClientId(clientId);
        entity.setClientType(clientType);
        entity.setIdpType(idpType);
        entity.setIdpClientId(idpClientId);
        entity.setAllowedScopes(allowedScopes);
        entity.setRedirectUris(redirectUris);
        entity.setLaunchMode(launchMode);
        entity.setAutoApprove(autoApprove);
        entity.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
        entity.setJwksUri(jwksUri);
        entity.setJwks(jwks);
        return entity;
    }
}
