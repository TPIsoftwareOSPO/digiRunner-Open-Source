package tpi.dgrv4.gateway.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

/**
 * SMART App Launch .well-known/smart-configuration 的回應結構。
 *
 * 背景：DGR 作為多個 FHIR Server 的 gateway，同時負責核發與驗證 token，
 * 需要對外公告一份統一的 SMART configuration。此 DTO 定義了所有欄位與預設值，
 * 讓管理者可透過管理 API 修改特定欄位（如 scopesSupported 中的 resource 類型），
 * 改變 .well-known/smart-configuration 的回傳內容。
 *
 * 欄位設計依據 HL7 SMART App Launch STU2.2 規格。
 * nullable 欄位標記為 {@code @JsonInclude(NON_NULL)}，未設定時不會出現在 JSON 中。
 *
 * 範例：
 *   // 建立預設值
 *   SmartConfigurationDto dto = SmartConfigurationDto.createDefault();
 *
 *   // 部分更新（透過 toBuilder 複製再修改）
 *   SmartConfigurationDto updated = dto.toBuilder()
 *       .scopesSupported(List.of("openid", "profile", "patient/Condition.rs"))
 *       .build();
 *
 * @param tokenEndpoint                    OAuth2 Token 端點（REQUIRED）
 * @param grantTypesSupported              支援的 OAuth2 授權類型（REQUIRED）
 * @param capabilities                     伺服器支援的 SMART 功能列表（REQUIRED）
 * @param codeChallengeMethodsSupported    支援的 PKCE 方法（REQUIRED）。
 *                                         STU2.2 將此欄位提升為 REQUIRED，且禁止 "plain"，只能用 "S256"。
 * @param issuer                           OpenID Connect Issuer URL（capabilities 含 "sso-openid-connect" 時必填）
 * @param jwksUri                          JSON Web Key Set URL（capabilities 含 "sso-openid-connect" 時必填）
 * @param authorizationEndpoint            OAuth2 授權端點（支援 "launch-ehr" 或 "launch-standalone" 時必填）
 * @param scopesSupported                  客戶端可請求的 OAuth scope 列表（RECOMMENDED）
 * @param responseTypesSupported           支援的 OAuth2 response_type 值（RECOMMENDED）
 * @param introspectionEndpoint            Token 驗證端點（RECOMMENDED）
 * @param revocationEndpoint               Token 撤銷端點（RECOMMENDED）
 * @param managementEndpoint               終端使用者管理應用程式存取權限的頁面（RECOMMENDED）
 * @param tokenEndpointAuthMethodsSupported Token 端點支援的客戶端驗證方式（OPTIONAL）
 * @param registrationEndpoint             OAuth2 動態註冊端點（OPTIONAL）
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SmartConfigurationDto(

	// ===== REQUIRED =====

	@JsonProperty("token_endpoint")
	String tokenEndpoint,

	@JsonProperty("grant_types_supported")
	List<String> grantTypesSupported,

	@JsonProperty("capabilities")
	List<String> capabilities,

	@JsonProperty("code_challenge_methods_supported")
	List<String> codeChallengeMethodsSupported,

	// ===== CONDITIONAL =====

	@JsonProperty("issuer")
	String issuer,

	@JsonProperty("jwks_uri")
	String jwksUri,

	@JsonProperty("authorization_endpoint")
	String authorizationEndpoint,

	// ===== RECOMMENDED =====

	@JsonProperty("scopes_supported")
	List<String> scopesSupported,

	@JsonProperty("response_types_supported")
	List<String> responseTypesSupported,

	@JsonProperty("introspection_endpoint")
	String introspectionEndpoint,

	@JsonProperty("revocation_endpoint")
	String revocationEndpoint,

	@JsonProperty("management_endpoint")
	String managementEndpoint,

	// ===== OPTIONAL =====

	@JsonProperty("token_endpoint_auth_methods_supported")
	List<String> tokenEndpointAuthMethodsSupported,

	@JsonProperty("registration_endpoint")
	String registrationEndpoint
) {

	/**
	 * 建立帶有預設值的 SmartConfigurationDto。
	 *
	 * 背景：端點 URL 由 DGR 的對外 base URL 加上固定的 OAuth 路徑組成，
	 * base URL 來自 TsmpSetting 的 DGR_PUBLIC_DOMAIN / DGR_PUBLIC_PORT 設定，
	 * 透過 {@code GtwIdPWellKnownService.getSchemeAndDomainAndPort()} 組裝。
	 * scopesSupported 和 capabilities 為管理者可調整的欄位，此處提供初始預設值。
	 *
	 * 範例：
	 *   String baseUrl = GtwIdPWellKnownService
	 *       .getSchemeAndDomainAndPort(domain, port);
	 *   SmartConfigurationDto dto = SmartConfigurationDto.createDefault(baseUrl);
	 *
	 *   // 部分更新
	 *   dto = dto.toBuilder()
	 *       .scopesSupported(List.of("openid", "patient/Condition.rs"))
	 *       .build();
	 *
	 * @param schemeAndDomainAndPort DGR 對外 base URL（如 "https://dgr.example.com:18080"）
	 */
	public static SmartConfigurationDto createDefault(String schemeAndDomainAndPort) {
		return SmartConfigurationDto.builder()
			.issuer(schemeAndDomainAndPort)
			.authorizationEndpoint(schemeAndDomainAndPort + "/dgrv4/ssotoken/smart/authorize")
			.tokenEndpoint(schemeAndDomainAndPort + "/dgrv4/ssotoken/smart/token")
			.jwksUri(schemeAndDomainAndPort + "/dgrv4/ssotoken/smart/jwks")
			.introspectionEndpoint(schemeAndDomainAndPort + "/dgrv4/ssotoken/smart/introspect")
			.revocationEndpoint(schemeAndDomainAndPort + "/dgrv4/ssotoken/smart/revoke")
			.grantTypesSupported(List.of(
				"authorization_code",
				"client_credentials",
				"refresh_token"
			))
			.responseTypesSupported(List.of(
				"code"
			))
			.scopesSupported(List.of(
				"openid",
				"fhirUser",
				"profile",
				"launch",
				"launch/patient",
				"launch/encounter",
				"patient/*.cruds",
				"user/*.cruds",
				"system/*.cruds",
				"offline_access"
			))
			.codeChallengeMethodsSupported(List.of("S256"))
			.tokenEndpointAuthMethodsSupported(List.of(
				"client_secret_basic",
				"client_secret_post",
				"private_key_jwt"
			))
			.capabilities(List.of(
				"launch-standalone",
				"launch-ehr",
				"client-public",
				"client-confidential-symmetric",
				"client-confidential-asymmetric",
				"context-standalone-patient",
				"context-ehr-patient",
				"context-ehr-encounter",
				"permission-patient",
				"permission-user",
				"permission-v2",
				"permission-offline",
				"sso-openid-connect"
			))
			.build();
	}
}
