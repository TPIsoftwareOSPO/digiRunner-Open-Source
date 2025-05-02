package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0125Req extends ReqValidator {

	/** Start Time */
	private String timeS;

	/** End Time */
	private String timeE;

	/** Index Name */
	private String idxName;

	public String getTimeS() {
		return timeS;
	}

	public void setTimeS(String timeS) {
		this.timeS = timeS;
	}

	public String getTimeE() {
		return timeE;
	}

	public void setTimeE(String timeE) {
		this.timeE = timeE;
	}

	public String getIdxName() {
		return idxName;
	}

	public void setIdxName(String idxName) {
		this.idxName = idxName;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("timeS")
				// 時間格式在service檢查
				.isRequired(TsmpDpAaRtnCode._1366.getCode(), new String[0])
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				// 時間格式在service檢查
				.field("timeE")
				.isRequired(TsmpDpAaRtnCode._1366.getCode(), new String[0])
				.build(),
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("idxName")
				.isRequired()
				.build()
		});
	}

}