package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class AutoInitSQLTsmpDpMailTpltVo extends BasicFieldsVo{

	private Long mailtpltId;

	private String remark;
	
	private String code;

	private String templateTxt;

	/* constructors */

	public AutoInitSQLTsmpDpMailTpltVo() {}

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
