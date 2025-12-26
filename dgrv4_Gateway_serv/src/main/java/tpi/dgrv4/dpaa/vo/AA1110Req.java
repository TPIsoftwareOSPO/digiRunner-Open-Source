package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA1110Req extends ReqValidator{

	/** 核身型式代碼*/
	private String groupAuthoritieId;
	
	/** 核身型式名稱*/
	private String groupAuthoritieName;
	
	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}

	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}

	public String getGroupAuthoritieName() {
		return groupAuthoritieName;
	}

	public void setGroupAuthoritieName(String groupAuthoritieName) {
		this.groupAuthoritieName = groupAuthoritieName;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieId")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieName")
					.isRequired()
					.build()
				
			});
	}


}
