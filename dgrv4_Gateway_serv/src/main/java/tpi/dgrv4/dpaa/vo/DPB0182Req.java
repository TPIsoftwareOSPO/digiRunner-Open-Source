package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0182Req extends ReqValidator {
	private	String masterId;
	private String status;
	private Integer ldapTimeout;
	private String policy;
	private String approvalResultMail;
	private String iconFile;
	private String pageTitle;
	private List<DPB0182LdapDataItem> ldapDataList;
	public String getMasterId() {
		return masterId;
	}
	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getLdapTimeout() {
		return ldapTimeout;
	}
	public void setLdapTimeout(Integer ldapTimeout) {
		this.ldapTimeout = ldapTimeout;
	}
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
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
	public List<DPB0182LdapDataItem> getLdapDataList() {
		return ldapDataList;
	}
	public void setLdapDataList(List<DPB0182LdapDataItem> ldapDataList) {
		this.ldapDataList = ldapDataList;
	}
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("ldapTimeout")
				.isRequired()
				.min(0)
				.max(1800000)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("status")
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
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("policy")
				.isRequired()
				.pattern("^[S|R]$",TsmpDpAaRtnCode._2007.getCode(),null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildCollection(locale)
				.field("ldapDataList")
				.build()
	});
	}
}

