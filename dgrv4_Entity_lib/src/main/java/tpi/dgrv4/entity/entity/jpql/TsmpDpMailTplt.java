package tpi.dgrv4.entity.entity.jpql;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@SuppressWarnings("serial")
@Entity
@Table(name = "tsmp_dp_mail_tplt")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpMailTplt extends BasicFields implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "mailtplt_id")
	private Long mailtpltId;

	@Fuzzy
	@Column(name = "code")
	private String code;

	@Fuzzy
	@Column(name = "template_txt")
	private String templateTxt;

	@Fuzzy
	@Column(name = "remark")
	private String remark;

	/* constructors */

	public TsmpDpMailTplt() {}

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

	public Long getMailtpltId() {
		return mailtpltId;
	}

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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
