package tpi.dgrv4.dpaa.component;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.dpaa.vo.TsmpMailEvent;

public class TsmpMailEventBuilder {

	private String subject;

	private String content;

	private String recipients;

	private String createUser;

	private String refCode;

	public TsmpMailEventBuilder setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public TsmpMailEventBuilder setContent(String content) {
		this.content = content;
		return this;
	}

	public TsmpMailEventBuilder setRecipients(String recipients) {
		this.recipients = recipients;
		return this;
	}

	public TsmpMailEventBuilder setCreateUser(String createUser) {
		this.createUser = createUser;
		return this;
	}

	public TsmpMailEventBuilder setRefCode(String refCode) {
		this.refCode = refCode;
		return this;
	}

	public TsmpMailEvent build() {
 
		List<String> errFieldList = new LinkedList<>();
		if (this.subject == null){
			errFieldList.add("subject");
		}
		
		if (this.content == null){
			errFieldList.add("content");
		}
		
		if (this.recipients == null){
			errFieldList.add("recipients");
		}
		
		if (this.createUser == null){
			errFieldList.add("createUser");
		}
		
		if (this.refCode == null){
			errFieldList.add("refCode");
		}
		 
		if(!CollectionUtils.isEmpty(errFieldList)) {
			// 缺少必需的參數
			String errMsg = String.format("TsmpMailEvent missing required parameter: %s", errFieldList);
			throw new IllegalArgumentException(errMsg);
		}
		
		return new TsmpMailEvent(this.subject, this.content //
				, this.recipients, this.createUser, this.refCode);
	}
	
}
