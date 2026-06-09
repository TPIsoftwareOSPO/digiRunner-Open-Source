package tpi.dgrv4.digicare.mail;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * digiCare 寄信回應 payload。
 *
 * accepted 的語意是「dgr 已收下請求並呼叫 MailHelper.sendEmail」，
 * 不代表 SMTP 端真的投遞成功——MailHelper 內部會 catch 所有例外並寫入
 * tsmp_dp_mail_log，呼叫端拿不到實際投遞結果。
 * 真實投遞狀態的查詢屬於後續第二階段需求 5.1，本版不處理。
 *
 * @param accepted dgr 是否已接收請求並交給 MailHelper 處理
 * @param refCode 原樣回傳請求中的 refCode，方便呼叫端對應；可為 null
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MailSendResponse(
    boolean accepted,
    String refCode
) {
}
