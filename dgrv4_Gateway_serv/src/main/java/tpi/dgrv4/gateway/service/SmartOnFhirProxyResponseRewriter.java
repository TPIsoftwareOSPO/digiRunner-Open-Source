package tpi.dgrv4.gateway.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * Smart on FHIR Proxy 的 Response Body URL 改寫器。
 *
 * <p>當後端 FHIR Server 回傳 HTML/JS/CSS 時，將其中的 root-relative path
 * （如 {@code /css/bootstrap.css}、{@code /fhir/Patient}）加上 proxy 路徑前綴
 * （如 {@code /smart-on-fhir/hapi-tw}），使瀏覽器能正確通過 proxy 載入資源。</p>
 *
 * <p>JSON/FHIR+JSON/XML 只做絕對 URL 替換（後端 origin → proxy 絕對 URL），
 * 不做 root-relative 改寫或 script 注入。二進位等不處理。</p>
 *
 * <p>反向：寫入時（POST/PUT/PATCH）透過 {@link #rewriteRequestBody} 將 proxy URL 還原為後端 URL。</p>
 */
public class SmartOnFhirProxyResponseRewriter {

	private static final Set<String> REWRITABLE_TYPES = Set.of(
		"text/html",
		"text/css"
	);

	/**
	 * 判斷 + 改寫（主入口）。
	 *
	 * <p>若 Content-Type 不在改寫範圍內，直接回傳原始 body。
	 * 若有 Content-Encoding（gzip/deflate），先解壓再改寫，並移除 Content-Encoding header。</p>
	 *
	 * @param body        response body（byte[]）
	 * @param proxyPrefix proxy 路徑前綴，如 {@code "/smart-on-fhir/hapi-tw"}
	 * @param respHeaders response headers（會被修改：移除 Content-Encoding、更新 Content-Length）
	 * @return 改寫後的 body，或原始 body（若不需改寫）
	 */
	public static byte[] rewriteIfNeeded(
			byte[] body,
			String proxyPrefix,
			Map<String, List<String>> respHeaders) {
		return rewriteIfNeeded(body, proxyPrefix, null, respHeaders);
	}

	/**
	 * 判斷 + 改寫（含絕對 URL 改寫）。向後相容，proxyBaseUrl = null。
	 */
	public static byte[] rewriteIfNeeded(
			byte[] body,
			String proxyPrefix,
			String targetUrl,
			Map<String, List<String>> respHeaders) {
		return rewriteIfNeeded(body, proxyPrefix, targetUrl, null, respHeaders);
	}

	/**
	 * 判斷 + 改寫（完整版）。
	 *
	 * <ul>
	 *   <li>HTML/CSS：絕對 URL → 相對 proxy 前綴 + root-relative 改寫 + script 注入</li>
	 *   <li>JSON/XML 等文字：絕對 URL → 絕對 proxy URL（FHIR 規範需要完整 URL）</li>
	 *   <li>二進位：不處理</li>
	 * </ul>
	 *
	 * @param body         response body（byte[]）
	 * @param proxyPrefix  proxy 路徑前綴，如 {@code "/smart-on-fhir/fhir"}
	 * @param targetUrl    後端 origin，如 {@code "https://hapi.fhir.tw"}
	 * @param proxyBaseUrl proxy 絕對 URL，如 {@code "https://localhost:18080/smart-on-fhir/fhir"}；
	 *                     null 則跳過 JSON/XML 絕對 URL 改寫
	 * @param respHeaders  response headers（會被修改）
	 * @return 改寫後的 body，或原始 body（若不需改寫）
	 */
	public static byte[] rewriteIfNeeded(
			byte[] body,
			String proxyPrefix,
			String targetUrl,
			String proxyBaseUrl,
			Map<String, List<String>> respHeaders) {
		return rewriteIfNeeded(body, proxyPrefix, targetUrl, proxyBaseUrl, respHeaders, null);
	}

	/**
	 * 判斷 + 改寫（完整版，含 resourcePath）。
	 *
	 * @param resourcePath 當前請求的 resource 路徑（如 {@code "/fhir/swagger-ui/"}），
	 *                     用於計算 HTML {@code <base>} 標籤的正確目錄路徑；
	 *                     null 則 fallback 為 proxyPrefix + "/"
	 */
	public static byte[] rewriteIfNeeded(
			byte[] body,
			String proxyPrefix,
			String targetUrl,
			String proxyBaseUrl,
			Map<String, List<String>> respHeaders,
			String resourcePath) {

		if (body == null || body.length == 0) {
			return body;
		}

		boolean fullRewrite = shouldRewrite(respHeaders);          // HTML/CSS
		boolean absOnly = !fullRewrite && proxyBaseUrl != null
			&& isTextContentType(respHeaders);                     // JSON/XML 等文字

		if (!fullRewrite && !absOnly) {
			return body;
		}

		try {
			// 1. 處理壓縮
			byte[] decompressed = decompressIfNeeded(body, respHeaders);
			String text = new String(decompressed, StandardCharsets.UTF_8);

			if (fullRewrite) {
				// HTML/CSS：絕對 URL → 絕對 proxy URL，root-relative 改寫，script 注入
				String absReplacement = (proxyBaseUrl != null) ? proxyBaseUrl : proxyPrefix;
				text = rewriteAbsoluteUrls(text, absReplacement, targetUrl);
				text = rewriteUrls(text, proxyPrefix);
				String contentType = getContentType(respHeaders);
				if (contentType != null && contentType.toLowerCase().contains("text/html")) {
					text = injectProxyScript(text, proxyPrefix, targetUrl, resourcePath);
				}
			} else {
				// JSON/XML：絕對 URL → 絕對 proxy URL（FHIR 需要完整 URL）
				text = rewriteAbsoluteUrls(text, proxyBaseUrl, targetUrl);
			}

			// 2. 更新 headers
			byte[] result = text.getBytes(StandardCharsets.UTF_8);
			stripContentEncoding(respHeaders);
			updateContentLength(respHeaders, result.length);

			return result;
		} catch (Exception e) {
			TPILogger.tl.error("SmartOnFhirProxyResponseRewriter error, returning original body: " + e.getMessage());
			return body;
		}
	}

	/**
	 * 改寫 request body（反向：proxy URL → 後端 URL）。
	 *
	 * <p>用於 POST/PUT/PATCH：client 送來的 body 可能包含 proxy 絕對 URL，
	 * 寫入後端前需還原為後端的 canonical URL。</p>
	 *
	 * @param body         request body
	 * @param proxyBaseUrl proxy 絕對 URL，如 {@code "https://localhost:18080/smart-on-fhir/fhir"}
	 * @param targetUrl    後端 origin，如 {@code "https://hapi.fhir.tw"}
	 * @return 改寫後的 body，若無需改寫則回傳原始 body
	 */
	public static byte[] rewriteRequestBody(byte[] body, String proxyBaseUrl, String targetUrl) {
		if (body == null || body.length == 0) {
			return body;
		}
		if (proxyBaseUrl == null || targetUrl == null) {
			return body;
		}

		String text = new String(body, StandardCharsets.UTF_8);
		String proxy = proxyBaseUrl.endsWith("/")
			? proxyBaseUrl.substring(0, proxyBaseUrl.length() - 1) : proxyBaseUrl;
		String target = targetUrl.endsWith("/")
			? targetUrl.substring(0, targetUrl.length() - 1) : targetUrl;

		// proxy URL → 後端 URL
		String rewritten = text.replace(proxy, target);

		// 也處理 http ↔ https 變體
		if (proxy.startsWith("https://")) {
			rewritten = rewritten.replace("http://" + proxy.substring(8), target);
		} else if (proxy.startsWith("http://")) {
			rewritten = rewritten.replace("https://" + proxy.substring(7), target);
		}

		return rewritten.equals(text) ? body : rewritten.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * 判斷是否需要改寫。
	 * 根據 Content-Type header 判斷，只對 text/html、JS、CSS 進行改寫。
	 */
	static boolean shouldRewrite(Map<String, List<String>> respHeaders) {
		String contentType = getContentType(respHeaders);
		if (contentType == null) {
			return false;
		}
		// Content-Type 可能帶 charset，如 "text/html; charset=utf-8"
		String mimeType = contentType.split(";")[0].trim().toLowerCase();
		return REWRITABLE_TYPES.contains(mimeType);
	}

	/**
	 * 判斷是否為文字型 Content-Type（JSON/XML 等）。
	 * 用於決定是否需要緩衝 response body 做絕對 URL 改寫。
	 */
	static boolean isTextContentType(Map<String, List<String>> respHeaders) {
		String contentType = getContentType(respHeaders);
		if (contentType == null) {
			return false;
		}
		String mime = contentType.split(";")[0].trim().toLowerCase();
		return mime.startsWith("text/")
			|| mime.contains("json")
			|| mime.contains("xml")
			|| mime.contains("javascript");
	}

	/**
	 * URL 改寫核心邏輯。
	 *
	 * <p>正則說明：
	 * <pre>
	 * (?<=["'])    前面是引號（lookbehind），不包含左括號以避免破壞 JS 正則表達式
	 * /            根斜線
	 * (?!/)        後面不是斜線（排除 protocol-relative URL 如 //cdn.example.com）
	 * (?!prefix)   後面不是已有的 proxy 前綴（防止雙重改寫）
	 * </pre>
	 *
	 * <p>範例：
	 * <ul>
	 *   <li>{@code href="/css/bootstrap.css"} → {@code href="/smart-on-fhir/hapi-tw/css/bootstrap.css"}</li>
	 *   <li>{@code fetch("/fhir/Patient")} → {@code fetch("/smart-on-fhir/hapi-tw/fhir/Patient")}</li>
	 *   <li>{@code src="//cdn.example.com"} → 不動</li>
	 *   <li>{@code (/regex/flags)} → 不動（前面是左括號，不匹配）</li>
	 *   <li>已有前綴的 URL → 不動</li>
	 * </ul>
	 */
	/**
	 * 改寫絕對 URL。
	 *
	 * <p>將後端 origin（如 {@code https://hapi.fhir.tw}）替換為 proxy 前綴。
	 * 同時處理 http/https 兩種 scheme。不影響 JSON/FHIR 回應（由 shouldRewrite 控管）。</p>
	 */
	static String rewriteAbsoluteUrls(String text, String proxyPrefix, String targetUrl) {
		if (targetUrl == null || targetUrl.isEmpty()) {
			return text;
		}
		// 去尾 slash，統一格式
		String target = targetUrl.endsWith("/")
			? targetUrl.substring(0, targetUrl.length() - 1) : targetUrl;

		text = text.replace(target, proxyPrefix);

		// 也處理 http ↔ https 的變體
		if (target.startsWith("https://")) {
			text = text.replace("http://" + target.substring(8), proxyPrefix);
		} else if (target.startsWith("http://")) {
			text = text.replace("https://" + target.substring(7), proxyPrefix);
		}
		return text;
	}

	static String rewriteUrls(String text, String proxyPrefix) {
		// proxyPrefix = "/smart-on-fhir/hapi-tw"
		// escaped = "smart\-on\-fhir/hapi\-tw"（去掉開頭的 /）
		String escaped = Pattern.quote(proxyPrefix.substring(1));
		String regex = "(?<=[\"'])/(?!/)(?!" + escaped + ")";
		return text.replaceAll(regex, proxyPrefix + "/");
	}

	/**
	 * 在 HTML {@code <head>} 中注入 runtime proxy 腳本。
	 *
	 * <p>攔截 JS 在 runtime 產生的 root-relative URL 請求，自動加上 proxy 路徑前綴。
	 * 涵蓋範圍：
	 * <ul>
	 *   <li>{@code fetch()} 和 {@code XMLHttpRequest}（涵蓋 jQuery $.ajax）</li>
	 *   <li>{@code <a>} 點擊事件</li>
	 *   <li>{@code <form>} 表單送出</li>
	 *   <li>動態新增的 DOM 元素（透過 MutationObserver 監聽 src/href/action 屬性）</li>
	 * </ul>
	 *
	 * <p>這比改寫 JS 原始碼更安全，不會破壞 minified JS 中的正則表達式。</p>
	 */
	static String injectProxyScript(String html, String proxyPrefix, String targetUrl, String resourcePath) {
		// <base> 讓瀏覽器正確解析相對路徑
		// HTML 規範：只有第一個 <base> 生效，注入在 <head> 最前面可確保優先
		// 使用 proxyPrefix + resourcePath 的目錄路徑，確保子目錄頁面（如 swagger-ui）的相對資源能正確載入
		String basePath;
		if (resourcePath != null && !resourcePath.isEmpty()) {
			basePath = proxyPrefix + resourcePath;
			if (!basePath.endsWith("/")) {
				int lastSlash = basePath.lastIndexOf('/');
				basePath = basePath.substring(0, lastSlash + 1);
			}
		} else {
			basePath = proxyPrefix + "/";
		}
		String baseTag = "<base href=\"" + basePath + "\">";

		// targetOrigin：去尾 slash，傳入 JS 用於比對絕對 URL
		String targetOrigin = "";
		if (targetUrl != null && !targetUrl.isEmpty()) {
			targetOrigin = targetUrl.endsWith("/")
				? targetUrl.substring(0, targetUrl.length() - 1) : targetUrl;
		}

		String script = "<script data-proxy-injected>"
			+ "(function(p,o){"

			// rw：集中處理 root-relative + 絕對 URL 改寫
			+ "var alt=o?(o.indexOf('https://')===0?'http://'+o.substring(8)"
			+ ":'https://'+o.substring(7)):'';"
			+ "function rw(u){"
			+ "if(typeof u!=='string')return u;"
			// root-relative path：/xxx → /smart-on-fhir/name/xxx
			+ "if(u.charAt(0)==='/'&&u.charAt(1)!=='/'&&u.indexOf(p)!==0)return p+u;"
			// 絕對 URL：https://target/xxx → /smart-on-fhir/name/xxx
			+ "if(o&&u.indexOf(o)===0)return p+u.substring(o.length);"
			+ "if(alt&&u.indexOf(alt)===0)return p+u.substring(alt.length);"
			+ "return u}"

			// 1. Patch fetch
			+ "if(window.fetch){var F=window.fetch;window.fetch=function(u,opts){"
			+ "if(typeof u==='string')u=rw(u);return F.call(this,u,opts)};}"

			// 2. Patch XMLHttpRequest.open（涵蓋 jQuery $.ajax / $.get / $.post）
			+ "var X=XMLHttpRequest.prototype.open;"
			+ "XMLHttpRequest.prototype.open=function(m,u,a,s,w){"
			+ "if(typeof u==='string')u=rw(u);return X.call(this,m,u,a,s,w)};"

			// 3. Intercept <a> clicks
			+ "document.addEventListener('click',function(e){"
			+ "var a=e.target;while(a&&a.tagName!=='A')a=a.parentElement;"
			+ "if(!a)return;var h=a.getAttribute('href');"
			// 3a. 修正 <base> 副作用：href="#..." 和 href="?..." 會被解析到 <base> 路徑而非當前頁面
			+ "if(h&&(h.charAt(0)==='#'||h.charAt(0)==='?')){"
			+ "e.preventDefault();"
			+ "if(h.charAt(0)==='#'){history.pushState(null,'',window.location.pathname+window.location.search+h);"
			+ "var t;try{t=document.querySelector(h)}catch(x){}"
			+ "if(t)t.scrollIntoView();window.dispatchEvent(new HashChangeEvent('hashchange'))}"
			+ "else window.location.href=window.location.pathname+h;"
			+ "return}"
			// 3b. root-relative / 絕對 URL 改寫
			+ "var n=rw(h);if(h&&n!==h)a.setAttribute('href',n)},true);"

			// 4. Intercept form submissions（event listener + prototype patch）
			// 4a. 使用者觸發的 submit（點擊按鈕、按 Enter）
			+ "document.addEventListener('submit',function(e){"
			+ "var a=e.target.getAttribute('action');"
			+ "var n=rw(a);if(a&&n!==a)e.target.setAttribute('action',n)},true);"
			// 4b. JS 程式化呼叫 form.submit()（不觸發 submit event，需 patch prototype）
			+ "var FS=HTMLFormElement.prototype.submit;"
			+ "HTMLFormElement.prototype.submit=function(){"
			+ "var a=this.getAttribute('action');"
			+ "var n=rw(a);if(a&&n!==a)this.setAttribute('action',n);"
			+ "return FS.call(this)};"

			// 5. MutationObserver：攔截 JS 動態建立的 DOM 元素
			+ "function f(el){['src','href','action'].forEach(function(t){"
			+ "var v=el.getAttribute&&el.getAttribute(t);"
			+ "var n=rw(v);if(v&&n!==v)el.setAttribute(t,n);"
			+ "if(t==='href'&&v&&v.charAt(0)==='#')el.setAttribute(t,window.location.pathname+window.location.search+v)"
			+ "})}"
			+ "new MutationObserver(function(ms){ms.forEach(function(m){"
			+ "m.addedNodes.forEach(function(nd){"
			+ "if(nd.nodeType===1){f(nd);if(nd.querySelectorAll)"
			+ "nd.querySelectorAll('[src],[href],[action]').forEach(f)}"
			+ "})})})"
			+ ".observe(document.documentElement,{childList:true,subtree:true});"

			// 6. 頁面載入時改寫 fragment-only href，修正 <base> 導致的懸停預覽 URL 錯誤
			+ "document.addEventListener('DOMContentLoaded',function(){"
			+ "document.querySelectorAll('a[href^=\"#\"]').forEach(function(a){"
			+ "var h=a.getAttribute('href');"
			+ "if(h)a.setAttribute('href',window.location.pathname+window.location.search+h)"
			+ "})})"

			+ "})('" + proxyPrefix + "','" + targetOrigin + "')</script>";

		String injection = baseTag + script;

		// 注入到 <head> 標籤後方（case-insensitive）
		String lower = html.toLowerCase();
		int idx = lower.indexOf("<head");
		if (idx >= 0) {
			int closeTag = html.indexOf('>', idx);
			if (closeTag >= 0) {
				return html.substring(0, closeTag + 1) + injection + html.substring(closeTag + 1);
			}
		}
		// fallback：放在最前面
		return injection + html;
	}

	/**
	 * 如果 response 有 Content-Encoding（gzip/deflate），進行解壓。
	 * 若無壓縮或不認識的編碼，直接回傳原始 byte[]。
	 */
	static byte[] decompressIfNeeded(byte[] body, Map<String, List<String>> respHeaders)
			throws IOException, DataFormatException {

		String encoding = getContentEncoding(respHeaders);
		if (encoding == null) {
			return body;
		}

		encoding = encoding.toLowerCase().trim();
		if (encoding.contains("gzip")) {
			return gzipDecompress(body);
		} else if (encoding.contains("deflate")) {
			return deflateDecompress(body);
		}

		return body;
	}

	/**
	 * gzip 解壓（保留換行符，不用 readLine）。
	 */
	static byte[] gzipDecompress(byte[] compressed) throws IOException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
			 GZIPInputStream gis = new GZIPInputStream(bis);
			 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[4096];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			return bos.toByteArray();
		}
	}

	/**
	 * deflate 解壓。
	 */
	static byte[] deflateDecompress(byte[] compressed) throws DataFormatException {
		try (Inflater inflater = new Inflater()) {
			inflater.setInput(compressed);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(compressed.length);
			byte[] buffer = new byte[4096];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				bos.write(buffer, 0, count);
			}
			return bos.toByteArray();
		}
	}

	/**
	 * 從 respHeaders 取得 Content-Type（不分大小寫）。
	 */
	static String getContentType(Map<String, List<String>> respHeaders) {
		return getHeaderValue(respHeaders, "Content-Type");
	}

	/**
	 * 從 respHeaders 取得 Content-Encoding（不分大小寫）。
	 */
	static String getContentEncoding(Map<String, List<String>> respHeaders) {
		return getHeaderValue(respHeaders, "Content-Encoding");
	}

	/**
	 * 移除 Content-Encoding header（因為已解壓）。
	 */
	static void stripContentEncoding(Map<String, List<String>> respHeaders) {
		respHeaders.entrySet().removeIf(
			entry -> entry.getKey() != null
				&& entry.getKey().equalsIgnoreCase("Content-Encoding"));
	}

	/**
	 * 更新 Content-Length header。
	 */
	static void updateContentLength(Map<String, List<String>> respHeaders, int newLength) {
		// 先移除舊的
		respHeaders.entrySet().removeIf(
			entry -> entry.getKey() != null
				&& entry.getKey().equalsIgnoreCase("Content-Length"));
		// 設定新的
		respHeaders.put("Content-Length", List.of(String.valueOf(newLength)));
	}

	/**
	 * 不分大小寫地取得 header 第一個值。
	 */
	private static String getHeaderValue(Map<String, List<String>> headers, String name) {
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					return values.get(0);
				}
			}
		}
		return null;
	}
}
