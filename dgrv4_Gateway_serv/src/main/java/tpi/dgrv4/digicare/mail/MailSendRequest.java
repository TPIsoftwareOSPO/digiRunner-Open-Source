package tpi.dgrv4.digicare.mail;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * digiCare 寄信請求 payload。
 *
 * 對應 POST /internal/mail/send 的 JSON body。欄位設計刻意對齊
 * dgr 既有 TsmpMailEvent 的需求面，避免在 controller 層做多餘的轉換。
 *
 * 範例：
 *   {
 *     "recipients": "alice@example.com,bob@example.com",
 *     "subject": "[digiCare] 密碼重設通知",
 *     "htmlBody": "<html>...</html>",
 *     "refCode": "digicare-forgot-password-2026040701"
 *   }
 *
 * @param recipients 收件人，逗號分隔，至少一個合法 email
 * @param subject 信件主旨
 * @param htmlBody 信件內文，HTML 格式
 * @param refCode 追蹤碼，會寫入 tsmp_dp_mail_log 方便事後查詢；可為 null
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MailSendRequest(
    String recipients,
    String subject,
    String htmlBody,
    String refCode
) {
}
