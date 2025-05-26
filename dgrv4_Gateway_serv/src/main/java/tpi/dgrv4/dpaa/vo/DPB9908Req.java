package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB9908Req extends ReqValidator {

	/** (原)分類編號 */
	private String oriItemNo;

	/** 分類編號 */
	private String itemNo;

	/** 分類名稱清單 */
	private List<DPB9908Item> dataList;

	public String getOriItemNo() {
		return oriItemNo;
	}

	public void setOriItemNo(String oriItemNo) {
		this.oriItemNo = oriItemNo;
	}

	public String getItemNo() {
		return itemNo;
	}

	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}

	public List<DPB9908Item> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB9908Item> dataList) {
		this.dataList = dataList;
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
				.buildCollection(locale)
				.field("dataList")
				.isRequired()
				.build()
		});
	}

}
