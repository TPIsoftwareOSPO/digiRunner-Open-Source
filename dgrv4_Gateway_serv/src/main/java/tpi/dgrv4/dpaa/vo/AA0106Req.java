package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class AA0106Req extends ReqValidator{
	private String embeddedUrl;
	private List<AA0106Item> funcList;	
	private Integer type;
	private String masterFuncCode;
	private Boolean isKibana;

	public String getEmbeddedUrl() {
		return embeddedUrl;
	}

	public void setEmbeddedUrl(String embeddedUrl) {
		this.embeddedUrl = embeddedUrl;
	}

	public List<AA0106Item> getFuncList() {
		return funcList;
	}

	public void setFuncList(List<AA0106Item> funcList) {
		this.funcList = funcList;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getMasterFuncCode() {
		return masterFuncCode;
	}

	public void setMasterFuncCode(String masterFuncCode) {
		this.masterFuncCode = masterFuncCode;
	}

	public Boolean getIsKibana() {
		return isKibana;
	}

	public void setIsKibana(Boolean isKibana) {
		this.isKibana = isKibana;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("funcList")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("type")
					.isRequired()
					.build()
		
		}
		);
		
	}
}
