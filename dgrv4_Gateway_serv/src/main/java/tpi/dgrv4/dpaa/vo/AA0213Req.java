package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0213Req extends ReqValidator{

	// 群組編號
	private String groupID;

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	
	public String getGroupID() {
		return groupID;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupID")
					.isRequired()
					.build()
			});
	}
}
