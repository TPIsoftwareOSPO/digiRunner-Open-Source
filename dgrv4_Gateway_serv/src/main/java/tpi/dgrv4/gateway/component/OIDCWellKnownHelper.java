package tpi.dgrv4.gateway.component;

import tpi.dgrv4.common.constant.DgrIdPType;

public class OIDCWellKnownHelper {

	/**
	 * 是否要顯示 CallbackEndpoint
	 */
	public static boolean isShowCallbackEndpoint(String idPType) {
		if (DgrIdPType.GOOGLE.equals(idPType) //
				|| DgrIdPType.MS.equals(idPType) //
				|| DgrIdPType.OIDC.equals(idPType) //
		) {
			// OAuth 2.0 的 IdP type 才需要顯示 CallbackEndpoint
			return true;
		}

		return false;
	}

	/**
	 * 取得 dgR 的 Scheme Domain Port <br>
	 * 例如: https://domain:port <br>
	 * 若 port 為 443,則不顯示 port <br>
	 * 例如: https://domain <br>
	 */
	public static String getSchemeAndDomainAndPort(String dgrPublicDomain, String dgrPublicPort) {
		String schemeAndDomainAndPort = "";

		// 若 https 為預設 port 443,則不顯示 port
		if ("443".equals(dgrPublicPort)) {
			schemeAndDomainAndPort = String.format("https://%s", dgrPublicDomain);
		} else {
			schemeAndDomainAndPort = String.format("https://%s:%s", dgrPublicDomain, dgrPublicPort);
		}

		return schemeAndDomainAndPort;
	}

	/**
	 * 1. issuer 的值 <br>
	 * 2. 核發 ID token 的 iss 值 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/{idPType} <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/GOOGLE <br>
	 * 3.若 pathVer 有值,例如為 "/v2", 則 "https://127.0.0.1:8080/dgrv4/ssotoken/v2/OIDC"
	 */
	public static String getIssuer(String schemeAndDomainAndPort, String idPType, String pathVer) {
		return String.format("%s/dgrv4/ssotoken%s/%s", schemeAndDomainAndPort, pathVer, idPType);
	}

	/**
	 * OpenID Connect 啟動身分驗證的入口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/{idPType}/authorization <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/authorization <br>
	 */
	public static String getAuthorizationEndpoint(String schemeAndDomainAndPort, String idPType) {
		return String.format("%s/dgrv4/ssotoken/gtwidp/%s/authorization", schemeAndDomainAndPort, idPType);
	}

	/**
	 * OpenID Connect 讓 Client 取得 token 的端口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/oauth/token <br>
	 */
	public static String getTokenEndpoint(String schemeAndDomainAndPort, String pathVer) {
		return String.format("%s/oauth%s/token", schemeAndDomainAndPort, pathVer);
	}

	/**
	 * OpenID Connect 讓 Client 取得 UserInfo 的端口 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/v2/userInfo <br>
	 */
	public static String getUserinfoEndpoint(String schemeAndDomainAndPort) {
		return String.format("%s/dgrv4/ssotoken/gtwidp/v2/userInfo", schemeAndDomainAndPort);
	}

	/**
	 * digiRunner 的 callback URL <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/gtwidp/GOOGLE/gtwIdPCallback <br>
	 */
	public static String getCallbackEndpoint(String schemeAndDomainAndPort, String idPType) {
		return String.format("%s/dgrv4/ssotoken/gtwidp/%s/gtwIdPCallback", schemeAndDomainAndPort, idPType);
	}

	/**
	 * 放 JWK Set 的公鑰內容 <br>
	 * 例如: <br>
	 * https://127.0.0.1:8080/dgrv4/ssotoken/oauth2/certs <br>
	 */
	public static String getJwksUri(String schemeAndDomainAndPort) {
		return String.format("%s/dgrv4/ssotoken/oauth2/certs", schemeAndDomainAndPort);
	}
}
