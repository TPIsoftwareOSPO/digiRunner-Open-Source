package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartCallbackService;
import tpi.dgrv4.gateway.service.SmartCallbackService.CallbackResult;

/**
 * ssotoken IdP 認證完成後的 callback 端點。
 *
 * 背景：SMART authorize 將使用者導向 ssotoken 登入頁時，
 * 帶入此端點作為 redirect_uri；ssotoken 認證後帶著 dgRcode 和 state 跳回。
 * 此 controller 串接兩個系統，完成 SMART 授權流程的身份驗證階段。
 */
@RestController
public class SmartCallbackController {

    private final SmartCallbackService smartCallbackService;

    @Autowired
    public SmartCallbackController(SmartCallbackService smartCallbackService) {
        this.smartCallbackService = smartCallbackService;
    }

    /**
     * 接收 ssotoken 的 callback。
     *
     * @param code  ssotoken 產生的 dgRcode
     * @param state 原始 SMART 授權請求的 state（原封不動帶回）
     */
    @GetMapping("/dgrv4/ssotoken/smart/callback")
    public ResponseEntity<?> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "rtn_code", required = false) String rtnCode,
            @RequestParam(value = "msg", required = false) String msg) {

        TPILogger.tl.info("\n--【SMART Callback】-- state=" + state);

        // GTW IdP 設定異常：ssotoken 還沒走到認證就失敗，不會帶 state
        if (state == null || state.isBlank()) {
            String errorMsg = msg != null ? decodeBase64(msg) : "Unknown error (no state)";
            TPILogger.tl.warn("[SMART Callback] No state parameter. rtn_code=" + rtnCode + ", msg=" + errorMsg);
            return ResponseEntity.badRequest().body("Authorization error: " + errorMsg);
        }

        if (code == null || code.isBlank()) {
            String errorMsg = (error != null) ? error : "access_denied";
            TPILogger.tl.warn("[SMART Callback] IdP returned error: " + errorMsg);
            CallbackResult idpError = smartCallbackService.handleIdpError(state, errorMsg);
            return toResponse(idpError);
        }

        CallbackResult result = smartCallbackService.processCallback(code, state);

        return toResponse(result);
    }

    private String decodeBase64(String encoded) {
        try {
            return new String(java.util.Base64.getDecoder().decode(encoded), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }

    private ResponseEntity<?> toResponse(CallbackResult result) {
        return switch (result) {
            case CallbackResult.Approved approved ->
                    ResponseEntity.status(302).header("Location", approved.redirectUrl()).build();

            case CallbackResult.NeedConsent needConsent ->
                    ResponseEntity.status(302).header("Location", needConsent.consentUrl()).build();

            case CallbackResult.Error err ->
                    ResponseEntity.status(302).header("Location", err.redirectUrl()).build();

            case CallbackResult.FatalError fatal ->
                    ResponseEntity.badRequest().body("Authorization error: " + fatal.message());
        };
    }
}
