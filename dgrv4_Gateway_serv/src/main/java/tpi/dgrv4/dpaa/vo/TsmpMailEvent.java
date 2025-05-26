package tpi.dgrv4.dpaa.vo;

public class TsmpMailEvent {

	private final String subject;

	private final String content;

	private final String recipients;

	// 紀錄在 tsmp_dp_mail_log 用
	private final String createUser;

	// 紀錄在 tsmp_dp_mail_log 用
	private final String refCode;

	public TsmpMailEvent(String subject, String content, String recipients //
			, String createUser, String refCode) {
		this.subject = subject;
		this.content = content;
		this.recipients = recipients;
		this.createUser = createUser;
		this.refCode = refCode;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public String getRecipients() {
		return recipients;
	}

	public String getCreateUser() {
		return this.createUser;
	}

	public String getRefCode() {
		return this.refCode;
	}

	@Override
	public String toString() {
		return "TsmpMailEvent [subject=" + subject + ", content=" + content + ", recipients=" + recipients
				+ ", createUser=" + createUser + ", refCode=" + refCode + "]\n";
	}
}
