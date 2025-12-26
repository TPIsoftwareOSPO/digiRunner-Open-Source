package tpi.dgrv4.entity.entity.autoInitSQL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "tsmp_dp_mail_tplt")
public class AutoInitSQLTsmpDpMailTplt extends BasicFields{
	@Id
	@Column(name = "mailtplt_id")
	private Long mailtpltId;

	@Fuzzy
	@Column(name = "remark")
	private String remark;
	
	@Fuzzy
	@Column(name = "code")
	private String code;

	@Fuzzy
	@Column(name = "template_txt")
	private String templateTxt;

	/* constructors */

	public AutoInitSQLTsmpDpMailTplt() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpMailTplt [mailtpltId=" + mailtpltId + ", code=" + code + ", templateTxt=" + templateTxt
				+ ", remark=" + remark + ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()="
				+ getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()="
				+ getUpdateUser() + ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch()
				+ "]";
	}

	/* getters and setters */

	public void setMailtpltId(Long mailtpltId) {
		this.mailtpltId = mailtpltId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTemplateTxt() {
		return templateTxt;
	}

	public void setTemplateTxt(String templateTxt) {
		this.templateTxt = templateTxt;
	}

	public Long getMailtpltId() {
		return mailtpltId;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
