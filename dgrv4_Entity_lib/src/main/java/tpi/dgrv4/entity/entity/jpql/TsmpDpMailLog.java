package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_mail_log")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpMailLog  {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "maillog_id")
	private Long maillogId;

	@Fuzzy
	@Column(name = "recipients")
	private String recipients;

	@Fuzzy
	@Column(name = "template_txt")
	private String templateTxt;

	@Fuzzy
	@Column(name = "ref_code")
	private String refCode;

	@Column(name = "result")
	private String result;
	
	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	@Column(name = "stack_trace")
	private String stackTrace;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	/* constructors */

	public TsmpDpMailLog() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpMailLog [maillogId=" + maillogId + ", recipients=" + recipients + ", templateTxt=" + templateTxt
				+ ", refCode=" + refCode + ", result=" + result + ", getCreateDateTime()=" + getCreateDateTime()
				+ ", getCreateUser()=" + getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime()
				+ ", getUpdateUser()=" + getUpdateUser() + ", getVersion()=" + getVersion() + ", getStackTrace()="
				+ getStackTrace() + "]\n";
	}

	/* getters and setters */

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

	public String getTemplateTxt() {
		return templateTxt;
	}

	public void setTemplateTxt(String templateTxt) {
		this.templateTxt = templateTxt;
	}

	public String getRefCode() {
		return refCode;
	}

	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
}
