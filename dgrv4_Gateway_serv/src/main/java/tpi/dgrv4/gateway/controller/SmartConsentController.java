package tpi.dgrv4.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.SmartConsentService;
import tpi.dgrv4.gateway.service.SmartConsentService.ConsentInfo;
import tpi.dgrv4.gateway.service.SmartConsentService.ConsentResult;

/**
 * SMART App Launch Consent 端點。
 *
 * 背景：autoApprove=N 的 SMART client 在 callback 後導向 consent 頁面；
 * 使用者在 consent 頁面可以選擇批准（approve，可選取 requestedScope 子集）
 * 或拒絕（deny）。此 controller 接收使用者的 consent 決策並回傳適當的 redirect。
 *
 * GatewayFilter 已 bypass /dgrv4/ssotoken/smart/approve、/dgrv4/ssotoken/smart/deny
 * 與 /dgrv4/ssotoken/smart/consent-info。
 */
@RestController
public class SmartConsentController {

    private final SmartConsentService smartConsentService;

    @Autowired
    public SmartConsentController(SmartConsentService smartConsentService) {
        this.smartConsentService = smartConsentService;
    }

    /**
     * 使用者批准 consent。
     *
     * @param state          SMART session 的 state
     * @param approvedScopes 使用者選擇批准的 scope（空格分隔）
     * @param patientId      病患 ID（選填；grantedScope 含 launch/patient 時必填）
     * @param encounterId    就醫 ID（選填；grantedScope 含 launch/encounter 時必填）
     */
    @PostMapping("/dgrv4/ssotoken/smart/approve")
    public ResponseEntity<?> approve(
            @RequestParam("state") String state,
            @RequestParam("approved_scopes") String approvedScopes,
            @RequestParam(value = "patient_id", required = false) String patientId,
            @RequestParam(value = "encounter_id", required = false) String encounterId,
            @RequestParam(value = "fhir_user", required = false) String fhirUser) {

        TPILogger.tl.info("\n--【SMART Consent Approve】-- state=" + state);

        ConsentResult result = smartConsentService.approve(state, approvedScopes, patientId, encounterId, fhirUser);

        return switch (result) {
            case ConsentResult.Redirect redirect ->
                    ResponseEntity.status(302).header("Location", redirect.url()).build();

            case ConsentResult.FatalError fatal ->
                    ResponseEntity.badRequest().body("Consent error: " + fatal.message());
        };
    }

    /**
     * 取得 consent 頁面所需的顯示資訊。
     *
     * <p>不需 DGR Bearer token（GatewayFilter 已 bypass）。
     *
     * @param state SMART session 的 state
     * @return 200 ConsentInfo JSON；state 無效或 phase 非 AUTHENTICATED 則回傳 400
     */
    @GetMapping("/dgrv4/ssotoken/smart/consent-info")
    public ResponseEntity<?> getConsentInfo(
            @RequestParam("state") String state) {

        TPILogger.tl.info("\n--【SMART Consent Info】-- state=" + state);

        try {
            ConsentInfo info = smartConsentService.getConsentInfo(state);
            return ResponseEntity.ok(info);
        } catch (IllegalArgumentException | IllegalStateException e) {
            TPILogger.tl.warn("[SmartConsent] getConsentInfo error: " + e.getMessage());
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "invalid_state");
            error.put("message", "Session not found or not in consent phase");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 使用者拒絕 consent。
     *
     * @param state SMART session 的 state
     */
    @PostMapping("/dgrv4/ssotoken/smart/deny")
    public ResponseEntity<?> deny(
            @RequestParam("state") String state) {

        TPILogger.tl.info("\n--【SMART Consent Deny】-- state=" + state);

        ConsentResult result = smartConsentService.deny(state);

        return switch (result) {
            case ConsentResult.Redirect redirect ->
                    ResponseEntity.status(302).header("Location", redirect.url()).build();

            case ConsentResult.FatalError fatal ->
                    ResponseEntity.badRequest().body("Consent error: " + fatal.message());
        };
    }
}
