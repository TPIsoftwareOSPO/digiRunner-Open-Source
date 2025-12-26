package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0124Req extends ReqValidator {

	/** 索引名稱 */
	private List<String> indexList;

	/** 設定Index開啟或關閉  */
	/** 
	 * 使用BcryptParam, 
	 * ITEM_NO='ES_INDEX_FLAG' , 
	 * DB儲存值對應代碼如下: DB值 (PARAM1) = 中文說明; 1=啟用, 0=關閉
	 */
	private String isOpen;	

	public List<String> getIndexList() {
		return indexList;
	}

	public void setIndexList(List<String> indexList) {
		this.indexList = indexList;
	}

	public String getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(String isOpen) {
		this.isOpen = isOpen;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildCollection(locale)
				.field("indexList")
				.isRequired()
				.min(1)
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("isOpen")
				.isRequired()
				.build()
		});
	}

}
