package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA1106Req extends ReqValidator{

	/** 核身代碼 */
	private String groupAuthoritieId;
	
	/** 核身名稱 */
	private String groupAuthoritieName;
	
	/** 核身描述 */
	private String groupAuthoritieDesc;
	
	/** 核身等級 */
	private String groupAuthoritieLevel;
	
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

	public void setGroupAuthoritieDesc(String groupAuthoritieDesc) {
		this.groupAuthoritieDesc = groupAuthoritieDesc;
	}
	
	public String getGroupAuthoritieDesc() {
		return groupAuthoritieDesc;
	}

	public String getGroupAuthoritieLevel() {
		return groupAuthoritieLevel;
	}

	public void setGroupAuthoritieLevel(String groupAuthoritieLevel) {
		this.groupAuthoritieLevel = groupAuthoritieLevel;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieId")
					.isRequired()
					.maxLength(10)
					.pattern(RegexpConstant.ENGLISH_NUMBER, TsmpDpAaRtnCode._2008.getCode(), null)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieName")
					.isRequired()
					.maxLength(30)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieDesc")
					.isRequired()
					.maxLength(60)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("groupAuthoritieLevel")
					.isRequired()
					.maxLength(1)
					.pattern("\\d", TsmpDpAaRtnCode._2021.getCode(), null)
					.build()
				
			});
	}


}
