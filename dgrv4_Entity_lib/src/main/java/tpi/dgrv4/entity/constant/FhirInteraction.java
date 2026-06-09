package tpi.dgrv4.entity.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * FHIR RESTful Interaction 類型
 * 參考: FHIR RESTful API Specification
 * 
 * 用於識別 FHIR RESTful API 的請求類型,並提供 URL 模式匹配功能
 * 注意：此實作用於轉發/路由目的，不驗證參數是否符合 FHIR 規範
 */
@Getter
public enum FhirInteraction implements CodeEnum<String> {

	/**
	 * 讀取特定資源實例
	 * GET /[type]/[id]
	 */
	READ("read",
		    new PathPattern("^/(\\w+)/([^/?]+)(?:\\?.*)?$", "GET", "type", "id")),

	/**
	 * 讀取特定版本的資源
	 * GET /[type]/[id]/_history/[vid]
	 */
	VREAD("vread",
		    new PathPattern("^/(\\w+)/([^/?]+)/_history/([^/?]+)(?:\\?.*)?$", "GET", "type", "id", "vid")),

	/**
	 * 更新資源
	 * PUT /[type]/[id]
	 */
	UPDATE("update",
	    new PathPattern("^/(\\w+)/([^/?]+)(?:\\?.*)?$", "PUT", "type", "id")),

	/**
	 * 部分更新資源
	 * PATCH /[type]/[id]
	 */
	PATCH("patch",
	    new PathPattern("^/(\\w+)/([^/?]+)(?:\\?.*)?$", "PATCH", "type", "id")),

	/**
	 * 刪除資源
	 * DELETE /[type]/[id]
	 */
	DELETE("delete",
	    new PathPattern("^/(\\w+)/([^/?]+)(?:\\?.*)?$", "DELETE", "type", "id")),

	/**
	 * 建立新資源
	 * POST /[type]
	 */
	CREATE("create", 
	    new PathPattern("^/(\\w+)/?$", "POST", "type")),

	/**
	 * 搜尋特定類型的資源
	 * GET /[type]?...
	 * POST /[type]/_search?...
	 */
	SEARCH_TYPE("search-type", 
	    new PathPattern("^/(\\w+)\\?.*$", "GET", "type"),
	    new PathPattern("^/(\\w+)/_search(?:\\?.*)?$", "POST", "type")),

	/**
	 * 全系統搜尋
	 * GET /?...
	 * POST /_search?...
	 */
	SEARCH_SYSTEM("search-system", 
	    new PathPattern("^/\\?.*$", "GET"), 
	    new PathPattern("^/_search(?:\\?.*)?$", "POST")),

	/**
	 * Compartment 搜尋
	 * GET /[compartment]/[id]/*?...
	 * GET /[compartment]/[id]/[type]?...
	 * POST /[compartment]/[id]/_search?...
	 * POST /[compartment]/[id]/[type]/_search?...
	 */
	SEARCH_COMPARTMENT("search-compartment", 
	    new PathPattern("^/(\\w+)/([^/?]+)/\\*\\?.*$", "GET", "compartment", "id"),
	    new PathPattern("^/(\\w+)/([^/?]+)/(\\w+)\\?.*$", "GET", "compartment", "id", "type"),
	    new PathPattern("^/(\\w+)/([^/?]+)/_search(?:\\?.*)?$", "POST", "compartment", "id"),
	    new PathPattern("^/(\\w+)/([^/?]+)/(\\w+)/_search(?:\\?.*)?$", "POST", "compartment", "id", "type")),

	/**
	 * 查詢伺服器能力聲明
	 * GET /metadata
	 */
	CAPABILITIES("capabilities",
	    new PathPattern("^/metadata(?:\\?.*)?$", "GET")),

	/**
	 * 查詢全系統的歷史版本
	 * GET /_history?...
	 */
	HISTORY_SYSTEM("history-system", 
	    new PathPattern("^/_history(?:\\?.*)?$", "GET")),

	/**
	 * 查詢特定類型資源的歷史版本
	 * GET /[type]/_history?...
	 */
	HISTORY_TYPE("history-type", 
	    new PathPattern("^/(\\w+)/_history(?:\\?.*)?$", "GET", "type")),

	/**
	 * 查詢特定資源實例的歷史版本
	 * GET /[type]/[id]/_history?...
	 */
	HISTORY_INSTANCE("history-instance", 
	    new PathPattern("^/(\\w+)/([^/?]+)/_history(?:\\?.*)?$", "GET", "type", "id"));

	private final String code;
	private final List<PathPattern> patterns;

	FhirInteraction(String code, PathPattern... patterns) {
		this.code = code;
		this.patterns = Collections.unmodifiableList(Arrays.asList(patterns));
	}

	/**
	 * 檢查指定的 URL 和 HTTP Method 是否符合此 Interaction
	 * 
	 * @param url    URL 路徑(不含 domain,如 /Patient/123)
	 * @param method HTTP Method (GET, POST, PUT, PATCH, DELETE)
	 * @return 是否匹配
	 */
	public boolean matches(String url, String method) {
		if (url == null || method == null) {
			return false;
		}
		return patterns.stream().anyMatch(pattern -> pattern.matches(url, method));
	}

	/**
	 * 從 URL 中提取參數
	 * 
	 * @param url    URL 路徑
	 * @param method HTTP Method
	 * @return 提取的參數 Map,如 {type: "Patient", id: "123"}
	 */
	public Map<String, String> extractParams(String url, String method) {
		if (url == null || method == null) {
			return Collections.emptyMap();
		}

		return patterns.stream()
			.filter(pattern -> pattern.matches(url, method))
			.findFirst()
			.map(pattern -> pattern.extractParams(url))
			.orElse(Collections.emptyMap());
	}

	/**
	 * 根據 URL 和 HTTP Method 識別 FHIR Interaction
	 * 
	 * @param url    URL 路徑(不含 domain,如 /Patient/123)
	 * @param method HTTP Method (GET, POST, PUT, PATCH, DELETE)
	 * @return 識別出的 FhirInteraction
	 */
	public static Optional<FhirInteraction> identify(String url, String method) {
		if (url == null || method == null) {
			return Optional.empty();
		}

	    // 正規化 URL: 移除開頭的多餘斜線,保留查詢參數
	    String normalizedUrl = url.replaceAll("^/+", "/");

	    // 按照優先順序匹配(從具體到一般)
	    // 優先順序設計原則：
	    // 1. 特殊端點優先（metadata）
	    // 2. 精確路徑優先（_history, _search 在根路徑）
	    // 3. 路徑段較多的優先（避免被較短的模式誤判）
	    // 4. 通用模式最後（CRUD 和 SEARCH_TYPE）
	    return Stream.of(
	        // 最高優先：特殊端點
	        CAPABILITIES,               // /metadata
	        
	        // 次高優先：包含特殊路徑段的操作（從長到短）
	        VREAD,                      // /[type]/[id]/_history/[vid]
	        HISTORY_INSTANCE,           // /[type]/[id]/_history
	        SEARCH_COMPARTMENT,         // /[compartment]/[id]/[type]/_search 等
	        HISTORY_TYPE,               // /[type]/_history
	        
	        // 重要：根路徑的特殊操作必須在 SEARCH_TYPE 之前
	        // 避免 /_history 和 /_search 被誤判為資源類型搜尋
	        HISTORY_SYSTEM,             // /_history
	        SEARCH_SYSTEM,              // /_search
	        
	        // 通用搜尋模式（會匹配 /[type]?...）
	        SEARCH_TYPE,                // /[type]?... 或 /[type]/_search
	        
	        // 最低優先：CRUD 操作（最通用的模式）
	        READ, UPDATE, PATCH, DELETE, CREATE
	    )
	    .filter(interaction -> interaction.matches(normalizedUrl, method))
	    .findFirst();

	}

	/**
	 * 識別 FHIR Interaction 並提取參數
	 * 
	 * @param url    URL 路徑
	 * @param method HTTP Method
	 * @return 包含 Interaction 和參數的結果
	 */
	public static IdentifyResult identifyWithParams(String url, String method) {
		return identify(url, method)
			.map(interaction -> {
				Map<String, String> params = interaction.extractParams(url, method);
				return new IdentifyResult(interaction, params);
			})
			.orElse(new IdentifyResult(null, Collections.emptyMap()));
	}

	/**
	 * 根據 code 查找 Interaction (使用 CodeEnums 工具)
	 */
	public static Optional<FhirInteraction> fromCode(String code) {
		return CodeEnums.tryFromCode(FhirInteraction.class, code);
	}

	/**
	 * 驗證 code 是否有效
	 */
	public static boolean isValidCode(String code) {
		return CodeEnums.isValidCode(FhirInteraction.class, code);
	}

	/**
	 * URL 模式定義
	 * 包含正則表達式和 HTTP Method
	 */
	@Getter
	public static class PathPattern {
		private final Pattern pattern;
		private final String method;
		private final String patternString;
		private final List<String> paramNames;

	    public PathPattern(String regex, String method, String... paramNames) {
	        this.patternString = regex;
	        this.pattern = Pattern.compile(regex);
	        this.method = method.toUpperCase();
	        this.paramNames = paramNames != null ? 
	            Collections.unmodifiableList(Arrays.asList(paramNames)) : 
	            Collections.emptyList();
	    }

		
	    /**
	     * 從 URL 中提取參數
	     * 根據預定義的參數名稱映射提取
	     */
	    public Map<String, String> extractParams(String url) {
	        Matcher matcher = pattern.matcher(url);
	        if (!matcher.matches()) {
	            return Collections.emptyMap();
	        }

	        Map<String, String> params = new LinkedHashMap<>();
	        int groupCount = matcher.groupCount();

	        // 根據預定義的參數名稱提取
	        for (int i = 0; i < Math.min(groupCount, paramNames.size()); i++) {
	            String value = matcher.group(i + 1);
	            if (value != null && !value.isEmpty()) {
	                params.put(paramNames.get(i), value);
	            }
	        }

	        return params;
	    }

		/**
		 * 檢查 URL 和 Method 是否匹配
		 */
		public boolean matches(String url, String method) {
			if (url == null || method == null) {
				return false;
			}
			return this.method.equalsIgnoreCase(method) && pattern.matcher(url).matches();
		}

		@Override
		public String toString() {
			return method + " " + patternString;
		}
	}

	/**
	 * 識別結果
	 * 包含識別出的 Interaction 和提取的參數
	 */
	@Getter
	public static class IdentifyResult {
		private final FhirInteraction interaction;
		private final Map<String, String> params;

		public IdentifyResult(FhirInteraction interaction, Map<String, String> params) {
			this.interaction = interaction;
			this.params = Collections.unmodifiableMap(params);
		}

		public boolean isIdentified() {
			return interaction != null;
		}

		public String getType() {
			return params.get("type");
		}

		public String getId() {
			return params.get("id");
		}

		public String getVid() {
			return params.get("vid");
		}

		public String getCompartment() {
			return params.get("compartment");
		}

		@Override
		public String toString() {
			return "IdentifyResult{" +
				"interaction=" + interaction +
				", params=" + params +
				'}';
		}
	}
}
