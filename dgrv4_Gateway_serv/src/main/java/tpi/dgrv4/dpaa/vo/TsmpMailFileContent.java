package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class TsmpMailFileContent {
	
	private String mailType;//mailType=內文相同/內文不同
	
	private String filename;//mailFileName=timestamp.jobID.mail
	
	private String identifData;//識別資料 
	
	private String subject;

	private String content;

	private List<String> recipientsList;

	// 紀錄在 tsmp_dp_mail_log 用
	private String createUser;

	// 紀錄在 tsmp_dp_mail_log 用
	private String refCode;

	public String getMailType() {
		return mailType;
	}

	public void setMailType(String mailType) {
		this.mailType = mailType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getIdentifData() {
		return identifData;
	}

	public void setIdentifData(String identifData) {
		this.identifData = identifData;
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

	public List<String> getRecipientsList() {
		return recipientsList;
	}

	public void setRecipientsList(List<String> recipientsList) {
		this.recipientsList = recipientsList;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getRefCode() {
		return refCode;
	}

	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}

	@Override
	public String toString() {
		return "TsmpMailFileContent [mailType=" + mailType + ", filename=" + filename + ", identifData=" + identifData
				+ ", subject=" + subject + ", content=" + content + ", recipientsList=" + recipientsList
				+ ", createUser=" + createUser + ", refCode=" + refCode + "]";
	}
}
