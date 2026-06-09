package tpi.dgrv4.gateway.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.vo.SmartConfigurationDto;

/**
 * 處理 SMART App Launch .well-known/smart-configuration 端點的服務。
 *
 * 背景：從 {@link SmartOnFhirProxyService} 拆分而來，
 * 讓 discovery 邏輯可獨立測試，並集中 CORS preflight 的處理。
 *
 * 支援的 HTTP method：
 * - GET：回應 SmartConfigurationDto JSON
 * - OPTIONS：回應 CORS preflight（無 body）
 *
 * CORS：SMART App Launch STU2.2 規格要求此端點必須支援跨來源存取。
 * 前提：部署時 DGR 前方有 Nginx，以 proxy_set_header 覆蓋模式注入
 * X-Forwarded-Proto / X-Forwarded-Host，client 無法偽造。
 */
@Service
public class SmartDiscoveryService {

	// ===== Content Types =====
	private static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";

	// ===== CORS Headers =====
	private static final String CORS_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private static final String CORS_ALLOW_METHODS = "Access-Control-Allow-Methods";
	private static final String CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	private static final String CORS_MAX_AGE = "Access-Control-Max-Age";

	// ===== HTTP Headers =====
	private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
	private static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
	private static final String HEADER_HOST = "Host";

	private final ObjectMapper objectMapper;

	@Autowired
	public SmartDiscoveryService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 處理 .well-known/smart-configuration 的請求，依 HTTP method 分派。
	 *
	 * @param request  原始 Servlet request
	 * @param response Servlet response（直接寫入）
	 * @throws Exception 序列化或 IO 錯誤
	 */
	public void handleDiscovery(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String method = request.getMethod();
		if ("OPTIONS".equalsIgnoreCase(method)) {
			handleOptions(response);
		} else {
			handleGet(request, response);
		}
	}

	// ==================== 私有處理方法 ====================

	/**
	 * 處理 GET 請求：組裝 SmartConfigurationDto 並寫入 response。
	 */
	private void handleGet(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String baseUrl = resolveBaseUrl(request);
		SmartConfigurationDto config = SmartConfigurationDto.createDefault(baseUrl);
		byte[] body = objectMapper.writeValueAsBytes(config);

		setCorsHeaders(response);
		response.setHeader(CORS_ALLOW_METHODS, "GET");
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(CONTENT_TYPE_JSON_UTF8);
		writeBody(response, body);
	}

	/**
	 * 處理 OPTIONS preflight：回傳 CORS headers，無 body。
	 *
	 * 設計決策：Max-Age 設 86400（24 小時）以減少 preflight 頻率，
	 * 符合 SMART App Launch 規格對高頻 client 端的建議。
	 */
	private void handleOptions(HttpServletResponse response) {
		setCorsHeaders(response);
		response.setHeader(CORS_ALLOW_METHODS, "GET, OPTIONS");
		response.setHeader(CORS_ALLOW_HEADERS, "Accept, Authorization");
		response.setHeader(CORS_MAX_AGE, "86400");
		response.setStatus(HttpStatus.OK.value());
	}

	/**
	 * 設定所有請求共用的 CORS 基本 header。
	 */
	private void setCorsHeaders(HttpServletResponse response) {
		response.setHeader(CORS_ALLOW_ORIGIN, "*");
	}

	/**
	 * 從 request header 推算 DGR 的對外 base URL（scheme + host，不含路徑）。
	 *
	 * 優先使用 X-Forwarded-Proto / X-Forwarded-Host（由前方 Nginx 注入），
	 * 其次 fallback 到 Host header 或 server 設定。
	 * 逗號分隔（多層代理）時取第一個值。
	 *
	 * 範例：
	 *   Nginx 注入 X-Forwarded-Proto: https, X-Forwarded-Host: dgr.example.com
	 *   → 回傳 "https://dgr.example.com"
	 *
	 *   直連 localhost:18080
	 *   → 回傳 "http://localhost:18080"
	 */
	String resolveBaseUrl(HttpServletRequest request) {
		// 1. Scheme：優先使用 X-Forwarded-Proto
		String scheme = request.getHeader(HEADER_X_FORWARDED_PROTO);
		if (scheme == null || scheme.isEmpty()) {
			scheme = request.getScheme();
		}
		if (scheme.contains(",")) {
			scheme = scheme.split(",")[0].trim();
		}

		// 2. Host：優先使用 X-Forwarded-Host，其次 Host header，最後 server 設定
		String host = request.getHeader(HEADER_X_FORWARDED_HOST);
		if (host == null || host.isEmpty()) {
			host = request.getHeader(HEADER_HOST);
		}
		if (host == null || host.isEmpty()) {
			host = request.getServerName();
			int port = request.getServerPort();
			if (port != 80 && port != 443) {
				host += ":" + port;
			}
		}
		if (host.contains(",")) {
			host = host.split(",")[0].trim();
		}

		return scheme + "://" + host;
	}

	/**
	 * 將 byte[] body 寫入 response。
	 */
	private void writeBody(HttpServletResponse response, byte[] body) throws IOException {
		response.setContentLength(body.length);
		response.getOutputStream().write(body);
	}
}
