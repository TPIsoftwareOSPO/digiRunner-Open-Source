package tpi.dgrv4.dpaa.vo;

public class DPB0116Data {
	private Long maillogId;
	private String recipients;
	private String subject;
	private String createDate;
	private String result;

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

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
