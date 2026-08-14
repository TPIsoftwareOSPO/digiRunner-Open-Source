package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

@Getter
@Setter
public class DPB9940Req extends ReqValidator{
	private List<String> exportItemList;
	private List<String> exportLocaleList;
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("exportItemList")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("exportLocaleList")
					.isRequired()
					.build(),
		});
	}
}
