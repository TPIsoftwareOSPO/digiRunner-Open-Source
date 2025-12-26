package tpi.dgrv4.gateway.constant;

public class DgrTokenGrantType {

	public final static String PASSWORD = "password";

	public final static String CLIENT_CREDENTIALS = "client_credentials";

	public final static String REFRESH_TOKEN = "refresh_token";

	// GTW IdP 流程使用
	public final static String AUTHORIZATION_CODE = "authorization_code";

	// --以下為客製
	
	// SSO AC IdP 介接使用
	public final static String DELEGATE_AUTH = "delegate_auth";

	// 不是真的 grant type, GTW IdP 流程使用
	public final static String COOKIE_TOKEN = "cookie_token";
}
