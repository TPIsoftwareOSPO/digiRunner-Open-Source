package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9909Req extends ReqValidator {

	/** 分類編號 */
	private String itemNo;

	/** 是否為預設 */
	private String isDefault;

	/** (原)子分類編號 */
	private String oriSubitemNo;

	/** 子分類編號 */
	private String subitemNo;

	/** 子分類名稱清單 */
	private List<DPB9909Item> subitemNameList;

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String getOriSubitemNo() {
		return oriSubitemNo;
	}

	public void setOriSubitemNo(String oriSubitemNo) {
		this.oriSubitemNo = oriSubitemNo;
	}

	public String getSubitemNo() {
		return subitemNo;
	}

	public void setSubitemNo(String subitemNo) {
		this.subitemNo = subitemNo;
	}

	public List<DPB9909Item> getSubitemNameList() {
		return subitemNameList;
	}

	public void setSubitemNameList(List<DPB9909Item> subitemNameList) {
		this.subitemNameList = subitemNameList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("itemNo")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("subitemNo")
				.isRequired()
				.build(),
			new BeforeControllerRespItemBuilderSelector()
				.buildCollection(locale)
				.field("subitemNameList")
				.isRequired()
				.build()
		});
	}

}