package tpi.dgrv4.gateway.vo;

public class QueryMailLogData {

    private String refCode;

    private Long maillogId;

    private String recipients;

    /** 由 template_txt 拆出的主旨 */
    private String subject;

    /** 由 template_txt 拆出的內文 */
    private String content;

    /** 原始碼值, "1":成功, "0":失敗; 不做 RESULT_FLAG 字典轉譯, 由呼叫端自行判讀 */
    private String result;

    /** 僅 result 為 "0" 時回 stack_trace */
    private String errorMsg;

    /** epoch millis */
    private Long createDateTime;

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public Long getMaillogId() {
        return maillogId;
    }

    public void setMaillogId(Long maillogId) {
        this.maillogId = maillogId;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Long createDateTime) {
        this.createDateTime = createDateTime;
    }
}
