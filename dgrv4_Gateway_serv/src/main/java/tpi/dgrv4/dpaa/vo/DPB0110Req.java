package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0110Req extends ReqValidator {

	/** 角色代碼 */
	private String roleId;

	/** 交易代碼, ","分隔字串 */
	private String txId;

	/** 名單類型 */
	private String listType;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("roleId")
				.isRequired()
				.maxLength(10)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("txId")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("listType")
				.isRequired()
				.build()
		});
	}

}