package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA1108Req extends ReqValidator{

	/** 原始資料-核身型式代碼 */
	private String groupAuthoritieId;
	
	/** 原始資料-核身型式名稱 */
	private String oriGroupAuthoritieName;
	
	/** 新-核身型式名稱 */
	private String newGroupAuthoritieName;
	
	/** 新-核身型式描述 */
	private String newGroupAuthoritieDesc;
	
	/** 新-核身型式等級 */
	private String newGroupAuthoritieLevel;
	
	public String getGroupAuthoritieId() {
		return groupAuthoritieId;
	}

	public void setGroupAuthoritieId(String groupAuthoritieId) {
		this.groupAuthoritieId = groupAuthoritieId;
	}

	public String getOriGroupAuthoritieName() {
		return oriGroupAuthoritieName;
	}

	public void setOriGroupAuthoritieName(String oriGroupAuthoritieName) {
		this.oriGroupAuthoritieName = oriGroupAuthoritieName;
	}

	public String getNewGroupAuthoritieName() {
		return newGroupAuthoritieName;
	}

	public void setNewGroupAuthoritieName(String newGroupAuthoritieName) {
		this.newGroupAuthoritieName = newGroupAuthoritieName;
	}

	public String getNewGroupAuthoritieDesc() {
		return newGroupAuthoritieDesc;
	}

	public void setNewGroupAuthoritieDesc(String newGroupAuthoritieDesc) {
		this.newGroupAuthoritieDesc = newGroupAuthoritieDesc;
	}

	public String getNewGroupAuthoritieLevel() {
		return newGroupAuthoritieLevel;
	}

	public void setNewGroupAuthoritieLevel(String newGroupAuthoritieLevel) {
		this.newGroupAuthoritieLevel = newGroupAuthoritieLevel;
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
					.field("oriGroupAuthoritieName")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newGroupAuthoritieName")
					.isRequired()
					.maxLength(30)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newGroupAuthoritieDesc")
					.isRequired()
					.maxLength(60)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("newGroupAuthoritieLevel")
					.isRequired()
					.maxLength(1)
					.pattern("\\d", TsmpDpAaRtnCode._2021.getCode(), null)
					.build()
				
			});
	}


}
