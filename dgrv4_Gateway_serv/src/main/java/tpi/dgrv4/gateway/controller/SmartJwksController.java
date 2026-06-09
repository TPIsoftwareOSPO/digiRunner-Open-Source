package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.gateway.component.SmartKeyProvider;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * SMART on FHIR JWKS 端點。
 *
 * 背景：SMART App Launch STU2.2 要求 Authorization Server 在 JWKS URI 公告 RSA 公鑰，
 * 供 Resource Server 驗證 access token 的 RS256 簽章。
 * JWKS URI 在 /.well-known/smart-configuration 的 jwks_uri 欄位宣告。
 *
 * CORS 設定：
 * SMART App 通常在瀏覽器中執行（Single Page Application），需要跨來源讀取 JWKS。
 * 公鑰本身是公開資訊（非敏感），因此允許 Access-Control-Allow-Origin: *。
 *
 * 規格依據：
 * - HL7 SMART App Launch STU2.2 §4.3
 * - RFC 7517（JWK）
 * - RFC 7638（JWK Thumbprint，用於 kid）
 */
@RestController
public class SmartJwksController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SmartKeyProvider keyProvider;

    @Autowired
    public SmartJwksController(SmartKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    /**
     * 回傳 RSA 公鑰的 JWKS JSON。
     *
     * 流程：
     * 1. 呼叫 SmartKeyProvider.getPublicJwkJson() 取得單一 JWK JSON
     * 2. 包裝為 JWKS Set（{"keys": [JWK]}）
     * 3. 設定 CORS header，允許所有 origin（公鑰為公開資訊）
     * 4. 回傳 200 application/json
     *
     * 錯誤處理：
     * 若金鑰不可用（IllegalStateException），回傳 500 + OAuth 2.0 錯誤 JSON。
     */
    @GetMapping(value = "/dgrv4/ssotoken/smart/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getJwks() {
        TPILogger.tl.info("\n--【SMART JWKS】--");

        try {
            String jwkJson = keyProvider.getPublicJwkJson();
            Map<String, Object> jwk = OBJECT_MAPPER.readValue(jwkJson,
                new TypeReference<Map<String, Object>>() {});

            Map<String, Object> jwkSet = new LinkedHashMap<>();
            jwkSet.put("keys", List.of(jwk));

            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jwkSet);

        } catch (IllegalStateException e) {
            TPILogger.tl.error("[SMART JWKS] 金鑰不可用：" + e.getMessage());
            return serverError("SMART JWKS 金鑰暫時不可用，請聯繫系統管理員");

        } catch (Exception e) {
            TPILogger.tl.error("[SMART JWKS] 無法解析 JWK JSON：" + e.getMessage());
            return serverError("SMART JWKS 格式解析失敗");
        }
    }

    /**
     * CORS preflight 端點。
     *
     * 背景：瀏覽器在跨來源請求前，會先發送 OPTIONS preflight 確認伺服器允許的方法與 headers。
     * JWKS 端點需要支援此 preflight，讓 SMART App（SPA）能正常存取。
     */
    @RequestMapping(value = "/dgrv4/ssotoken/smart/jwks", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> jwksCorsOptions() {
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
            .build();
    }

    // ==================== 私有輔助方法 ====================

    /**
     * 組裝 OAuth 2.0 格式的伺服器錯誤回應。
     *
     * 使用 "server_error" 錯誤碼（RFC 6749 §5.2）。
     */
    private ResponseEntity<?> serverError(String description) {
        Map<String, String> errorBody = new LinkedHashMap<>();
        errorBody.put("error", "server_error");
        errorBody.put("error_description", description);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorBody);
    }
}
