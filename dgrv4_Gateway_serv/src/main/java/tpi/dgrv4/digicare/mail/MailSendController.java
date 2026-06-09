package tpi.dgrv4.digicare.mail;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 內部寄信入口，給 dgr 自己的 GatewayFilter forward 進來呼叫。
 *
 * 設計背景：
 * digiCare 後端需要寄信能力，但不想自己接 SMTP，於是走 A 路線——
 * 在 dgr 註冊一支被代理 API（src_url 指回 dgr 自己的 /dgrv4/digicare/mail/send），
 * digiCare 透過 OAuth client_credentials 取 token 後打 /dgrc/...，
 * 由 dgr 既有的 GatewayFilter 走完整 client→group→api 授權鏈，
 * 通過後 forward 到本 controller，由本 controller 包一層
 * MailHelper.sendEmail 完成寄信。本 controller 本身不做授權，
 * 只做來源 IP 檢查確保不被外部直接打到。
 *
 * 路徑掛在 /dgrv4/digicare/mail 是為了對齊 dgr 既有 controller 風格——
 * dgrManagedRoute 集合（GatewayFilter 行 256-275）已含 /dgrv4/ 前綴，
 * 落在這個前綴下的請求會被 GatewayFilter 直接放行給 Spring DispatcherServlet
 * 處理，不會被改寫成 /dgrc/... 當被代理 API。註冊到 TSMP_API_REG 的
 * src_url 也指向同一個路徑，達成「dgr forward 回自己」的閉環。
 *
 * 安全：路徑落在 /dgrv4/ 對外可見的前綴下，理論上外部也能直接打到，
 * 因此必須在 controller 內檢查來源 IP，僅放行來自 localhost 的呼叫——
 * dgr 自己 forward 進來的 source 一定是 127.0.0.1 / ::1，外部呼叫直接 403。
 */
@RestController
@RequestMapping("/dgrv4/digicare/mail")
@RequiredArgsConstructor
public class MailSendController {

    /**
     * 允許呼叫的來源 IP 集合。
     *
     * 涵蓋 IPv4 與 IPv6 兩種 loopback 寫法。實測時 dgr forward 進來的
     * remoteAddr 字面值會是其中之一，視 JVM 的 IPv4/IPv6 偏好而定。
     */
    private static final Set<String> LOCALHOSTS = Set.of(
        "127.0.0.1",
        "0:0:0:0:0:0:0:1",
        "::1"
    );

    /**
     * createUser 寫進 tsmp_dp_mail_log 的固定識別字，方便事後追溯哪些
     * 信件是 digiCare 後端透過本 endpoint 寄出的。
     */
    private static final String CREATE_USER = "digiCareBackend";

    private final MailHelper mailHelper;

    /**
     * 接收寄信請求，呼叫 dgr 既有 MailHelper 完成投遞。
     *
     * 流程：
     * 1. 檢查來源 IP，非 localhost 直接 403（防止繞過 dgr 直打）
     * 2. 檢查必填欄位，缺漏回 400
     * 3. 組 TsmpMailEvent 呼叫 MailHelper.sendEmail
     * 4. 一律回 200 + accepted=true（MailHelper 內部 catch 所有例外，
     *    呼叫端無法知道投遞是否真成功；查 tsmp_dp_mail_log 才能確認）
     */
    @PostMapping("/send")
    public ResponseEntity<MailSendResponse> send(
            @RequestBody MailSendRequest req,
            HttpServletRequest httpReq) {

        // 來源 IP 檢查：只接受 dgr 自己 forward 進來的 loopback 呼叫
        String remote = httpReq.getRemoteAddr();
        if (!LOCALHOSTS.contains(remote)) {
            TPILogger.tl.warn(
                "Rejected /dgrv4/digicare/mail/send from non-localhost: " + remote);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 必填欄位檢查：dgr 沒引入 spring-boot-starter-validation，
        // @Valid + @NotBlank 不會觸發，改在這裡手動檢查
        String invalid = validate(req);
        if (invalid != null) {
            TPILogger.tl.warn(
                "Rejected /dgrv4/digicare/mail/send with invalid payload: " + invalid);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        TPILogger.tl.info(
            "/dgrv4/digicare/mail/send accepted: refCode=" + req.refCode()
            + ", recipients=" + req.recipients());

        // 組 TsmpMailEvent。MailHelper.sendEmail 內部會走 primary →
        // secondary SMTP fallback，並把結果（含 stack trace）寫入
        // tsmp_dp_mail_log。任何例外都不會 propagate 出來。
        TsmpMailEvent event = new TsmpMailEvent(
            req.subject(),
            req.htmlBody(),
            req.recipients(),
            CREATE_USER,
            req.refCode()
        );
        mailHelper.sendEmail(event);

        return ResponseEntity.ok(
            MailSendResponse.builder()
                .accepted(true)
                .refCode(req.refCode())
                .build()
        );
    }

    /**
     * 檢查必填欄位是否齊全。
     *
     * @return null 表示通過；否則回傳缺少的欄位名稱
     */
    private String validate(MailSendRequest req) {
        if (req == null) {
            return "request body is null";
        }
        if (isBlank(req.recipients())) {
            return "recipients is blank";
        }
        if (isBlank(req.subject())) {
            return "subject is blank";
        }
        if (isBlank(req.htmlBody())) {
            return "htmlBody is blank";
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
