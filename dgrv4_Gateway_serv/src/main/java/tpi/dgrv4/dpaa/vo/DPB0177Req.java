package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0177Req extends ReqValidator{
	private String longId;
	private String targetWs;
	private String memo;
	private String auth;
	
	public String getLongId() {
		return longId;
	}
	public void setLongId(String longId) {
		this.longId = longId;
	}
	public String getTargetWs() {
		return targetWs;
	}
	public void setTargetWs(String targetWs) {
		this.targetWs = targetWs;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getAuth() {
		return auth;
	}
	public void setAuth(String auth) {
		this.auth = auth;
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("targetWs")
					.isRequired()
					.maxLength(200)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("memo")
					.maxLength(2000)
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("auth")
					.isRequired()
					.pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2007.getCode(), null)
					.build(),
		});
	}
	
}
