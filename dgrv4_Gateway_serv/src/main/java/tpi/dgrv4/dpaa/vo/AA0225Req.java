package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0225Req extends ReqValidator{

	/** 虛擬群組ID*/
	private String vgroupId;
	
	public String getVgroupId() {
		return vgroupId;
	}


	public void setVgroupId(String vgroupId) {
		this.vgroupId = vgroupId;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("vgroupId")
					.isRequired()
					.build()
			});
	}

	@Override
	public String toString() {
		return "AA0225Req [vgroupId=" + vgroupId + "]";
	}
	
}
