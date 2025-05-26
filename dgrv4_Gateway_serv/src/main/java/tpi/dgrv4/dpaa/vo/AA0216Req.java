package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0216Req extends ReqValidator{

	/** 用戶端帳號 */
	private String clientID;

	/** 群組名稱 */
	private List<String> groupIDList;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public List<String> getGroupIDList() {
		return groupIDList;
	}

	public void setGroupIDList(List<String> groupIDList) {
		this.groupIDList = groupIDList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("clientID")
					.isRequired()
					.build()
			});
	}


}
