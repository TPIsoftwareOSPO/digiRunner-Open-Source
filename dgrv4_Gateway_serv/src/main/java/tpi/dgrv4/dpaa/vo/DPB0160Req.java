package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class DPB0160Req extends ReqValidator {
	
	private String ldapUrl;
	
	private String ldapBaseDn;
	
	private String ldapDn;
	
	private Integer ldapTimeout;
	
	private String ldapStatus;
	
	private String approvalResultMail;
	
	private String iconFile;

	private String pageTitle;
	
	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

	public String getLdapDn() {
		return ldapDn;
	}

	public void setLdapDn(String ldapDn) {
		this.ldapDn = ldapDn;
	}

	public Integer getLdapTimeout() {
		return ldapTimeout;
	}

	public void setLdapTimeout(Integer ldapTimeout) {
		this.ldapTimeout = ldapTimeout;
	}

	public String getLdapStatus() {
		return ldapStatus;
	}

	public void setLdapStatus(String ldapStatus) {
		this.ldapStatus = ldapStatus;
	}

	public String getApprovalResultMail() {
		return approvalResultMail;
	}

	public void setApprovalResultMail(String approvalResultMail) {
		this.approvalResultMail = approvalResultMail;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		String URIRegex = "^(ldap?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		return Arrays.asList(new BeforeControllerRespItem[] {
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("ldapUrl")
					.isRequired()
					.pattern(URIRegex, TsmpDpAaRtnCode._1405.getCode(), null)
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("ldapBaseDn")
					.isRequired()
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("ldapDn")
					.isRequired()
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("ldapTimeout")
					.min(0)
					.max(1800000)
					.isRequired()
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("ldapStatus")
					.isRequired()
					.pattern(RegexpConstant.Y_OR_N,TsmpDpAaRtnCode._2007.getCode(),null)
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("approvalResultMail")
					.isRequired()
					.build(),
					new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("pageTitle")
					.isRequired()
					.build(),
		});
		
	}

}
