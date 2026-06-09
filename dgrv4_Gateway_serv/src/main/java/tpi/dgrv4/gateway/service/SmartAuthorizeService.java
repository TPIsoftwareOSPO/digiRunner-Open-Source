package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrSmartAuthSession;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.entity.DgrSmartLaunchContext;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.repository.DgrSmartAuthSessionDao;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.entity.repository.DgrSmartLaunchContextDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext.ProxyPathParts;
import tpi.dgrv4.gateway.vo.SmartAuthorizeRequest;
import tpi.dgrv4.gateway.vo.SmartAuthorizeRequest.SmartAuthorizeException;
import tpi.dgrv4.gateway.vo.SmartConfigurationDto;

/**
 * SMART App Launch 授權業務驗證服務。
 *
 * 背景：SMART App 發起授權請求時，需要依序驗證多個參數的業務合法性：
 * 1. aud — 對應合法的 FHIR Proxy route
 * 2. client_id — 已註冊的 OAuth client
 * 3. redirect_uri — 在該 client 的白名單內
 * 4. scope — 在該 client 被授權的範圍內
 *
 * 結構驗證（必填、格式）由 SmartAuthorizeRequest.validate() 負責，
 * 本 Service 只處理需要查 DB 的業務驗證。
 */
@Service
public class SmartAuthorizeService {

	private final DgrSmartOnFhirProxyDao proxyDao;
	private final DgrSmartOnFhirProxyDiversionDao diversionDao;
	private final OauthClientDetailsDao oauthClientDetailsDao;
	private final DgrSmartClientDao smartClientDao;
	private final DgrSmartAuthSessionDao sessionDao;
	private final DgrSmartLaunchContextDao launchContextDao;

	@Autowired
	public SmartAuthorizeService(DgrSmartOnFhirProxyDao proxyDao,
			DgrSmartOnFhirProxyDiversionDao diversionDao,
			OauthClientDetailsDao oauthClientDetailsDao,
			DgrSmartClientDao smartClientDao,
			DgrSmartAuthSessionDao sessionDao,
			DgrSmartLaunchContextDao launchContextDao) {
		this.proxyDao = proxyDao;
		this.diversionDao = diversionDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.smartClientDao = smartClientDao;
		this.sessionDao = sessionDao;
		this.launchContextDao = launchContextDao;
	}

	// ==================== 驗證結果 ====================

	/**
	 * 授權請求的業務驗證結果，供後續授權流程使用。
	 *
	 * @param websiteName  從 aud 解析出的 proxy 名稱
	 * @param fhirBasePath 從 aud 解析出的 FHIR base path（正規化後）
	 * @param proxy        對應的 FHIR Proxy 實體
	 * @param client       對應的 OAuth client 實體
	 * @param smartClient  對應的 SMART client 設定（含 idpType、allowedScopes、autoApprove 等）
	 */
	public record AuthorizeValidationResult(
			String websiteName,
			String fhirBasePath,
			DgrSmartOnFhirProxy proxy,
			OauthClientDetails client,
			DgrSmartClient smartClient) {
	}

	/**
	 * processAuthorize() 的回傳結果。
	 *
	 * @param redirectUrl ssotoken 的授權端點 URL（302 redirect 目的地）
	 * @param session     已建立並儲存的 PENDING session
	 */
	public record AuthorizeResult(
			String redirectUrl,
			DgrSmartAuthSession session) {
	}

	// ==================== 主驗證流程 ====================

	/**
	 * 對 SMART 授權請求進行業務驗證。
	 *
	 * 背景：結構驗證（必填、格式）已由 SmartAuthorizeRequest.validate() 完成，
	 * 此方法接手做需要查 DB 的驗證，依序為：
	 * 1. aud → 確認 FHIR Proxy route 存在且 fhirBasePath 命中
	 * 2. client_id → 確認 OAuth client 已註冊
	 * 3. redirect_uri → 確認在該 client 的白名單內
	 * 4. scope → 確認在該 client 的授權範圍內
	 *
	 * 範例：
	 *   SmartAuthorizeRequest req = SmartAuthorizeRequest.fromQueryParams(params);
	 *   req.validate();
	 *   AuthorizeValidationResult result = smartAuthorizeService.validateRequest(req);
	 *
	 * @param request 已通過結構驗證的授權請求
	 * @return 驗證結果，包含解析出的路由資訊和 client 實體
	 * @throws SmartAuthorizeException 驗證失敗時拋出，含 OAuth error code 和描述
	 */
	public AuthorizeValidationResult validateRequest(SmartAuthorizeRequest request) {
		// 1. 驗證 aud（FHIR Proxy route 存在且 fhirBasePath 命中）
		AudResult audResult = validateAud(request.aud());

		// 2. 驗證 client_id（OAuth client 已註冊 + SMART 設定存在）
		OauthClientDetails client = validateClientId(request.clientId());
		DgrSmartClient smartClient = validateSmartClient(request.clientId());

		// 3. 驗證 redirect_uri（在 SMART client 的白名單內）
		validateRedirectUri(request.redirectUri(), smartClient);

		// 4. 驗證 scope（系統支援 + client 授權範圍）
		validateScope(request.scope(), smartClient);

		// 5. 驗證 launchMode（帶 launch 參數時，client 必須支援 EHR launch）
		validateLaunchMode(request, smartClient);

		// 6. 驗證 scope launch（scope 含 "launch" 時，必須帶 launch 參數）
		validateScopeLaunch(request);

		return new AuthorizeValidationResult(
				audResult.websiteName, audResult.fhirBasePath,
				audResult.proxy, client, smartClient);
	}

	// ==================== authorize 主流程 ====================

	/**
	 * 執行完整的 SMART authorize 流程：驗證請求、強制 PKCE、建立 session、組裝 ssotoken redirect URL。
	 *
	 * 流程：
	 * 1. validateRequest（aud、client_id、redirect_uri、scope 查 DB）
	 * 2. PKCE 強制：public client 必須帶 code_challenge
	 * 3. state 唯一性：不可與現有 PENDING/AUTHENTICATED session 的 state 相同
	 * 4. 建立 DgrSmartAuthSession（phase=PENDING）並儲存
	 * 5. 組裝 ssotoken redirect URL
	 *
	 * @param request 已通過結構驗證的授權請求
	 * @param scheme  呼叫端的 scheme（http / https），用於組裝 callback URL
	 * @param host    呼叫端的 host，用於組裝 callback URL
	 * @param port    呼叫端的 port，用於組裝 callback URL
	 * @return 含 redirectUrl 和 session 的結果
	 * @throws SmartAuthorizeException 驗證失敗時拋出
	 */
	public AuthorizeResult processAuthorize(SmartAuthorizeRequest request,
			String scheme, String host, int port) {
		AuthorizeValidationResult validation = validateRequest(request);

		// 錯誤模擬：auth_* 系列在驗證通過後、建立 session 前注入
		injectSimulatedAuthError(request.simError());

		enforcePkce(request, validation.smartClient());
		ensureStateIsUnique(request.state());

		DgrSmartAuthSession session = buildSession(request, validation);
		sessionDao.save(session);

		String redirectUrl = buildSsotokenRedirectUrl(
				validation.smartClient().getIdpType(),
				validation.smartClient().getIdpClientId(),
				scheme, host, port, session.getState());

		return new AuthorizeResult(redirectUrl, session);
	}

	private void enforcePkce(SmartAuthorizeRequest request, DgrSmartClient smartClient) {
		if ("public".equals(smartClient.getClientType()) && request.codeChallenge() == null) {
			throw new SmartAuthorizeException("invalid_request",
					"PKCE is required for public clients: code_challenge must be provided");
		}
	}

	private void ensureStateIsUnique(String state) {
		Optional<DgrSmartAuthSession> existing = sessionDao.findByState(state);
		if (existing.isEmpty()) {
			return;
		}
		String phase = existing.get().getPhase();
		if ("PENDING".equals(phase) || "AUTHENTICATED".equals(phase)) {
			throw new SmartAuthorizeException("invalid_request",
					"state is already in use by an active session");
		}
	}

	private DgrSmartAuthSession buildSession(SmartAuthorizeRequest request,
			AuthorizeValidationResult validation) {
		DgrSmartAuthSession session = new DgrSmartAuthSession();
		session.setState(request.state());
		session.setClientId(request.clientId());
		session.setAud(request.aud());
		session.setWebsiteName(validation.websiteName());
		session.setFhirBasePath(validation.fhirBasePath());
		session.setRequestedScope(List.of(request.scope().trim().split("\\s+")));
		session.setRedirectUri(request.redirectUri());
		session.setCodeChallenge(request.codeChallenge());
		session.setCodeChallengeMethod(request.codeChallengeMethod());
		session.setPhase("PENDING");
		session.setLaunch(request.launch());
		session.setSimError(request.simError());

		// EHR Launch：解析 launch token 並將 context 寫入 session
		if (StringUtils.hasText(request.launch())) {
			resolveLaunchContext(request.launch(), request.clientId(), session);
		}

		return session;
	}

	/**
	 * 查詢並驗證 launch token，將 patient/encounter context 寫入 session，並以原子操作標記 token 為已消費。
	 *
	 * 驗證步驟：
	 * 1. launch token 必須存在
	 * 2. launch token 未過期（expireAt > 現在時間）
	 * 3. launch token 未被消費（consumed = false）—— 快速失敗保護
	 * 4. launch token 的 client_id 與請求的 client_id 一致
	 * 5. 寫入 context 至 session
	 * 6. 原子消費：consumeByLaunchToken 回傳 0 表示被搶先消費 —— race condition 防護
	 *
	 * 步驟 3 和步驟 6 均檢查 consumed，是雙重保護：
	 * 步驟 3 快速失敗（避免不必要的後續處理），步驟 6 原子防護（防 TOCTOU）。
	 *
	 * @param launchToken EHR 提供的不透明 launch token
	 * @param clientId    授權請求的 client_id
	 * @param session     待寫入 patientId / encounterId 的 session
	 * @throws SmartAuthorizeException launch token 驗證失敗時拋出 invalid_request
	 */
	@Transactional
	void resolveLaunchContext(String launchToken, String clientId,
			DgrSmartAuthSession session) {
		// 1. token 必須存在（不洩漏 token 值至錯誤訊息）
		DgrSmartLaunchContext ctx = launchContextDao.findByLaunchToken(launchToken)
				.orElseThrow(() -> new SmartAuthorizeException("invalid_request",
						"Launch token not found"));

		// 2. 未過期
		if (System.currentTimeMillis() > ctx.getExpireAt()) {
			throw new SmartAuthorizeException("invalid_request",
					"Launch token has expired");
		}

		// 3. 快速失敗：已消費（避免不必要的後續處理）
		if (Boolean.TRUE.equals(ctx.getConsumed())) {
			throw new SmartAuthorizeException("invalid_request",
					"Launch token has already been used");
		}

		// 4. client_id 一致性檢查
		if (!clientId.equals(ctx.getClientId())) {
			throw new SmartAuthorizeException("invalid_request",
					"Launch token was not issued to this client");
		}

		// 5. 寫入 context 至 session
		session.setPatientId(ctx.getPatientId());
		session.setEncounterId(ctx.getEncounterId());
		session.setFhirUserType(ctx.getFhirUserType());

		// Patient 候選清單（多選）
		List<String> patientIds = ctx.getPatientIds();
		if (patientIds != null && !patientIds.isEmpty() && !StringUtils.hasText(session.getPatientId())) {
			session.setPatientCandidates(patientIds);
			if (patientIds.size() == 1) {
				session.setPatientId(patientIds.get(0));
			}
		}

		// Provider 候選清單與自動帶入
		// 注意：providerIds 只在 Provider 端 launch（Practitioner type）有意義；
		// Patient Portal launch 的 fhirUser 由 patient pick 推導，不靠 providerIds。
		List<String> providerIds = ctx.getProviderIds();
		boolean isPatientFhirUser = "patient".equalsIgnoreCase(ctx.getFhirUserType())
				|| isPatientPortalScope(session.getRequestedScope());
		if (providerIds != null && !providerIds.isEmpty() && !isPatientFhirUser) {
			session.setProviderCandidates(providerIds);
			if (providerIds.size() == 1) {
				session.setFhirUser("Practitioner/" + providerIds.get(0));
			}
		}

		// 6. 原子消費：防止 TOCTOU race condition
		int affected = launchContextDao.consumeByLaunchToken(launchToken);
		if (affected == 0) {
			throw new SmartAuthorizeException("invalid_request",
					"Launch token has already been used");
		}
	}

	/**
	 * 判斷 requested scope 是否屬於 Patient Portal context（fhirUser 對應到 Patient resource）。
	 * launch/patient scope 是 SMART 規格中 Patient Portal Launch 的標記。
	 */
	private boolean isPatientPortalScope(List<String> scope) {
		if (scope == null) return false;
		return scope.contains("launch/patient");
	}

	private String buildSsotokenRedirectUrl(String idpType, String idpClientId,
			String scheme, String host, int port, String state) {
		String callbackUrl = buildCallbackUrl(scheme, host, port);
		return "/dgrv4/ssotoken/gtwidp/" + idpType + "/authorization"
				+ "?response_type=code"
				+ "&client_id=" + idpClientId
				+ "&redirect_uri=" + urlEncode(callbackUrl)
				+ "&scope=openid"
				+ "&state=" + urlEncode(state);
	}

	private String buildCallbackUrl(String scheme, String host, int port) {
		boolean isDefaultPort = ("https".equals(scheme) && port == 443)
				|| ("http".equals(scheme) && port == 80);
		String base = isDefaultPort
				? scheme + "://" + host
				: scheme + "://" + host + ":" + port;
		return base + "/dgrv4/ssotoken/smart/callback";
	}

	private String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	// ==================== aud 驗證 ====================

	private record AudResult(String websiteName, String fhirBasePath, DgrSmartOnFhirProxy proxy) {
	}

	/**
	 * 驗證 aud 參數並解析出路由資訊。
	 *
	 * 驗證流程：
	 * 1. 從 aud URL 取出路徑，用 extractProxyPathParts 拆出 websiteName 和剩餘路徑
	 * 2. 查 DB 確認 websiteName 對應的 Proxy 存在
	 * 3. 查該 Proxy 的所有 Diversion，確認剩餘路徑至少 match 其中一個的 fhirBasePath
	 *
	 * 範例：
	 *   aud = "https://dgr.example.com/smart-on-fhir/hapi-tw/fhir/r4"
	 *   → proxyName="hapi-tw", remainingPath="/fhir/r4"
	 *   → 查 DB 確認 hapi-tw 存在，且有 Diversion 的 fhirBasePath = "/fhir/r4"
	 */
	private AudResult validateAud(String aud) {
		String audPath = extractPath(aud);
		ProxyPathParts parts = SmartOnFhirProxyRouteContext.extractProxyPathParts(audPath);
		if (parts == null) {
			throw new SmartAuthorizeException("invalid_request",
					"Invalid aud: must contain /smart-on-fhir/{websiteName} path");
		}

		List<DgrSmartOnFhirProxy> proxyList = proxyDao.findBySofProxyName(parts.proxyName());
		if (proxyList == null || proxyList.isEmpty()) {
			throw new SmartAuthorizeException("invalid_request",
					"Unknown FHIR server: " + parts.proxyName());
		}
		DgrSmartOnFhirProxy proxy = proxyList.get(0);

		String fhirBasePath = SmartOnFhirProxyRouteContext.normalizeFhirBasePath(parts.remainingPath());
		List<DgrSmartOnFhirProxyDiversion> diversions = diversionDao.findBySofProxyId(proxy.getSofProxyId());

		boolean matched = diversions.stream()
				.map(d -> SmartOnFhirProxyRouteContext.normalizeFhirBasePath(d.getSofProxyDiversionFhirBasePath()))
				.anyMatch(fhirBasePath::equals);

		if (!matched) {
			throw new SmartAuthorizeException("invalid_request",
					"aud does not match any known FHIR base URL for: " + parts.proxyName());
		}

		return new AudResult(parts.proxyName(), fhirBasePath, proxy);
	}

	// ==================== client_id 驗證 ====================

	/**
	 * 驗證 client_id 對應已註冊的 OAuth client。
	 *
	 * 背景：DGR 的 OAuth client 資料存在 oauth_client_details 表，
	 * 以 client_id 為 primary key。
	 */
	private OauthClientDetails validateClientId(String clientId) {
		return oauthClientDetailsDao.findById(clientId)
				.orElseThrow(() -> new SmartAuthorizeException("invalid_client",
						"Unknown client_id: " + clientId));
	}

	// ==================== SMART client 驗證 ====================

	/**
	 * 驗證 client_id 對應的 SMART 設定存在。
	 *
	 * 背景：SMART client 設定存在 dgr_smart_client 表，
	 * 與 oauth_client_details 以 client_id 關聯。
	 * 沒有 SMART 設定的 client 不能發起 SMART authorize 請求。
	 */
	private DgrSmartClient validateSmartClient(String clientId) {
		return smartClientDao.findByClientId(clientId)
				.orElseThrow(() -> new SmartAuthorizeException("invalid_client",
						"SMART configuration not found for client: " + clientId));
	}

	// ==================== redirect_uri 驗證 ====================

	/**
	 * 驗證 redirect_uri 在 SMART client 的白名單內。
	 *
	 * 背景：SMART client 的 redirect URI 存在 dgr_smart_client.redirect_uris，
	 * 以 List 儲存。請求的 redirect_uri 必須精確匹配其中一個。
	 */
	private void validateRedirectUri(String redirectUri, DgrSmartClient smartClient) {
		List<String> redirectUris = smartClient.getRedirectUris();
		if (redirectUris == null || redirectUris.isEmpty()) {
			throw new SmartAuthorizeException("invalid_request",
					"No redirect URIs configured for client: " + smartClient.getClientId());
		}

		boolean matched = redirectUris.stream()
				.map(String::trim)
				.anyMatch(redirectUri::equals);

		if (!matched) {
			throw new SmartAuthorizeException("invalid_request",
					"redirect_uri is not registered for client: " + smartClient.getClientId());
		}
	}

	// ==================== scope 驗證 ====================

	/**
	 * 驗證請求的 scope 是否在系統支援清單內，且在 client 被授權的範圍內。
	 *
	 * 背景：scope 驗證分兩層：
	 * 1. 系統支援檢查：請求的 scope 是否在 SmartConfigurationDto.scopesSupported 內
	 * 2. Client 授權檢查：請求的 scope 是否在 DgrSmartClient.allowedScopes 內
	 *
	 * 比對規則：
	 * - FHIR resource scope 支援萬用符：granted "patient/*.rs" 涵蓋 requested "patient/Observation.rs"
	 * - 非 resource scope 做精確字串比對
	 *
	 * @param scope       請求的 scope 字串（空格分隔）
	 * @param smartClient SMART client 設定
	 * @throws SmartAuthorizeException scope 不在支援清單或授權範圍時拋出 invalid_scope
	 */
	private void validateScope(String scope, DgrSmartClient smartClient) {
		String[] requestedScopes = scope.trim().split("\\s+");

		// 第 1 層：系統支援檢查
		List<String> supportedScopes = SmartConfigurationDto.createDefault("").scopesSupported();
		for (String requested : requestedScopes) {
			boolean supported = supportedScopes.stream()
					.anyMatch(s -> scopeCovers(s, requested));
			if (!supported) {
				throw new SmartAuthorizeException("invalid_scope",
						"Unsupported scope: " + requested);
			}
		}

		// 第 2 層：Client 授權檢查
		List<String> allowedScopes = smartClient.getAllowedScopes();
		if (allowedScopes == null || allowedScopes.isEmpty()) {
			throw new SmartAuthorizeException("invalid_scope",
					"Client has no authorized scopes: " + smartClient.getClientId());
		}
		for (String requested : requestedScopes) {
			boolean allowed = allowedScopes.stream()
					.anyMatch(a -> scopeCovers(a, requested));
			if (!allowed) {
				throw new SmartAuthorizeException("invalid_scope",
						"Scope not authorized for client: " + requested);
			}
		}
	}

	// ==================== launch 模式驗證 ====================

	/**
	 * 驗證 launchMode：若請求帶有 launch 參數，client 必須支援 EHR launch。
	 *
	 * 背景：launchMode = "standalone" 的 client 不允許帶 launch 參數；
	 * launchMode = "ehr" 或 "both" 才能進行 EHR Launch 流程。
	 * launchMode 為 null 時視為不支援 EHR launch（預設 standalone）。
	 *
	 * @param request     授權請求
	 * @param smartClient SMART client 設定
	 * @throws SmartAuthorizeException 不支援 EHR launch 的 client 帶了 launch 參數時拋出
	 */
	private void validateLaunchMode(SmartAuthorizeRequest request, DgrSmartClient smartClient) {
		if (!StringUtils.hasText(request.launch())) {
			return;
		}
		String launchMode = smartClient.getLaunchMode();
		if (!"ehr".equals(launchMode) && !"both".equals(launchMode)) {
			throw new SmartAuthorizeException("invalid_request",
					"Client does not support EHR launch");
		}
	}

	/**
	 * 驗證 scope 含 "launch" 時必須帶 launch 參數。
	 *
	 * 背景：SMART spec 規定，若請求的 scope 包含 "launch"，
	 * 代表 App 期待 EHR 提供 launch context，此時 launch 參數為必填。
	 * 反之（帶 launch 參數但 scope 不含 "launch"）則不報錯。
	 *
	 * @param request 授權請求
	 * @throws SmartAuthorizeException scope 含 "launch" 但 launch 參數缺失時拋出
	 */
	private void validateScopeLaunch(SmartAuthorizeRequest request) {
		boolean scopeHasLaunch = Stream.of(request.scope().trim().split("\\s+"))
				.anyMatch("launch"::equals);
		if (scopeHasLaunch && !StringUtils.hasText(request.launch())) {
			throw new SmartAuthorizeException("invalid_request",
					"launch parameter is required when scope includes \"launch\"");
		}
	}

	/**
	 * 判斷 granted scope 是否涵蓋 requested scope。
	 *
	 * 比對規則：
	 * - 精確相等 → 涵蓋
	 * - 兩者都是 FHIR resource scope（含 "/" 和 "."）時，resource 和 permission 分別比對：
	 *   - resource：granted 為 "*" 時涵蓋所有 resource type
	 *   - permission：granted 的每個字母都必須涵蓋 requested 的每個字母
	 *
	 * 範例：
	 *   scopeCovers("patient/*.rs", "patient/Observation.rs")     → true
	 *   scopeCovers("patient/Observation.rs", "patient/Patient.rs") → false
	 *   scopeCovers("patient/*.cruds", "patient/Observation.rs")   → true
	 *   scopeCovers("openid", "openid")                            → true
	 */
	boolean scopeCovers(String granted, String requested) {
		if (granted.equals(requested)) {
			return true;
		}

		// 嘗試解析為 FHIR resource scope：context/resource.permission
		String[] gParts = parseFhirScope(granted);
		String[] rParts = parseFhirScope(requested);

		// 兩者都不是 FHIR scope → 只做精確比對（已在上面處理）
		if (gParts == null || rParts == null) {
			return false;
		}

		// context 必須相同（patient/user/system）
		if (!gParts[0].equals(rParts[0])) {
			return false;
		}

		// resource：granted 為 "*" 時涵蓋所有，否則精確比對
		if (!"*".equals(gParts[1]) && !gParts[1].equals(rParts[1])) {
			return false;
		}

		// permission：granted 的字母集合必須涵蓋 requested 的每個字母
		return permissionCovers(gParts[2], rParts[2]);
	}

	/**
	 * 解析 FHIR resource scope 為 [context, resource, permission]。
	 * 非 FHIR scope（如 openid、launch/patient）回傳 null。
	 *
	 * 範例：
	 *   "patient/Observation.rs" → ["patient", "Observation", "rs"]
	 *   "system/*.cruds"         → ["system", "*", "cruds"]
	 *   "openid"                 → null
	 *   "launch/patient"         → null（沒有 "." 分隔 permission）
	 */
	private String[] parseFhirScope(String scope) {
		String[] contextSplit = scope.split("/");
		if (contextSplit.length != 2) {
			return null;
		}
		String[] resourcePermSplit = contextSplit[1].split("\\.");
		if (resourcePermSplit.length != 2) {
			return null;
		}
		return new String[]{contextSplit[0], resourcePermSplit[0], resourcePermSplit[1]};
	}

	/**
	 * 判斷 granted permission 是否涵蓋 requested permission。
	 *
	 * - "*" 涵蓋一切
	 * - "write" 涵蓋 c、u、d
	 * - "read" 涵蓋 r
	 * - 字母組合（cruds）：granted 的每個字母是否包含 requested 的每個字母
	 */
	private boolean permissionCovers(String granted, String requested) {
		if ("*".equals(granted)) {
			return true;
		}
		if (granted.equals(requested)) {
			return true;
		}

		// 展開為字母集合後比對
		String gLetters = expandPermission(granted);
		String rLetters = expandPermission(requested);

		for (char c : rLetters.toCharArray()) {
			if (gLetters.indexOf(c) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 將 permission 展開為 cruds 字母。
	 * "*" → "cruds"，"write" → "cud"，"read" → "r"，其餘原樣。
	 */
	private String expandPermission(String permission) {
		return switch (permission) {
			case "*" -> "cruds";
			case "write" -> "cud";
			case "read" -> "rs";
			default -> permission;
		};
	}

	// ==================== 錯誤模擬 ====================

	private void injectSimulatedAuthError(String simError) {
		if (simError == null || simError.isEmpty()) return;
		switch (simError) {
			case "auth_invalid_client_id":
				throw new SmartAuthorizeException("invalid_client",
						"Simulated error: invalid client_id");
			case "auth_invalid_redirect_uri":
				throw new SmartAuthorizeException("invalid_request",
						"Simulated error: invalid redirect_uri");
			case "auth_invalid_scope":
				throw new SmartAuthorizeException("invalid_scope",
						"Simulated error: invalid scope");
			case "auth_invalid_client_secret":
				throw new SmartAuthorizeException("invalid_client",
						"Simulated error: invalid client_secret");
			default:
				break;
		}
	}

	// ==================== URL 工具 ====================

	/**
	 * 從完整 URL 取出路徑部分。
	 *
	 * 範例：
	 *   "https://dgr.example.com/smart-on-fhir/hapi-tw/fhir/r4" → "/smart-on-fhir/hapi-tw/fhir/r4"
	 */
	private String extractPath(String url) {
		try {
			return new URI(url).getPath();
		} catch (Exception e) {
			return url;
		}
	}

}
