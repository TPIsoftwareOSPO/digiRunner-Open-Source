package tpi.dgrv4.gateway.vo;

import java.net.URI;
import java.util.Map;

import lombok.Builder;

/**
 * SMART App Launch 授權請求的輸入封裝。
 *
 * 背景：OAuth 2.0 授權端點的 query parameters 散落在 Map 裡，
 * 欄位名稱是 snake_case、容易拼錯、驗證邏輯也沒有歸屬。
 * 此 record 將 input 集中管理，提供結構驗證（必填、格式），
 * 並透過 record 自帶的 toString() 在 exception 發生時完整呈現所有 input。
 *
 * 驗證分兩層：
 * - 結構驗證（本 class 的 validate()）：必填欄位、response_type 值、URL 格式
 * - 業務驗證（SmartAuthorizeService）：aud 對應 DB route、scope 權限等
 *
 * 範例：
 *   SmartAuthorizeRequest req = SmartAuthorizeRequest.fromQueryParams(queryParams);
 *   req.validate();  // 結構不合法時拋 SmartAuthorizeException，message 含完整 input
 *
 * 規格依據：HL7 SMART App Launch STU2.2、OAuth 2.0（RFC 6749 §4.1.1）
 *
 * @param responseType        OAuth2 response_type，必須為 "code"（REQUIRED）
 * @param clientId            客戶端識別碼（REQUIRED）
 * @param redirectUri         授權完成後的回呼 URL（REQUIRED）
 * @param scope               請求的 OAuth scope，空格分隔（REQUIRED）
 * @param state               防 CSRF 的隨機值（RECOMMENDED，但實務上必填）
 * @param aud                 目標 FHIR Server 的 base URL（REQUIRED by SMART）
 * @param codeChallengeMethod PKCE 方法，僅接受 "S256"（CONDITIONAL）
 * @param codeChallenge       PKCE challenge 值（CONDITIONAL，搭配 codeChallengeMethod）
 * @param launch              EHR launch 時由 EHR 提供的 launch context（OPTIONAL）
 */
@Builder(toBuilder = true)
public record SmartAuthorizeRequest(
	String responseType,
	String clientId,
	String redirectUri,
	String scope,
	String state,
	String aud,
	String codeChallengeMethod,
	String codeChallenge,
	String launch,
	String simError
) {

	// ==================== 工廠方法 ====================

	/**
	 * 從 query parameter Map 建立 SmartAuthorizeRequest。
	 *
	 * 背景：Spring 的 @RequestParam Map 使用 snake_case key（如 "response_type"），
	 * 此方法集中處理 snake_case → camelCase 對應，避免散落在 Controller 各處手動 get。
	 * 即使某些欄位為 null，也會正常建構——驗證交給 validate()，
	 * 確保 exception 發生時 toString() 能呈現完整的原始 input（包括哪些欄位是 null）。
	 */
	public static SmartAuthorizeRequest fromQueryParams(Map<String, String> params) {
		return SmartAuthorizeRequest.builder()
			.responseType(params.get("response_type"))
			.clientId(params.get("client_id"))
			.redirectUri(params.get("redirect_uri"))
			.scope(params.get("scope"))
			.state(params.get("state"))
			.aud(params.get("aud"))
			.codeChallengeMethod(params.get("code_challenge_method"))
			.codeChallenge(params.get("code_challenge"))
			.launch(params.get("launch"))
			.simError(params.get("sim_error"))
			.build();
	}

	// ==================== 結構驗證 ====================

	/**
	 * 驗證 request 的結構完整性（必填欄位、值域、格式）。
	 *
	 * 背景：結構驗證不需要 DB，在 Controller 層即可執行，
	 * 盡早擋掉明顯錯誤的請求，避免無謂的 DB 查詢。
	 * 驗證失敗時拋出的 SmartAuthorizeException 會自動附上 this.toString()，
	 * 讓 log 中能一眼看到是哪組 input 出了問題。
	 *
	 * @throws SmartAuthorizeException 結構不合法時拋出
	 */
	public void validate() {
		// response_type 必須為 "code"（SMART STU2.2 僅支援 authorization code flow）
		if (responseType == null) {
			throw fail("invalid_request", "Missing required parameter: response_type");
		}
		if (!"code".equals(responseType)) {
			throw fail("unsupported_response_type",
					"Unsupported response_type: " + responseType + ". Only \"code\" is supported");
		}

		// 必填欄位檢查
		requireNonBlank("client_id", clientId);
		requireNonBlank("redirect_uri", redirectUri);
		requireNonBlank("scope", scope);
		requireNonBlank("aud", aud);

		// redirect_uri 格式驗證
		validateUrl("redirect_uri", redirectUri);

		// aud 格式驗證
		validateUrl("aud", aud);

		// PKCE：兩個欄位必須同時存在或同時不存在
		if (codeChallengeMethod != null && codeChallenge == null) {
			throw fail("invalid_request",
					"code_challenge is required when code_challenge_method is present");
		}
		if (codeChallenge != null && codeChallengeMethod == null) {
			throw fail("invalid_request",
					"code_challenge_method is required when code_challenge is present");
		}
		if (codeChallengeMethod != null && !"S256".equals(codeChallengeMethod)) {
			throw fail("invalid_request",
					"Unsupported code_challenge_method: " + codeChallengeMethod + ". Only \"S256\" is supported");
		}
	}

	// ==================== 驗證工具 ====================

	private void requireNonBlank(String paramName, String value) {
		if (value == null || value.isBlank()) {
			throw fail("invalid_request", "Missing required parameter: " + paramName);
		}
	}

	private void validateUrl(String paramName, String value) {
		try {
			new URI(value).toURL();
		} catch (Exception e) {
			throw fail("invalid_request",
					"Invalid " + paramName + ": " + value + ". " + e.getMessage());
		}
	}

	/**
	 * 建立附帶完整 input 的 SmartAuthorizeException。
	 * toString() 由 record 自動產生，包含所有欄位值。
	 */
	private SmartAuthorizeException fail(String error, String description) {
		return new SmartAuthorizeException(error, description, this.toString());
	}

	// ==================== 例外 ====================

	/**
	 * SMART 授權驗證失敗例外。
	 *
	 * 背景：OAuth 2.0（RFC 6749 §4.1.2.1）定義授權端點錯誤應帶 error code。
	 * 此例外攜帶三項資訊：
	 * - error：OAuth error code（如 "invalid_request"）
	 * - description：人類可讀的錯誤描述
	 * - requestInput：觸發錯誤的完整 input（來自 SmartAuthorizeRequest.toString()）
	 *
	 * requestInput 僅用於 server-side log，不應回傳給 client。
	 */
	public static class SmartAuthorizeException extends RuntimeException {

		private final String error;
		private final String description;
		private final String requestInput;

		public SmartAuthorizeException(String error, String description, String requestInput) {
			super(description + " | input: " + requestInput);
			this.error = error;
			this.description = description;
			this.requestInput = requestInput;
		}

		/**
		 * 業務驗證用：Service 層呼叫時可能尚未持有 request 物件，
		 * requestInput 設為 null。
		 */
		public SmartAuthorizeException(String error, String description) {
			this(error, description, null);
		}

		public String getError() {
			return error;
		}

		public String getDescription() {
			return description;
		}

		public String getRequestInput() {
			return requestInput;
		}
	}
}
