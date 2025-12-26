package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class DPB0233Req extends ReqValidator {

	private String status;
	private List<DPB0233WhitelistItem> whitelistDataList;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<DPB0233WhitelistItem> getWhitelistDataList() {
		return whitelistDataList;
	}

	public void setWhitelistDataList(List<DPB0233WhitelistItem> whitelistDataList) {
		this.whitelistDataList = whitelistDataList;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		String[] params = { "status" };

		return Arrays.asList(new BeforeControllerRespItem[] { //

				new BeforeControllerRespItemBuilderSelector() //
						.buildString(locale) //
						.field("status") //
						.isRequired() //
						.pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2025.getCode(), params) //
						.build(), //
		});
	}
}
