package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0156Req extends ReqValidator implements Serializable {

	// DGR_WEBSITE的主鍵
	private Long dgrWebsiteId;

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		
		return Arrays.asList(
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("dgrWebsiteId")
					.isRequired()
					.build());
	}
}
