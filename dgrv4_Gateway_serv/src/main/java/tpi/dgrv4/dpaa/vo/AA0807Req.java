package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0807Req extends ReqValidator{

	/** 註冊主機ID*/
	private String regHostID;

	public String getRegHostID() {
		return regHostID;
	}

	public void setRegHostID(String regHostID) {
		this.regHostID = regHostID;
	}

	@Override
	public String toString() {
		return "AA0807Req [regHostID=" + regHostID + "]";
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("regHostID")
					.isRequired()
					.build()
		});
	}
}
