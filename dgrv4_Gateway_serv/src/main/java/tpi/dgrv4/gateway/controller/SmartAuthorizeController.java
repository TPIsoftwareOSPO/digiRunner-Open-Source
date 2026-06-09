package tpi.dgrv4.gateway.controller;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartAuthorizeService;
import tpi.dgrv4.gateway.service.SmartAuthorizeService.AuthorizeResult;
import tpi.dgrv4.gateway.vo.SmartAuthorizeRequest;
import tpi.dgrv4.gateway.vo.SmartAuthorizeRequest.SmartAuthorizeException;

/**
 * SMART App Launch 授權端點。
 *
 * 背景：SMART App Launch 規範要求授權端點獨立於既有的 GTW IdP 流程，
 * 因為兩者的 concern 不同——SMART 是「授權第三方 App 存取 FHIR 資源」，
 * GTW IdP 是「透過外部 IdP 進行身份驗證」。
 * Client 透過 .well-known/smart-configuration 的 authorization_endpoint 欄位
 * 動態取得此端點的 URL，規範不限定路徑格式（Conformance line 168）。
 *
 * 驗證分兩層：
 * 1. 結構驗證（SmartAuthorizeRequest.validate()）：必填欄位、格式、PKCE 一致性
 * 2. 業務驗證（SmartAuthorizeService.validateRequest()）：aud/client_id/redirect_uri/scope 查 DB 比對
 *
 * 規格依據：
 * - HL7 SMART App Launch STU2.2
 * - OAuth 2.0（RFC 6749 §4.1.1、§4.1.2.1）
 * - OIDC Core 3.1.2.1（GET 和 POST 都必須支援）
 */
@RestController
public class SmartAuthorizeController {

	private final SmartAuthorizeService smartAuthorizeService;

	@Autowired
	public SmartAuthorizeController(SmartAuthorizeService smartAuthorizeService) {
		this.smartAuthorizeService = smartAuthorizeService;
	}

	// ==================== 授權端點 ====================

	/**
	 * 處理 GET 授權請求。
	 *
	 * 背景：OIDC Core 3.1.2.1 要求 GET 時參數以 URI Query String 傳遞。
	 * 大多數 SMART App 使用 GET（瀏覽器 redirect）。
	 */
	@GetMapping(value = "/dgrv4/ssotoken/smart/authorize")
	public ResponseEntity<?> authorizeGet(@RequestParam Map<String, String> queryParams,
			HttpServletRequest httpRequest) {
		TPILogger.tl.info("\n--【SMART Authorize (GET)】--");
		return handleAuthorize(queryParams, httpRequest);
	}

	/**
	 * 處理 POST 授權請求。
	 *
	 * 背景：OIDC Core 3.1.2.1 要求 POST 時參數以 Form Serialization 傳遞，
	 * Content-Type 為 application/x-www-form-urlencoded。
	 */
	@PostMapping(value = "/dgrv4/ssotoken/smart/authorize",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<?> authorizePost(@RequestParam Map<String, String> formParams,
			HttpServletRequest httpRequest) {
		TPILogger.tl.info("\n--【SMART Authorize (POST)】--");
		return handleAuthorize(formParams, httpRequest);
	}

	// ==================== 共用處理邏輯 ====================

	/**
	 * GET 和 POST 共用的授權處理邏輯。
	 *
	 * 流程：
	 * 1. 將參數轉為 SmartAuthorizeRequest
	 * 2. 結構驗證（必填、格式、PKCE）
	 * 3. 呼叫 processAuthorize：業務驗證 + 建立 session + 組裝 redirect URL
	 * 4. 成功時 302 redirect 到 ssotoken
	 *
	 * 錯誤分兩種：
	 * - redirect_uri 尚未驗證（結構驗證失敗）或驗證失敗 → 直接回傳錯誤頁（不 redirect）
	 * - redirect_uri 已驗證通過後才發生的錯誤 → redirect 到 app 帶 error（目前統一走錯誤頁，安全優先）
	 */
	private ResponseEntity<?> handleAuthorize(Map<String, String> params, HttpServletRequest httpRequest) {
		SmartAuthorizeRequest request = SmartAuthorizeRequest.fromQueryParams(params);
		try {
			request.validate();
		} catch (SmartAuthorizeException e) {
			TPILogger.tl.debug("[SMART Authorize] Structural validation failed: " + e.getMessage());
			return errorPage(e.getError(), e.getDescription());
		}

		try {
			String scheme = resolveScheme(httpRequest);
			String host = resolveHost(httpRequest);
			int port = resolvePort(httpRequest);

			AuthorizeResult result = smartAuthorizeService.processAuthorize(request, scheme, host, port);

			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(URI.create(result.redirectUrl()));
			return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();

		} catch (SmartAuthorizeException e) {
			TPILogger.tl.debug("[SMART Authorize] Business validation failed: " + e.getMessage());
			return errorPage(e.getError(), e.getDescription());
		}
	}

	// ==================== scheme/host/port 解析 ====================

	private String resolveScheme(HttpServletRequest req) {
		String forwarded = req.getHeader("X-Forwarded-Proto");
		return (forwarded != null && !forwarded.isBlank()) ? forwarded.trim() : req.getScheme();
	}

	private String resolveHost(HttpServletRequest req) {
		String forwarded = req.getHeader("X-Forwarded-Host");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return req.getServerName();
	}

	private int resolvePort(HttpServletRequest req) {
		String forwarded = req.getHeader("X-Forwarded-Port");
		if (forwarded != null && !forwarded.isBlank()) {
			try {
				return Integer.parseInt(forwarded.trim());
			} catch (NumberFormatException ignored) {
			}
		}
		return req.getServerPort();
	}

	// ==================== 錯誤頁 ====================

	/**
	 * 當 redirect_uri 無效或尚未驗證時，直接顯示錯誤頁（不 redirect）。
	 *
	 * 背景：RFC 6749 §4.1.2.1 指出，當 redirect_uri 無效或 client_id 不合法時，
	 * 授權伺服器「不應」redirect 到 redirect_uri，而應直接通知 resource owner（即顯示錯誤頁）。
	 */
	private ResponseEntity<?> errorPage(String error, String description) {
		String html = "<html><body><h2>Authorization Error</h2>"
				+ "<p><strong>Error:</strong> " + escape(error) + "</p>"
				+ "<p><strong>Description:</strong> " + escape(description) + "</p>"
				+ "</body></html>";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.TEXT_HTML)
				.body(html);
	}

	private String escape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}
}
