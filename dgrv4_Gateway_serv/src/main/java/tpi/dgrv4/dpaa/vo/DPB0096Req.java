package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0096Req extends ReqValidator {

	/** 回覆代碼*/
	private String tsmpRtnCode;
	
	/** 語言地區 */
	private String locale;
	
	/** 顯示的回覆訊息 */
	private String tsmpRtnMsg;
	
	/** 說明  */
	private String tsmpRtnDesc;
	
	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public String getLocale() {
		return locale;
	}
	
	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public String getTsmpRtnMsg() {
		return tsmpRtnMsg;
	}
	
	public void setTsmpRtnMsg(String tsmpRtnMsg) {
		this.tsmpRtnMsg = tsmpRtnMsg;
	}
	
	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("tsmpRtnCode")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("locale")
				.isRequired()
				.build()
			,
			new BeforeControllerRespItemBuilderSelector() //
				.buildString(locale)
				.field("tsmpRtnMsg")
				.isRequired()
				.build()
		});
	}

}
