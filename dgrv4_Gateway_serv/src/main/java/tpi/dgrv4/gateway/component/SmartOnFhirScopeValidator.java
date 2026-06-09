package tpi.dgrv4.gateway.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.SmartOnFhirTokenClaims;

/**
 * 驗證 Access Token 的 scope list 是否允許打此 API
 * 
 * @author Mini
 */
@Component
public class SmartOnFhirScopeValidator {

	// HTTP Method -> FHIR interaction 對應
	private static final Map<String, String> METHOD_TO_INTERACTION = new HashMap<>();
	static {
		METHOD_TO_INTERACTION.put("GET", "read");
		METHOD_TO_INTERACTION.put("POST", "create");
		METHOD_TO_INTERACTION.put("PUT", "update");
		METHOD_TO_INTERACTION.put("PATCH", "update");
		METHOD_TO_INTERACTION.put("DELETE", "delete");
	}

	// FHIR interaction -> 需要的字母（v2 用）
	private static final Map<String, String> INTERACTION_TO_LETTER = new HashMap<>();
	static {
		INTERACTION_TO_LETTER.put("read", "r");
		INTERACTION_TO_LETTER.put("search", "s");
		INTERACTION_TO_LETTER.put("create", "c");
		INTERACTION_TO_LETTER.put("update", "u");
		INTERACTION_TO_LETTER.put("delete", "d");
	}

	/**
	 * 判斷 Access Token 的 scope list 是否允許打此 API
	 *
	 * @param smartOnFhirTokenClaims token 內的 playload 資料
	 * @param httpMethod       HTTP method, e.g. "GET", "POST", "PUT", "DELETE"
	 * @param resourceType     FHIR resource type, e.g. "Patient", "Observation"
	 * @param isSearch         是否為 search 操作（GET 帶 ? 參數）
	 * @return true = 有權限
	 */
	public ResponseEntity<?> validateScope(SmartOnFhirTokenClaims smartOnFhirTokenClaims, String httpMethod,
			String resourceType, boolean isSearch, String reqUrl, boolean isBundleTransaction) {

		// token 內的 scope 清單, e.g. ["system/Patient.read", "system/*.cruds"]
		List<String> grantedScopes = smartOnFhirTokenClaims.getScopes();
		
		String errResult = "";
		boolean isAuthResult = false;
		if (isBundleTransaction) {// 打 Bundle Transaction API, 走專用判斷
			isAuthResult = isBundleTransactionAllowed(grantedScopes);
			if (!isAuthResult) {
				// 請求 Bundle Transaction API 時, scope 需包含 system/*.* 或 system/*.cruds
				errResult = "When requesting the Bundle Transaction API, the scope must include system/*.* or system/*.cruds";
			}

		} else {
			isAuthResult = isAuthorized(grantedScopes, httpMethod, resourceType, isSearch);
		}

		if (!isAuthResult) {
			String errorMsg = String.format("Insufficient scope. Granted: %s. %s", grantedScopes, errResult);
			TPILogger.tl.debug(errorMsg);
			return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errorMsg,
					HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);// 403
		}

		return null;
	}
	
	/**
	 * 判斷 Access Token 的 scope list 是否允許打 Bundle transaction API, <br>
	 * 要打 Bundle transaction API, <br>
	 * 1.直接要求最高權限 且 2.目前只接受 system subject(嚴格控管): <br>
	 * 即要求有 system/*.* 或 system/*.cruds <br>
	 * 
	 * @param grantedScopes Access Token 內的 scope 清單, e.g. ["system/Patient.read",
	 *                      "system/*.cruds"]
	 */
	boolean isBundleTransactionAllowed(List<String> grantedScopes) {
	    return grantedScopes.stream()
	            .map(String::trim)
	            // 1. 過濾：必須以 system/ 開頭
	            .filter(s -> s.startsWith("system/"))
	            // 2. 解析與過濾格式：必須能被 / 分成兩段
	            .map(s -> s.split("/"))
	            .filter(parts -> parts.length == 2)
	            // 3. 解析與過濾格式：第二部分必須能被 . 分成兩段
	            .map(parts -> parts[1].split("\\."))
	            .filter(rp -> rp.length == 2)
	            // 4. 核心邏輯判斷
	            .anyMatch(rp -> {
	                String resource = rp[0];
	                String permission = rp[1];
	                // 必須是 * 資源 且 有完整權限
	                return "*".equals(resource) && ("*".equals(permission) || "cruds".equals(permission));
	            });
	}

	/**
	 * 判斷 Access Token 的 scope list 是否允許打此 API（含 compartment 過濾）<br>
	 * 此多載在原有 scope/permission 驗證之上，額外針對 patient scope 實施 compartment 比對。
	 *
	 * @param claims         token 內的 payload 資料
	 * @param httpMethod     HTTP method, e.g. "GET", "POST", "PUT", "DELETE"
	 * @param resourceType   FHIR resource type, e.g. "Patient", "Observation"
	 * @param isSearch       是否為 search 操作（GET 帶 ? 參數）
	 * @param reqUrl         請求 URL（用於錯誤回應）
	 * @param resourceId     URL 中的 resource ID，例如 GET /Patient/123 → "123"；無法解析時為 null
	 * @param compartment    compartment 類型，例如 GET /Patient/123/Observation → "Patient"；無時為 null
	 * @param compartmentId  compartment ID，例如 GET /Patient/123/Observation → "123"；無時為 null
	 * @return null 代表通過；非 null 代表 ResponseEntity（403）
	 */
	public ResponseEntity<?> validateScope(SmartOnFhirTokenClaims claims, String httpMethod,
			String resourceType, boolean isSearch, String reqUrl,
			String resourceId, String compartment, String compartmentId) {

		List<String> grantedScopes = claims.getScopes();
		String interaction = resolveInteraction(httpMethod, isSearch);

		if (interaction == null) {
			String errorMsg = "[SMART on FHIR] Unsupported HTTP method: " + httpMethod;
			TPILogger.tl.debug(errorMsg);
			return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errorMsg,
					HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
		}

		TPILogger.tl.debug("[SMART on FHIR] Compartment scope validation, Request: "
				+ resourceType + " / " + interaction
				+ ", resourceId=" + resourceId
				+ ", compartment=" + compartment + "/" + compartmentId);

		for (String scope : grantedScopes) {
			ResponseEntity<?> scopeResult = checkScopeWithCompartment(
					scope, interaction, resourceType, claims, reqUrl, resourceId, compartment, compartmentId);
			// null 代表此 scope 通過
			if (scopeResult == null) {
				return null;
			}
			// 非 null 但狀態碼不是 FORBIDDEN 也算通過（防禦性處理，目前不會發生）
		}

		// 所有 scope 均未通過
		String errorMsg = String.format("Insufficient scope. Granted: %s", grantedScopes);
		TPILogger.tl.debug(errorMsg);
		return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN, errorMsg,
				HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
	}

	/**
	 * 檢查單一 scope 是否在 compartment 規則下允許此操作。<br>
	 * 回傳 null 代表此 scope 通過；回傳 ResponseEntity（403）代表此 scope 拒絕。
	 */
	private ResponseEntity<?> checkScopeWithCompartment(String scope, String interaction, String resourceType,
			SmartOnFhirTokenClaims claims, String reqUrl,
			String resourceId, String compartment, String compartmentId) {

		// 解析 scope 格式：context/resource.permission
		String[] contextSplit = scope.split("/");
		if (contextSplit.length != 2) {
			return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN,
					"Invalid scope format: " + scope,
					HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
		}

		String context = contextSplit[0]; // "patient" / "user" / "system"

		// 先確認基本 resource + permission 是否符合（不論 context）
		if (!scopeAllows(scope, interaction, resourceType)) {
			return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN,
					"Insufficient permission in scope: " + scope,
					HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
		}

		// system / user scope：不施加 compartment 限制
		if ("system".equals(context) || "user".equals(context)) {
			return null; // 通過
		}

		// patient scope：需要 patient claim
		if ("patient".equals(context)) {
			String patientClaim = claims.getPatient();
			if (patientClaim == null || patientClaim.isEmpty()) {
				return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp("insufficient_scope",
						"patient scope requires patient context",
						HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
			}

			// 直接存取 Patient 資源（GET /Patient/{id}）
			if ("Patient".equals(resourceType) && resourceId != null) {
				if (!patientClaim.equals(resourceId)) {
					return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN,
							"Access denied: patient compartment mismatch",
							HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
				}
				return null; // 通過
			}

			// Compartment search（GET /Patient/{id}/ResourceType）
			if ("Patient".equals(compartment) && compartmentId != null) {
				if (!patientClaim.equals(compartmentId)) {
					return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN,
							"Access denied: patient compartment mismatch",
							HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
				}
				return null; // 通過
			}

			// 其他路徑（無法從 URL 判斷 patient）：放行，交後端 FHIR server 過濾
			return null;
		}

		// 未知 context，保守拒絕
		return new ResponseEntity<>(TokenHelper.getOAuthTokenErrorResp(TokenHelper.FORBIDDEN,
				"Unknown scope context: " + context,
				HttpStatus.FORBIDDEN.value(), reqUrl), TokenHelper.setContentTypeHeader(), HttpStatus.FORBIDDEN);
	}

	/**
	 * 判斷 Access Token 的 scope list 是否允許打此 API
	 *
	 * @param grantedScopes Access Token 內的 scope 清單, e.g. ["system/Patient.read",
	 *                      "system/*.cruds"]
	 * @param httpMethod    HTTP method, e.g. "GET", "POST", "PUT", "DELETE"
	 * @param resourceType  FHIR resource type, e.g. "Patient", "Observation"
	 * @param isSearch      是否為 search 操作（GET 帶 ? 參數）
	 * @return true = 有權限
	 */
	public boolean isAuthorized(List<String> grantedScopes, String httpMethod, String resourceType, boolean isSearch) {
		// 決定 interaction
		String interaction = resolveInteraction(httpMethod, isSearch);
		if (interaction == null) {
			TPILogger.tl.debug("[SMART on FHIR] Unsupported HTTP methods: " + httpMethod);
			return false; // 不支援的 HTTP method
		}

		TPILogger.tl.debug("[SMART on FHIR] Starting scope validation, Request: " + resourceType + " / " + interaction);

		for (String scope : grantedScopes) {
			if (scopeAllows(scope, interaction, resourceType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析 HTTP method -> interaction
	 */
	private String resolveInteraction(String httpMethod, boolean isSearch) {
		if ("GET".equalsIgnoreCase(httpMethod)) {
			return isSearch ? "search" : "read";
		}
		return METHOD_TO_INTERACTION.get(httpMethod.toUpperCase());
	}

	/**
	 * 判斷單一 scope 是否允許此操作 <br>
	 * scope 格式: [context]/[resource].[permission] <br>
	 * context: system, patient, user <br>
	 */
	private boolean scopeAllows(String scope, String interaction, String resourceType) {
		// 格式驗證
		String[] contextSplit = scope.split("/");
		if (contextSplit.length != 2)
			return false;

		String[] resourcePermSplit = contextSplit[1].split("\\.");
		if (resourcePermSplit.length != 2)
			return false;

		String resource = resourcePermSplit[0];
		String permission = resourcePermSplit[1];

		// 比對 resource type（* 代表全部資源）
		boolean resourceMatch = "*".equals(resource) || resource.equals(resourceType);
		if (!resourceMatch)
			return false;

		// 比對 permission
		return permissionAllows(permission, interaction);
	}

	/**
	 * 判斷 permission 後綴是否允許此 interaction
	 */
	private boolean permissionAllows(String permission, String interaction) {

		// v1: * 允許所有操作
		if ("*".equals(permission))
			return true;

		// v1: write 允許 create / update / delete
		if ("write".equals(permission)) {
			return interaction.equals("create") || interaction.equals("update") || interaction.equals("delete");
		}

		// v1: read 允許 read 和 search
		if ("read".equals(permission)) {
			return interaction.equals("read") || interaction.equals("search");
		}

		// v1/v2: 完整關鍵字比對
		if (permission.equals(interaction))
			return true;

		// v2: 純字母組合（只能包含 c, r, u, d, s）
		if (permission.matches("[cruds]+")) {
			String requiredLetter = INTERACTION_TO_LETTER.get(interaction);
			if (requiredLetter != null) {
				return permission.contains(requiredLetter);
			}
		}

		return false;
	}
}