package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0113Req extends ReqValidator {

	private String oriRoleId;

	private String oriListType;

	private List<String> oriTxIdList;

	private String newRoleId;

	private String newListType;

	private String newTxId;

	public String getOriRoleId() {
		return oriRoleId;
	}

	public void setOriRoleId(String oriRoleId) {
		this.oriRoleId = oriRoleId;
	}

	public String getOriListType() {
		return oriListType;
	}

	public void setOriListType(String oriListType) {
		this.oriListType = oriListType;
	}

	public List<String> getOriTxIdList() {
		return oriTxIdList;
	}

	public void setOriTxIdList(List<String> oriTxIdList) {
		this.oriTxIdList = oriTxIdList;
	}

	public String getNewRoleId() {
		return newRoleId;
	}

	public void setNewRoleId(String newRoleId) {
		this.newRoleId = newRoleId;
	}

	public String getNewListType() {
		return newListType;
	}

	public void setNewListType(String newListType) {
		this.newListType = newListType;
	}

	public String getNewTxId() {
		return newTxId;
	}

	public void setNewTxId(String newTxId) {
		this.newTxId = newTxId;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("oriRoleId")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("oriListType")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("newRoleId")
				.isRequired()
				.maxLength(10)
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("newListType")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("newTxId")
				.isRequired()
				.build()
		});
	}

}