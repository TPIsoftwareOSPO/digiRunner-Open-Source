package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
import tpi.dgrv4.dpaa.util.ServiceUtil;

public class DPB0147Req extends ReqValidator {

	private String longId;

	private String userName;

	private String newUserName;

	private String newUserAlias;

	private String newStatus;

	private String newUserEmail;

	private String idpType;

	private String newIdpType;

	/** 原角色清單 */
	private List<String> roleIdList;

	/** 角色清單 */
	private List<String> newRoleIdList;

	/** 原組織單位名稱 */
	private String orgId;

	/** 組織單位名稱 */
	private String newOrgId;

	public String getLongId() {
		return longId;
	}

	public void setLongId(String longId) {
		this.longId = longId;
	}

	public List<String> getRoleIdList() {
		return roleIdList;
	}

	public void setRoleIdList(List<String> roleIdList) {
		this.roleIdList = roleIdList;
	}

	public List<String> getNewRoleIdList() {
		return newRoleIdList;
	}

	public void setNewRoleIdList(List<String> newRoleIdList) {
		this.newRoleIdList = newRoleIdList;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getNewOrgId() {
		return newOrgId;
	}

	public void setNewOrgId(String newOrgId) {
		this.newOrgId = newOrgId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNewUserName() {
		return newUserName;
	}

	public void setNewUserName(String newUserName) {
		this.newUserName = newUserName;
	}

	public String getNewUserAlias() {
		return newUserAlias;
	}

	public void setNewUserAlias(String newUserAlias) {
		this.newUserAlias = newUserAlias;
	}

	public String getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}

	public String getNewUserEmail() {
		return newUserEmail;
	}

	public void setNewUserEmail(String newUserEmail) {
		this.newUserEmail = newUserEmail;
	}

	public String getIdpType() {
		return idpType;
	}

	public void setIdpType(String idpType) {
		this.idpType = idpType;
	}

	public String getNewIdpType() {
		return newIdpType;
	}

	public void setNewIdpType(String newIdpType) {
		this.newIdpType = newIdpType;
	}

	public void switchCusIdpTypeUserName() {

		if (DgrIdPType.CUS.equalsIgnoreCase(this.idpType)) {
			this.userName = ServiceUtil.encodeBase64URL(this.userName);
		}

		if (DgrIdPType.CUS.equalsIgnoreCase(this.newIdpType)) {
			this.newUserName = ServiceUtil.encodeBase64URL(this.newUserName);
		}
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("newRoleIdList")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newOrgId")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newUserName")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newStatus")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newIdpType")
					.isRequired()
					.build(),
			});
	}

}
